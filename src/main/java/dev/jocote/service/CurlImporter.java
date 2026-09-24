package dev.jocote.service;

import dev.jocote.model.AuthConfig;
import dev.jocote.model.KeyValue;
import dev.jocote.model.RequestDefinition;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Imports the supported literal cURL subset without processes, file access or network I/O. */
public final class CurlImporter {
    private static final Map<String, String> SHORT_OPTIONS = Map.ofEntries(
            Map.entry("X", "--request"), Map.entry("H", "--header"), Map.entry("d", "--data"),
            Map.entry("u", "--user"), Map.entry("m", "--max-time"), Map.entry("A", "--user-agent"),
            Map.entry("I", "--head"), Map.entry("G", "--get"), Map.entry("g", "--globoff"),
            Map.entry("s", "--silent"), Map.entry("S", "--show-error"), Map.entry("i", "--include"));
    private static final Set<String> VALUES = Set.of("--request", "--header", "--data", "--data-ascii",
            "--data-raw", "--data-binary", "--data-urlencode", "--json", "--user", "--max-time", "--url", "--user-agent");
    private static final Set<String> FLAGS = Set.of("--head", "--get", "--globoff", "--basic",
            "--silent", "--show-error", "--include", "--show-headers");
    private final RequestPreparer preparer;

    public CurlImporter(RequestPreparer preparer) { this.preparer = Objects.requireNonNull(preparer); }

    public record Result(RequestDefinition request, List<String> notices) {
        public Result { notices = List.copyOf(notices); }
    }

    public Result parse(String command, CurlGenerator.Shell shell) {
        var tokens = new ArrayList<>(CurlTokenizer.tokenize(command, Objects.requireNonNull(shell)));
        if (tokens.isEmpty() || !(tokens.getFirst().equals("curl") || tokens.getFirst().equals("curl.exe"))) {
            throw new IllegalArgumentException("El comando debe comenzar con curl o curl.exe.");
        }
        String url = null;
        String method = null;
        String userAgent = null;
        AuthConfig auth = AuthConfig.none();
        var headers = new ArrayList<KeyValue>();
        var bodyParts = new ArrayList<String>();
        var notices = new ArrayList<String>();
        boolean head = false, get = false, json = false, globoff = false, positional = false;
        int timeout = 30;
        for (int i = 1; i < tokens.size(); i++) {
            String option = tokens.get(i);
            if (!positional && option.equals("--")) { positional = true; continue; }
            if (positional || !option.startsWith("-")) {
                if (url != null) throw new IllegalArgumentException("Importa una sola URL por petición.");
                url = option; continue;
            }
            String value = null;
            if (option.startsWith("--")) {
                int equals = option.indexOf('=');
                if (equals >= 0) { value = option.substring(equals + 1); option = option.substring(0, equals); }
            } else if (option.length() >= 2) {
                String shortName = option.substring(1, 2);
                String expanded = SHORT_OPTIONS.get(shortName);
                if (expanded == null) throw unsupported("-" + shortName);
                if (option.length() > 2) {
                    if (VALUES.contains(expanded)) value = option.substring(2);
                    else {
                        if (option.length() > 64) throw new IllegalArgumentException("Demasiadas opciones cortas agrupadas.");
                        tokens.set(i, "-" + option.substring(2)); i--;
                    }
                }
                option = expanded;
            }
            if (!VALUES.contains(option) && !FLAGS.contains(option)) throw unsupported(option);
            if (FLAGS.contains(option) && value != null) throw new IllegalArgumentException("La opción " + option + " no acepta un valor.");
            if (VALUES.contains(option) && value == null) {
                if (i + 1 >= tokens.size()) throw new IllegalArgumentException("Falta el valor de " + option + ".");
                value = tokens.get(++i);
            }
            switch (option) {
                case "--url" -> {
                    if (url != null) throw new IllegalArgumentException("Importa una sola URL por petición.");
                    url = value;
                }
                case "--request" -> {
                    if (value.isBlank()) throw new IllegalArgumentException("El método no puede estar vacío.");
                    method = value;
                }
                case "--header" -> headers.add(parseHeader(value));
                case "--user-agent" -> userAgent = value;
                case "--user" -> {
                    int colon = value.indexOf(':');
                    if (colon < 0) throw new IllegalArgumentException("Usa --user 'usuario:contraseña'; no se solicitan contraseñas interactivamente.");
                    auth = new AuthConfig(AuthConfig.Type.BASIC, value.substring(0, colon), value.substring(colon + 1), "", AuthConfig.Placement.HEADER);
                }
                case "--max-time" -> {
                    try { timeout = Integer.parseInt(value); }
                    catch (NumberFormatException e) { throw new IllegalArgumentException("--max-time requiere segundos enteros entre 1 y 600."); }
                    if (timeout < 1 || timeout > 600) throw new IllegalArgumentException("--max-time requiere segundos enteros entre 1 y 600.");
                }
                case "--json" -> {
                    rejectFile(value);
                    if (!bodyParts.isEmpty() && !json) throw new IllegalArgumentException("No se admite mezclar --json con otras opciones de datos.");
                    json = true; bodyParts.add(value);
                }
                case "--data", "--data-ascii", "--data-raw", "--data-binary", "--data-urlencode" -> {
                    if (json) throw new IllegalArgumentException("No se admite mezclar --json con otras opciones de datos.");
                    if (!option.equals("--data-raw")) rejectFile(value);
                    bodyParts.add(option.equals("--data-urlencode") ? encodeData(value) : value);
                }
                case "--head" -> head = true;
                case "--get" -> get = true;
                case "--globoff" -> globoff = true;
                case "--basic" -> { /* Basic is the only supported --user scheme. */ }
                default -> {
                    String notice = "Las opciones de salida de consola (-s, -S, -i) se omiten; Jocote muestra la respuesta en su panel.";
                    if (!notices.contains(notice)) notices.add(notice);
                }
            }
        }
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Falta una URL completa http:// o https://.");
        if (!globoff) rejectUrlGlobs(url);
        if (json && get) throw new IllegalArgumentException("No se admite combinar --json con --get; usa --data-urlencode para parámetros de query.");
        if (head && method != null && !method.equals("HEAD")) throw new IllegalArgumentException("No se admite combinar --head con un método distinto de HEAD.");
        if (head && !get && !bodyParts.isEmpty()) throw new IllegalArgumentException("--head no admite body; usa --get para enviar los datos como query.");
        if (auth.type() != AuthConfig.Type.NONE && hasHeader(headers, "Authorization")) {
            throw new IllegalArgumentException("Usa --user o un header Authorization, sin combinarlos.");
        }
        if (userAgent != null) {
            if (userAgent.isEmpty()) throw new IllegalArgumentException("No se admite suprimir User-Agent con un valor vacío.");
            if (!hasHeader(headers, "User-Agent")) headers.add(new KeyValue(true, "User-Agent", userAgent));
        }
        String body = String.join(json ? "" : "&", bodyParts);
        boolean hasBody = !bodyParts.isEmpty() && !get;
        if (get && !body.isEmpty()) {
            int fragment = url.indexOf('#');
            if (fragment >= 0) url = url.substring(0, fragment);
            url += (url.contains("?") ? (url.endsWith("?") || url.endsWith("&") ? "" : "&") : "?") + body;
        }
        if (json && !hasHeader(headers, "Accept")) headers.add(new KeyValue(true, "Accept", "application/json"));
        if (hasBody && !hasHeader(headers, "Content-Type")) {
            headers.add(new KeyValue(true, "Content-Type", json ? "application/json" : "application/x-www-form-urlencoded"));
        }
        var bodyType = RequestDefinition.BodyType.NONE;
        if (hasBody) {
            String contentType = headers.stream().filter(h -> h.key().equalsIgnoreCase("Content-Type"))
                    .map(KeyValue::value).findFirst().orElse("").toLowerCase(Locale.ROOT);
            bodyType = contentType.contains("json") ? RequestDefinition.BodyType.JSON
                    : contentType.contains("xml") ? RequestDefinition.BodyType.XML : RequestDefinition.BodyType.TEXT;
        }
        if (method == null) method = head ? "HEAD" : hasBody ? "POST" : "GET";
        var request = new RequestDefinition(null, "Importada de cURL", method, url, List.of(), headers,
                auth, bodyType, hasBody ? body : "", List.of(), timeout);
        // Use the same validation and normalization contract as Send and the cURL panel.
        var prepared = preparer.prepare(request);
        request = new RequestDefinition(request.id(), request.name(), prepared.method(), url, List.of(), headers,
                auth, bodyType, request.body(), List.of(), timeout);
        return new Result(request, notices);
    }

    private static KeyValue parseHeader(String value) {
        rejectFile(value);
        int colon = value.indexOf(':');
        if (colon > 0) {
            String content = value.substring(colon + 1).stripLeading();
            if (content.isBlank()) throw new IllegalArgumentException("La supresión de headers con 'Nombre:' no está soportada. Para un valor vacío usa 'Nombre;'.");
            return new KeyValue(true, value.substring(0, colon).trim(), content);
        }
        if (value.endsWith(";") && value.length() > 1) return new KeyValue(true, value.substring(0, value.length() - 1), "");
        throw new IllegalArgumentException("Header inválido; usa 'Nombre: valor' o 'Nombre;'.");
    }

    private static String encodeData(String value) {
        int equals = value.indexOf('=');
        if (equals < 0 && value.contains("@")) throw new IllegalArgumentException("--data-urlencode con archivos no está soportado.");
        String prefix = equals > 0 ? value.substring(0, equals + 1) : "";
        String content = equals >= 0 ? value.substring(equals + 1) : value;
        return prefix + URLEncoder.encode(content, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static boolean hasHeader(List<KeyValue> headers, String name) {
        return headers.stream().anyMatch(h -> h.key().equalsIgnoreCase(name));
    }

    private static void rejectFile(String value) {
        if (value.startsWith("@")) throw new IllegalArgumentException("No se importan archivos ni stdin (@archivo / @-). Pega el contenido usando --data-raw.");
    }

    private static void rejectUrlGlobs(String url) {
        String withoutHost;
        try {
            var uri = URI.create(url);
            withoutHost = (uri.getRawPath() == null ? "" : uri.getRawPath()) + (uri.getRawQuery() == null ? "" : uri.getRawQuery());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("URL inválida o con expansiones cURL. Usa una URL literal codificada.");
        }
        if (withoutHost.matches(".*[\\[\\]{}].*")) throw new IllegalArgumentException("No se expanden rangos/listas de URL; usa una URL literal o --globoff.");
    }

    private static IllegalArgumentException unsupported(String option) {
        String safeName = option.matches("--?[a-zA-Z0-9-]{1,40}") ? option : "desconocida";
        return new IllegalArgumentException("Opción no soportada: " + safeName + ". No se importó la petición; elimina esa opción solo si puedes prescindir de su comportamiento.");
    }
}
