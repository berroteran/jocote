package dev.jocote.model;

import java.util.List;
import java.util.UUID;

public record RequestCollection(String id, String name, List<RequestDefinition> requests) {
    public RequestCollection {
        id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        name = name == null || name.isBlank() ? "Mi colección" : name;
        requests = requests == null ? List.of() : List.copyOf(requests);
    }
}
