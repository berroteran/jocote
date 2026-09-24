package dev.jocote.service;

import dev.jocote.model.AppTheme;
import dev.jocote.model.LayoutDensity;
import dev.jocote.model.UserPreferences;
import dev.jocote.repository.PreferencesRepository;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

/** Serializes small settings writes off the UI thread; publishes only successful saves. */
public final class PreferencesService implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(PreferencesService.class.getName());
    private final PreferencesRepository repository;
    private final ExecutorService writer = Executors.newSingleThreadExecutor(Thread.ofPlatform().name("jocote-preferences").factory());
    private volatile UserPreferences current;
    private final String loadWarning;

    public PreferencesService(PreferencesRepository repository) {
        this.repository = repository;
        String warning = "";
        try { current = repository.load(); }
        catch (IOException e) {
            current = UserPreferences.defaults();
            warning = "No se pudieron leer las preferencias. Se usan valores temporales; el archivo se conserva sin modificar. Revisa preferences.json antes de volver a guardar.";
            LOG.warning("No se pudieron cargar las preferencias: " + e.getClass().getSimpleName());
        }
        loadWarning = warning;
    }

    public UserPreferences current() { return current; }
    public String loadWarning() { return loadWarning; }

    public CompletableFuture<UserPreferences> setTheme(AppTheme theme) { return update(value -> value.withTheme(theme)); }

    public CompletableFuture<UserPreferences> setDensity(LayoutDensity density) { return update(value -> value.withDensity(density)); }

    public CompletableFuture<UserPreferences> setProfile(String name, String alias, String email) {
        return update(value -> value.withProfile(name, alias, email));
    }

    private CompletableFuture<UserPreferences> update(UnaryOperator<UserPreferences> change) {
        if (!loadWarning.isEmpty()) return CompletableFuture.failedFuture(new IOException(loadWarning));
        return CompletableFuture.supplyAsync(() -> {
            var next = change.apply(current);
            try { repository.save(next); }
            catch (IOException e) {
                LOG.warning("No se pudieron guardar las preferencias: " + e.getClass().getSimpleName());
                throw new CompletionException(e);
            }
            current = next;
            return next;
        }, writer);
    }

    @Override public void close() { writer.shutdown(); }
}
