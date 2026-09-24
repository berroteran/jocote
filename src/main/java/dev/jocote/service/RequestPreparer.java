package dev.jocote.service;

import dev.jocote.model.AuthConfig;
import dev.jocote.model.KeyValue;
import dev.jocote.model.PreparedRequest;
import dev.jocote.model.RequestDefinition;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class RequestPreparer {
    private static final Set<String> RESTRICTED = Set.of("connection", "content-length", "expect", "host", "upgrade");

    public PreparedRequest prepare(RequestDefinition request) {
        String method = request.method().trim().toUpperCase(Locale.ROOT);
        if (!method.matches("[!#$%&'*+.^_`|~0-9A-Z-]+") || method.equals("CONNECT")) {
            throw new IllegalArgumentException("Método HTTP inválido o no soportado: " + method);
        }
        if (request.timeoutSeconds() < 1 || request.timeoutSeconds() > 600) {
            throw new IllegalArgumentException("El timeout debe estar entre 1 y 600 segundos.");
        }
        URI uri;
        try { uri = URI.create(request.url().trim()); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("La URL no es válida. Codifica los espacios como %20.", e); }
        if (uri.getScheme() == null || !(uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("Escribe una URL completa http:// o https:// con un host válido.");
        }
        if (uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("Configura las credenciales en Autorización, no dentro de la URL.");
        }
        var query = new ArrayList<>(active(request.parameters()));
        var headers = new ArrayList<>(active(request.headers()));
        AuthConfig auth = request.auth();
        switch (auth.type()) {
            case NONE -> { }
            case BEARER -> {
                if (auth.secret().isBlank()) throw new IllegalArgumentException("Escribe el token Bearer.");
                replaceHeader(headers, "Authorization", "Bearer " + auth.secret());
            }
            case BASIC -> {
                if (auth.username().contains(":")) throw new IllegalArgumentException("El usuario Basic no puede contener ':'.");
                String token = Base64.getEncoder().encodeToString((auth.username() + ":" + auth.secret()).getBytes(StandardCharsets.UTF_8));
                replaceHeader(headers, "Authorization", "Basic " + token);
            }
            case API_KEY -> {
                if (auth.key().isBlank()) throw new IllegalArgumentException("Escribe el nombre de la API key.");
                if (auth.placement() == AuthConfig.Placement.HEADER) replaceHeader(headers, auth.key().trim(), auth.secret());
                else query.add(new KeyValue(true, auth.key(), auth.secret()));
            }
        }
        String raw = uri.toASCIIString();
        int fragmentIndex = raw.indexOf('#');
        if (fragmentIndex >= 0) raw = raw.substring(0, fragmentIndex);
        String addedQuery = encodeFields(query);
        if (!addedQuery.isEmpty()) {
            raw += (uri.getRawQuery() == null ? "?" : raw.endsWith("?") || raw.endsWith("&") ? "" : "&") + addedQuery;
        }
        uri = URI.create(raw);
        boolean hasBody = request.bodyType() != RequestDefinition.BodyType.NONE;
        if (method.equals("HEAD") && hasBody) throw new IllegalArgumentException("HEAD debe enviarse sin body.");
        String body = request.bodyType() == RequestDefinition.BodyType.FORM ? encodeFields(active(request.formFields())) : request.body();
        String contentType = switch (request.bodyType()) {
            case NONE -> "";
            case JSON -> "application/json; charset=UTF-8";
            case TEXT -> "text/plain; charset=UTF-8";
            case XML -> "application/xml; charset=UTF-8";
            case FORM -> "application/x-www-form-urlencoded; charset=UTF-8";
        };
        if (hasBody && headers.stream().noneMatch(h -> h.key().equalsIgnoreCase("Content-Type"))) {
            headers.add(new KeyValue(true, "Content-Type", contentType));
        }
        for (KeyValue header : headers) {
            if (RESTRICTED.contains(header.key().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("El encabezado " + header.key() + " lo administra el cliente HTTP; desactívalo.");
            }
            try { HttpRequest.newBuilder(uri).header(header.key(), header.value()); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException("Nombre o valor de encabezado inválido: " + header.key(), e); }
        }
        return new PreparedRequest(method, uri, headers, hasBody ? body : "", hasBody, request.timeoutSeconds());
    }

    private static List<KeyValue> active(List<KeyValue> fields) {
        return fields.stream().filter(KeyValue::enabled).filter(f -> !f.key().isBlank())
                .map(f -> new KeyValue(true, f.key().trim(), f.value())).toList();
    }

    private static String encodeFields(List<KeyValue> fields) {
        return fields.stream().map(f -> encode(f.key()) + "=" + encode(f.value())).collect(Collectors.joining("&"));
    }

    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private static void replaceHeader(List<KeyValue> headers, String name, String value) {
        headers.removeIf(h -> h.key().equalsIgnoreCase(name));
        headers.add(new KeyValue(true, name, value));
    }
}
