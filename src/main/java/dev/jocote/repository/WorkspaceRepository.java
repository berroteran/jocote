package dev.jocote.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jocote.model.Workspace;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public final class WorkspaceRepository {
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public WorkspaceRepository(Path file) { this.file = file.toAbsolutePath(); }
    public Path path() { return file; }

    public Workspace load() throws IOException {
        if (!Files.exists(file)) return Workspace.empty();
        Workspace workspace = mapper.readValue(file.toFile(), Workspace.class);
        validate(workspace);
        return workspace;
    }

    public void save(Workspace workspace) throws IOException {
        validate(workspace);
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "jocote-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), workspace);
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }

    private static void validate(Workspace workspace) throws IOException {
        if (workspace == null || workspace.version() != 1) throw new IOException("Formato de workspace no compatible (se requiere versión 1).");
        Set<String> ids = new HashSet<>();
        for (var collection : workspace.collections()) {
            if (collection == null || !ids.add(collection.id())) throw new IOException("Colección inválida o ID duplicado.");
            for (var request : collection.requests()) {
                if (request == null || !ids.add(request.id())) throw new IOException("Petición inválida o ID duplicado.");
            }
        }
    }
}
