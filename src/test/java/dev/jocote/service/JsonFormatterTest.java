package dev.jocote.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonFormatterTest {
    @Test void formatsValidJsonWithoutDroppingContent() {
        String formatted = JsonFormatter.format("{\"name\":\"Jocote\",\"ok\":true}");
        assertTrue(formatted.contains("\n"));
        assertTrue(formatted.contains("\"Jocote\""));
        assertTrue(formatted.contains("true"));
    }

    @Test void preservesInvalidJsonAndTrailingContentExactly() {
        for (String text : new String[]{"plain text", "123 extra", "{\"a\":1}{\"b\":2}", "", "{invalid"}) {
            assertEquals(text, JsonFormatter.format(text));
        }
    }
}
