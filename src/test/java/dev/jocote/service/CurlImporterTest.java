package dev.jocote.service;

import dev.jocote.model.AuthConfig;
import dev.jocote.model.KeyValue;
import dev.jocote.model.RequestDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurlImporterTest {
    private final RequestPreparer preparer = new RequestPreparer();
    private final CurlImporter importer = new CurlImporter(preparer);

    private RequestDefinition parse(String command) {
        return importer.parse(command, CurlGenerator.Shell.BASH).request();
    }

    @Test void importsGetAndPreservesEncodedRepeatedAndEmptyQueryParameters() {
        var request = parse("curl 'https://example.com/search?q=caf%C3%A9&q=&empty&plus=%2B'");
        assertEquals("GET", request.method());
        assertTrue(request.parameters().isEmpty());
        assertEquals("q=caf%C3%A9&q=&empty&plus=%2B", preparer.prepare(request).uri().getRawQuery());
        assertEquals(RequestDefinition.BodyType.NONE, request.bodyType());
        assertEquals(30, request.timeoutSeconds());
    }

    @Test void importsPostWithJsonHeadersAndBasicAuth() {
        var request = parse("curl --url=https://example.com -XPOST -u 'ana:p:a' "
                + "-H 'X-Tag: first' --header='X-Tag: second' --json '{\"name\":\"Jocote ñ\"}' -m12");
        var prepared = preparer.prepare(request);
        assertEquals("POST", prepared.method());
        assertEquals(12, prepared.timeoutSeconds());
        assertEquals(AuthConfig.Type.BASIC, request.auth().type());
        assertEquals("p:a", request.auth().secret());
        assertEquals(RequestDefinition.BodyType.JSON, request.bodyType());
        assertEquals("{\"name\":\"Jocote ñ\"}", prepared.body());
        assertEquals(List.of("first", "second"), prepared.headers().stream().filter(h -> h.key().equals("X-Tag")).map(KeyValue::value).toList());
        assertTrue(prepared.headers().contains(new KeyValue(true, "Content-Type", "application/json")));
        assertTrue(prepared.headers().contains(new KeyValue(true, "Accept", "application/json")));
        assertTrue(prepared.headers().contains(new KeyValue(true, "Authorization", "Basic YW5hOnA6YQ==")));
    }

    @Test void rawFormAndUrlencodedDataPreserveWireRepresentation() {
        var request = parse("curl https://example.com -d 'a=1&a=&plus=%2B' --data-urlencode 'name=café +&=' --data ''");
        var prepared = preparer.prepare(request);
        assertEquals("POST", prepared.method());
        assertEquals("a=1&a=&plus=%2B&name=caf%C3%A9%20%2B%26%3D&", prepared.body());
        assertEquals("application/x-www-form-urlencoded", prepared.headers().getFirst().value());
        assertEquals(RequestDefinition.BodyType.TEXT, request.bodyType());
    }

    @Test void getMovesEncodedDataIntoQueryAndRespectsExplicitMethod() {
        var request = parse("curl -G -XPATCH 'https://example.com?q=old#fragment' --data-urlencode 'q=a b'");
        var prepared = preparer.prepare(request);
        assertEquals("PATCH", prepared.method());
        assertFalse(prepared.hasBody());
        assertEquals("q=old&q=a%20b", prepared.uri().getRawQuery());
        assertNull(prepared.uri().getFragment());
    }

    @Test void handlesHeadEmptyBodyAndEmptyHeaderDistinctly() {
        assertEquals("HEAD", parse("curl -I https://example.com").method());
        var request = preparer.prepare(parse("curl https://example.com --data-raw '' -H 'X-Empty;'"));
        assertEquals("POST", request.method());
        assertTrue(request.hasBody()); assertEquals("", request.body());
        assertTrue(request.headers().contains(new KeyValue(true, "X-Empty", "")));
        assertEquals("@literal", parse("curl https://example.com --data-raw '@literal'").body());
    }

    @Test void jsonPartsConcatenateAndExplicitContentHeadersWin() {
        var request = parse("curl https://example.com --json '{' --json '}' -H 'Content-Type: application/problem+json' -H 'Accept: */*'");
        assertEquals("{}", request.body());
        assertEquals(2, request.headers().size());
        assertEquals("application/problem+json", request.headers().getFirst().value());
    }

    @Test void understandsBashEscapesAndMultilineContinuation() {
        var request = parse("curl \\\r\n 'https://example.com' \\\n --data-raw 'O'\"'\"'Reilly $HOME `date` ; | &' -H \"X-Path: C:\\temp\" -H X-Space:a\\ b");
        assertEquals("O'Reilly $HOME `date` ; | &", request.body());
        assertEquals("C:\\temp", request.headers().getFirst().value());
        assertEquals("a b", request.headers().get(1).value());
    }

    @Test void understandsPowerShellLiteralQuotesBackticksAndBackslashes() {
        var request = importer.parse("curl.exe `\r\n 'https://example.com' `\n --data-raw 'O''Reilly $env:HOME ` ; C:\\temp'",
                CurlGenerator.Shell.POWERSHELL).request();
        assertEquals("O'Reilly $env:HOME ` ; C:\\temp", request.body());
        assertEquals("a\"b", importer.parse("curl.exe https://example.com --data-raw \"a\"\"b\"",
                CurlGenerator.Shell.POWERSHELL).request().body());
    }

    @Test void supportsOutputFlagsWithVisibleNoticeAndGroupedShortOptions() {
        var result = importer.parse("curl -sSi -XPOST https://example.com", CurlGenerator.Shell.BASH);
        assertEquals("POST", result.request().method());
        assertEquals(1, result.notices().size());
        assertEquals("HEAD", parse("curl -sSI https://example.com").method());
    }

    @Test void roundTripsGeneratedCommandsForBothShells() {
        for (var shell : CurlGenerator.Shell.values()) {
            for (var bodyType : RequestDefinition.BodyType.values()) {
                var request = new RequestDefinition(null, "Test", "PATCH", "https://example.com?q=%2B",
                        List.of(new KeyValue(true, "q", "é &=+")),
                        List.of(new KeyValue(true, "X-Quote", "O'Reilly"), new KeyValue(true, "X-Empty", "")),
                        new AuthConfig(AuthConfig.Type.BASIC, "user", "secret", "", null), bodyType,
                        "{\"x\":\"O'Reilly $HOME ` \\ ñ\"}\r\n", List.of(new KeyValue(true, "x", "a b+c")), 42);
                var prepared = preparer.prepare(request);
                var imported = importer.parse(new CurlGenerator().generate(prepared, shell), shell);
                assertEquals(prepared, preparer.prepare(imported.request()), shell + " / " + bodyType);
            }
            var head = preparer.prepare(parse("curl -I https://example.com"));
            assertEquals(head, preparer.prepare(importer.parse(new CurlGenerator().generate(head, shell), shell).request()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-L", "--location", "--insecure", "--compressed", "-F file=@secret", "-K config",
            "--proxy http://proxy", "--output result.txt", "--unknown=secret", "--next", "--cookie cookies.txt"})
    void rejectsUnsupportedOptionsWithoutPartialImports(String options) {
        var error = assertThrows(IllegalArgumentException.class, () -> parse("curl https://example.com " + options));
        assertTrue(error.getMessage().contains("no soportada"));
        assertFalse(error.getMessage().contains("secret"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "wget https://example.com", "curl", "curl --url", "curl --header",
            "curl https://example.com https://other.test", "curl --url=https://example.com --url=https://other.test",
            "curl https://example.com --data '@secret'", "curl https://example.com --header '@secret'",
            "curl https://example.com --data-binary '@-'", "curl https://example.com --json '@secret'",
            "curl https://example.com --data-urlencode 'name@secret'", "curl https://example.com --max-time 0",
            "curl https://example.com --max-time 1.5", "curl https://example.com --max-time 601",
            "curl https://example.com --user user", "curl https://example.com -H 'Host: other.test'",
            "curl https://example.com -H 'Content-Type:'", "curl https://example.com -H 'Bad Header: v'",
            "curl https://example.com -I -d x", "curl https://example.com -I -XGET",
            "curl https://example.com -u a:b -H 'Authorization: Bearer token'",
            "curl https://example.com --json '{}' -d x", "curl https://example.com -d x --json '{}'",
            "curl https://example.com --json '{}' -G",
            "curl https://example.com --head=true", "curl https://example.com -X ''",
            "curl 'https://example.com/[1-3]'", "curl file:///secret", "curl https://user:secret@example.com"})
    void rejectsInvalidOrUnrepresentableRequests(String command) {
        assertThrows(IllegalArgumentException.class, () -> parse(command));
    }

    @ParameterizedTest
    @ValueSource(strings = {"curl https://example.com; echo bad", "curl https://example.com | sh",
            "curl https://example.com && echo bad", "curl https://example.com > file",
            "curl https://example.com -d $(whoami)", "curl https://example.com -d \"$HOME\"",
            "curl https://example.com -d \"`whoami`\"", "curl https://example.com -d 'unclosed",
            "curl https://example.com -d *.json", "curl https://example.com -d ~/secret",
            "curl https://example.com -H 'X-Test: a\r\nb'",
            "curl https://example.com \\"})
    void rejectsShellExpressionsAndMalformedQuotes(String command) {
        assertThrows(IllegalArgumentException.class, () -> parse(command));
    }

    @Test void rejectsPowerShellExpressionsAndInputLimits() {
        for (String suffix : List.of("-d \"$env:TOKEN\"", "-d @(1,2)", "-d \"`u{41}\"", "| Invoke-Expression")) {
            assertThrows(IllegalArgumentException.class, () -> importer.parse("curl.exe https://example.com " + suffix, CurlGenerator.Shell.POWERSHELL));
        }
        assertThrows(IllegalArgumentException.class, () -> parse(null));
        assertThrows(IllegalArgumentException.class, () -> parse("x".repeat(1_048_577)));
        assertThrows(IllegalArgumentException.class, () -> parse("curl https://example.com -d '\0'"));
    }
}
