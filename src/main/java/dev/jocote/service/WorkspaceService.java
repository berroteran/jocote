package dev.jocote.service;

import dev.jocote.model.RequestCollection;
import dev.jocote.model.RequestDefinition;
import dev.jocote.model.Workspace;
import dev.jocote.repository.WorkspaceRepository;

import java.io.IOException;
import java.util.ArrayList;

/** Mutations are published in memory only after an atomic disk save succeeds. */
public final class WorkspaceService {
    private final WorkspaceRepository repository;
    private Workspace workspace;

    public WorkspaceService(WorkspaceRepository repository) throws IOException {
        this.repository = repository;
        workspace = repository.load();
    }

    public Workspace workspace() { return workspace; }
    public String storagePath() { return repository.path().toString(); }

    public void addCollection(String name) throws IOException {
        var collections = new ArrayList<>(workspace.collections());
        collections.add(new RequestCollection(null, name, java.util.List.of()));
        commit(new Workspace(1, collections));
    }

    public void renameCollection(String id, String name) throws IOException {
        commit(new Workspace(1, workspace.collections().stream().map(c -> c.id().equals(id)
                ? new RequestCollection(c.id(), name, c.requests()) : c).toList()));
    }

    public void deleteCollection(String id) throws IOException {
        commit(new Workspace(1, workspace.collections().stream().filter(c -> !c.id().equals(id)).toList()));
    }

    public void saveRequest(String collectionId, RequestDefinition request) throws IOException {
        if (workspace.collections().stream().noneMatch(c -> c.id().equals(collectionId))) {
            throw new IOException("La colección seleccionada ya no existe.");
        }
        var collections = new ArrayList<RequestCollection>();
        for (var collection : workspace.collections()) {
            var requests = new ArrayList<>(collection.requests().stream().filter(r -> !r.id().equals(request.id())).toList());
            if (collection.id().equals(collectionId)) requests.add(request);
            collections.add(new RequestCollection(collection.id(), collection.name(), requests));
        }
        commit(new Workspace(1, collections));
    }

    public void deleteRequest(String id) throws IOException {
        commit(new Workspace(1, workspace.collections().stream().map(c -> new RequestCollection(c.id(), c.name(),
                c.requests().stream().filter(r -> !r.id().equals(id)).toList())).toList()));
    }

    private void commit(Workspace updated) throws IOException {
        repository.save(updated);
        workspace = updated;
    }
}
