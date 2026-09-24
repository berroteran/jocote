package dev.jocote.repository;

import dev.jocote.model.RequestCollection;
import dev.jocote.model.RequestDefinition;
import dev.jocote.model.Workspace;
import dev.jocote.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceRepositoryTest {
    @TempDir Path directory;

    @Test void savesAndReloadsImmutableWorkspace() throws Exception {
        var repository = new WorkspaceRepository(directory.resolve("data/workspace.json"));
        assertEquals(1, repository.load().collections().size());
        var request = RequestDefinition.blank().withName("Prueba ñ");
        var workspace = new Workspace(1, List.of(new RequestCollection(null, "REST", List.of(request))));
        repository.save(workspace); assertEquals(workspace, repository.load());
        try (var files = Files.list(directory.resolve("data"))) { assertEquals(1, files.count()); }
    }

    @Test void corruptOrFutureWorkspaceIsPreserved() throws Exception {
        Path file = directory.resolve("workspace.json");
        for (String content : List.of("{broken", "{\"version\":99,\"collections\":[]}")) {
            Files.writeString(file, content);
            assertThrows(IOException.class, () -> new WorkspaceRepository(file).load());
            assertEquals(content, Files.readString(file));
        }
    }

    @Test void updatesAndMovesRequestsWithoutDuplicateIds() throws Exception {
        var repository = new WorkspaceRepository(directory.resolve("workspace.json"));
        var service = new WorkspaceService(repository);
        var first = service.workspace().collections().getFirst().id(); service.addCollection("Second");
        var second = service.workspace().collections().getLast().id();
        var request = RequestDefinition.blank(); service.saveRequest(first, request);
        service.saveRequest(second, request.withName("Updated"));
        assertTrue(service.workspace().collections().getFirst().requests().isEmpty());
        assertEquals("Updated", repository.load().collections().getLast().requests().getFirst().name());
        service.deleteRequest(request.id()); assertTrue(repository.load().collections().getLast().requests().isEmpty());
    }

    @Test void failedSaveDoesNotPublishUnsavedChanges() throws Exception {
        Path file = directory.resolve("workspace.json");
        var service = new WorkspaceService(new WorkspaceRepository(file));
        Files.createDirectory(file); Files.writeString(file.resolve("occupied"), "x");
        Workspace before = service.workspace();
        assertThrows(IOException.class, () -> service.addCollection("Will fail")); assertEquals(before, service.workspace());
    }
}
