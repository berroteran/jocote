package dev.jocote.model;

import java.util.List;

public record Workspace(int version, List<RequestCollection> collections) {
    public Workspace {
        collections = collections == null ? List.of() : List.copyOf(collections);
    }

    public static Workspace empty() {
        return new Workspace(1, List.of(new RequestCollection(null, "Mi colección", List.of())));
    }
}
