package dev.jocote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

public final class JsonFormatter {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private JsonFormatter() { }

    public static String format(String value) {
        try {
            var tree = MAPPER.readTree(value);
            return tree == null || tree.isMissingNode() ? value : MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
        } catch (JsonProcessingException e) { return value; }
    }
}
