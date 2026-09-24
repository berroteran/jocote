package dev.jocote.service;

import dev.jocote.model.AuthConfig;
import dev.jocote.model.KeyValue;
import dev.jocote.model.RequestDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestPreparerTest {
    private final RequestPreparer preparer = new RequestPreparer();

    static RequestDefinition request(String method, String url, List<KeyValue> parameters, List<KeyValue> headers,
                                     AuthConfig auth, RequestDefinition.BodyType type, String body, List<KeyValue> form, int timeout) {
        return new RequestDefinition(null, "Test", method, url, parameters, headers, auth, type, body, form, timeout);
    }

    @Test void encodesParametersPreservingExistingQueryAndDuplicates() {
        var request = request("get", "https://example.com/a?existing=%2F#ignored", List.of(
                new KeyValue(true, "q", "café & café"), new KeyValue(true, "tag", "a"), new KeyValue(true, "tag", "b"),
                new KeyValue(false, "ignored", "secret")), List.of(), AuthConfig.none(), RequestDefinition.BodyType.NONE, "stale", List.of(), 30);
        var prepared = preparer.prepare(request);
        assertEquals("https://example.com/a?existing=%2F&q=caf%C3%A9+%26+caf%C3%A9&tag=a&tag=b", prepared.uri().toString());
        assertEquals("GET", prepared.method()); assertFalse(prepared.hasBody()); assertEquals("", prepared.body());
    }

    @Test void authorizationReplacesExistingHeaderCaseInsensitively() {
        var request = request("POST", "https://example.com", List.of(), List.of(new KeyValue(true, "authorization", "old")),
                new AuthConfig(AuthConfig.Type.BEARER, "", "abc", "", null), RequestDefinition.BodyType.JSON, "{}", List.of(), 30);
        var headers = preparer.prepare(request).headers();
        assertEquals(1, headers.stream().filter(h -> h.key().equalsIgnoreCase("Authorization")).count());
        assertTrue(headers.contains(new KeyValue(true, "Authorization", "Bearer abc")));
        assertTrue(headers.contains(new KeyValue(true, "Content-Type", "application/json; charset=UTF-8")));
    }

    @Test void supportsBasicAndApiKeyInQuery() {
        var basic = request("GET", "http://localhost", List.of(), List.of(),
                new AuthConfig(AuthConfig.Type.BASIC, "user", "pass", "", null), RequestDefinition.BodyType.NONE, "", List.of(), 30);
        assertEquals("Basic dXNlcjpwYXNz", preparer.prepare(basic).headers().getFirst().value());
        var key = request("GET", "http://localhost?", List.of(), List.of(),
                new AuthConfig(AuthConfig.Type.API_KEY, "", "a+b", "key", AuthConfig.Placement.QUERY), RequestDefinition.BodyType.NONE, "", List.of(), 30);
        assertEquals("http://localhost?key=a%2Bb", preparer.prepare(key).uri().toString());
    }

    @Test void formBodyUsesUtf8AndHonorsExplicitContentType() {
        var request = request("POST", "http://localhost", List.of(), List.of(new KeyValue(true, "Content-Type", "custom/type")),
                AuthConfig.none(), RequestDefinition.BodyType.FORM, "", List.of(new KeyValue(true, "a", "ñ = &")), 30);
        var prepared = preparer.prepare(request);
        assertEquals("a=%C3%B1+%3D+%26", prepared.body());
        assertEquals(1, prepared.headers().size()); assertEquals("custom/type", prepared.headers().getFirst().value());
    }

    @Test void rejectsInvalidUrlsAndUnsafeHeaders() {
        for (String url : List.of("", "localhost:8080", "file:///etc/passwd", "https://user:pass@example.com", "https://example.com/a b")) {
            assertThrows(IllegalArgumentException.class, () -> preparer.prepare(request("GET", url, List.of(), List.of(), AuthConfig.none(), RequestDefinition.BodyType.NONE, "", List.of(), 30)));
        }
        for (KeyValue header : List.of(new KeyValue(true, "Host", "evil"), new KeyValue(true, "X-Test", "abc\r\nOther: injected"))) {
            assertThrows(IllegalArgumentException.class, () -> preparer.prepare(request("GET", "https://example.com", List.of(), List.of(header), AuthConfig.none(), RequestDefinition.BodyType.NONE, "", List.of(), 30)));
        }
    }

    @Test void rejectsInvalidTimeoutMethodAndHeadBody() {
        assertThrows(IllegalArgumentException.class, () -> preparer.prepare(request("GET", "https://example.com", List.of(), List.of(), AuthConfig.none(), RequestDefinition.BodyType.NONE, "", List.of(), -1)));
        assertThrows(IllegalArgumentException.class, () -> preparer.prepare(request("BAD METHOD", "https://example.com", List.of(), List.of(), AuthConfig.none(), RequestDefinition.BodyType.NONE, "", List.of(), 30)));
        assertThrows(IllegalArgumentException.class, () -> preparer.prepare(request("HEAD", "https://example.com", List.of(), List.of(), AuthConfig.none(), RequestDefinition.BodyType.TEXT, "x", List.of(), 30)));
    }
}
