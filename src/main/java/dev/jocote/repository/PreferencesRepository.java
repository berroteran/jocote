package dev.jocote.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jocote.model.UserPreferences;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class PreferencesRepository {
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public PreferencesRepository(Path file) { this.file = file.toAbsolutePath(); }

    public UserPreferences load() throws IOException {
        if (Files.notExists(file)) return UserPreferences.defaults();
        if (Files.size(file) > 64 * 1024) throw new IOException("El archivo de preferencias supera 64 KiB.");
        var preferences = mapper.readValue(file.toFile(), UserPreferences.class);
        if (preferences == null) throw new IOException("El archivo de preferencias está vacío.");
        return preferences;
    }

    public void save(UserPreferences preferences) throws IOException {
        Objects.requireNonNull(preferences);
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "jocote-preferences-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), preferences);
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
