package dev.jocote.service;

import dev.jocote.model.KeyValue;
import dev.jocote.model.PreparedRequest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurlGeneratorTest {
    private final PreparedRequest request = new PreparedRequest("POST", URI.create("https://example.com?a=1&b=2"),
            List.of(new KeyValue(true, "X-Test", "O'Reilly")), "{\"text\":\"O'Reilly\"}\n", true, 12);

    @Test void quotesShellMetacharactersForBash() {
        String curl = new CurlGenerator().generate(request, CurlGenerator.Shell.BASH);
        assertTrue(curl.startsWith("curl --request POST \\\n"));
        assertTrue(curl.contains("'https://example.com?a=1&b=2'"));
        assertTrue(curl.contains("O'\"'\"'Reilly"));
        assertTrue(curl.contains("--max-time 12")); assertTrue(curl.contains("--data-raw")); assertTrue(curl.contains("--globoff"));
    }

    @Test void quotesPowerShellAndUsesExecutableInsteadOfAlias() {
        String curl = new CurlGenerator().generate(request, CurlGenerator.Shell.POWERSHELL);
        assertTrue(curl.startsWith("curl.exe --request POST `\n"));
        assertTrue(curl.contains("O''Reilly"));
    }

    @Test void headAndEmptyHeadersHaveCorrectCurlSemantics() {
        var head = new PreparedRequest("HEAD", URI.create("https://example.com"), List.of(new KeyValue(true, "X-Empty", "")), "", false, 30);
        String curl = new CurlGenerator().generate(head, CurlGenerator.Shell.BASH);
        assertTrue(curl.contains("--head")); assertTrue(curl.contains("'X-Empty;'")); assertFalse(curl.contains("--data-raw"));
    }
}
