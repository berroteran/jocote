package dev.jocote;

import dev.jocote.repository.WorkspaceRepository;
import dev.jocote.repository.PreferencesRepository;
import dev.jocote.service.HttpRequestService;
import dev.jocote.service.WorkspaceService;
import dev.jocote.service.PreferencesService;
import dev.jocote.ui.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class JocoteApplication extends Application {
    private static final Logger LOG = Logger.getLogger(JocoteApplication.class.getName());
    private HttpRequestService http;
    private MainView view;
    private PreferencesService preferences;

    @Override public void start(Stage stage) {
        Path path = Path.of(System.getProperty("jocote.home", Path.of(System.getProperty("user.home"), ".jocote").toString()), "workspace.json");
        try {
            var workspace = new WorkspaceService(new WorkspaceRepository(path));
            http = new HttpRequestService();
            preferences = new PreferencesService(new PreferencesRepository(path.resolveSibling("preferences.json")));
            view = new MainView(workspace, http, preferences);
            var scene = new Scene(view, 1440, 900);
            stage.setTitle("Jocote · REST Client"); stage.setMinWidth(1060); stage.setMinHeight(700); stage.setScene(scene);
            stage.setOnCloseRequest(event -> { if (!view.requestClose()) event.consume(); });
            stage.show();
            if (getParameters().getRaw().contains("--demo")) view.runGitHubDemo();
            LOG.info("Jocote iniciado");
        } catch (IOException | RuntimeException e) {
            LOG.severe("No se pudo iniciar Jocote: " + e.getClass().getSimpleName());
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Jocote"); alert.setHeaderText("No se pudo abrir el workspace");
            alert.setContentText("Revisa el archivo y los permisos: " + path + "\n\nEl archivo existente no se ha modificado.\n" + e.getClass().getSimpleName());
            alert.showAndWait(); Platform.exit();
        }
    }

    @Override public void stop() {
        if (view != null) view.dispose();
        if (http != null) http.close();
        if (preferences != null) preferences.close();
    }

    public static void main(String[] args) { launch(args); }
}
