package dev.jocote.model;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public record ResponseData(int statusCode, Map<String, List<String>> headers, byte[] body,
                           long elapsedMillis, URI uri, String protocol) {
    private static final Pattern CHARSET = Pattern.compile("charset\\s*=\\s*[\"']?([^;\\s\"']+)", Pattern.CASE_INSENSITIVE);

    public String text() {
        var charset = StandardCharsets.UTF_8;
        var matcher = CHARSET.matcher(contentType());
        if (matcher.find()) {
            try { charset = Charset.forName(matcher.group(1)); }
            catch (IllegalArgumentException ignored) { /* Unknown encoding: readable UTF-8 fallback. */ }
        }
        return new String(body, charset);
    }

    public String contentType() {
        return headers.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase("content-type"))
                .flatMap(e -> e.getValue().stream()).findFirst().orElse("");
    }
}
