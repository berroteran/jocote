package dev.jocote.model;

import java.net.URI;
import java.util.List;

public record PreparedRequest(String method, URI uri, List<KeyValue> headers, String body, boolean hasBody, int timeoutSeconds) {
    public PreparedRequest {
        headers = List.copyOf(headers);
    }
}
