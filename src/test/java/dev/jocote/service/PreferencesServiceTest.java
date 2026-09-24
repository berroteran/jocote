package dev.jocote.service;

import dev.jocote.model.AppTheme;
import dev.jocote.model.LayoutDensity;
import dev.jocote.model.UserPreferences;
import dev.jocote.repository.PreferencesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PreferencesServiceTest {
    @TempDir Path directory;

    @Test void firstRunUsesDefaultsWithoutWritingFiles() throws Exception {
        Path path = directory.resolve("preferences.json");
        try (var service = new PreferencesService(new PreferencesRepository(path))) {
            assertEquals(UserPreferences.defaults(), service.current());
            assertEquals("UL", service.current().initials());
            assertFalse(Files.exists(path));
        }
    }

    @Test void serializesChangesAndRestoresProfileAndThemeWithoutTouchingCollections() throws Exception {
        Path path = directory.resolve("preferences.json");
        Path workspace = directory.resolve("workspace.json");
        Files.writeString(workspace, "existing collection data");
        var repository = new PreferencesRepository(path);
        try (var service = new PreferencesService(repository)) {
            var profile = service.setProfile(" Ana Pérez ", "ana", "ana@example.test");
            var theme = service.setTheme(AppTheme.CASPIAN);
            profile.get(5, TimeUnit.SECONDS); theme.get(5, TimeUnit.SECONDS);
            assertEquals(new UserPreferences(1, AppTheme.CASPIAN, "Ana Pérez", "ana", "ana@example.test"), repository.load());
            assertEquals("AP", service.current().initials());
        }
        try (var restored = new PreferencesService(repository)) { assertEquals(repository.load(), restored.current()); }
        assertEquals("existing collection data", Files.readString(workspace));
        try (var files = Files.list(directory)) { assertEquals(2, files.count()); }
    }

    @Test void corruptOrUnsupportedSettingsArePreservedAndNotOverwritten() throws Exception {
        Path path = directory.resolve("preferences.json");
        for (String content : new String[]{"not-json", "null", "{\"version\":2}", "{\"version\":1,\"theme\":\"UNKNOWN\"}"}) {
            Files.writeString(path, content);
            try (var service = new PreferencesService(new PreferencesRepository(path))) {
                assertEquals(UserPreferences.defaults(), service.current());
                assertFalse(service.loadWarning().isEmpty());
                assertThrows(ExecutionException.class, () -> service.setTheme(AppTheme.DARK).get(5, TimeUnit.SECONDS));
            }
            assertEquals(content, Files.readString(path));
        }
    }

    @Test void loadsOlderPreferencesAndAddsDensityWithoutLosingProfile() throws Exception {
        Path path = directory.resolve("preferences.json");
        Files.writeString(path, "{\"version\":1,\"theme\":\"DARK\",\"displayName\":\"Ana\",\"alias\":\"a\",\"email\":\"\"}");
        var repository = new PreferencesRepository(path);
        try (var service = new PreferencesService(repository)) {
            assertEquals(LayoutDensity.NORMAL, service.current().density());
            service.setDensity(LayoutDensity.COMPACT).get(5, TimeUnit.SECONDS);
            service.setTheme(AppTheme.CASPIAN).get(5, TimeUnit.SECONDS);
            service.setProfile("Ana Pérez", "ana", "ana@example.test").get(5, TimeUnit.SECONDS);
        }
        try (var restored = new PreferencesService(repository)) {
            assertEquals(new UserPreferences(1, AppTheme.CASPIAN, "Ana Pérez", "ana", "ana@example.test", LayoutDensity.COMPACT), restored.current());
        }
    }

    @Test void failedWriteDoesNotPublishUnsavedPreferences() throws Exception {
        Path path = directory.resolve("preferences.json");
        try (var service = new PreferencesService(new PreferencesRepository(path))) {
            Files.createDirectory(path); Files.writeString(path.resolve("keep.txt"), "keep");
            assertThrows(ExecutionException.class, () -> service.setTheme(AppTheme.DARK).get(5, TimeUnit.SECONDS));
            assertEquals(UserPreferences.defaults(), service.current());
            assertEquals("keep", Files.readString(path.resolve("keep.txt")));
        }
    }

    @Test void validatesProfileAndCyclesAllThemes() {
        assertThrows(IllegalArgumentException.class, () -> UserPreferences.defaults().withProfile("x".repeat(81), "", ""));
        assertThrows(IllegalArgumentException.class, () -> UserPreferences.defaults().withProfile("a\nb", "", ""));
        assertThrows(IllegalArgumentException.class, () -> UserPreferences.defaults().withProfile("Ana", "", "invalid"));
        assertEquals("Usuario local", UserPreferences.defaults().withProfile(" ", "", "").displayName());
        assertEquals("É", UserPreferences.defaults().withProfile("Érika", "", "").initials());
        assertEquals(AppTheme.DARK, AppTheme.LIGHT.next());
        assertEquals(AppTheme.MODENA, AppTheme.DARK.next());
        assertEquals(AppTheme.CASPIAN, AppTheme.MODENA.next());
        assertEquals(AppTheme.LIGHT, AppTheme.CASPIAN.next());
    }
}
