package dev.jocote;

import dev.jocote.repository.WorkspaceRepository;
import dev.jocote.service.HttpRequestService;
import dev.jocote.service.WorkspaceService;
import dev.jocote.ui.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

public final class JocoteApplication extends Application {
    private static final Logger LOG = Logger.getLogger(JocoteApplication.class.getName());
    private HttpRequestService http;
    private MainView view;

    @Override public void start(Stage stage) {
        Path path = Path.of(System.getProperty("jocote.home", Path.of(System.getProperty("user.home"), ".jocote").toString()), "workspace.json");
        try {
            var workspace = new WorkspaceService(new WorkspaceRepository(path));
            http = new HttpRequestService();
            view = new MainView(workspace, http);
            var scene = new Scene(view, 1440, 900);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/dev/jocote/jocote.css")).toExternalForm());
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
    }

    public static void main(String[] args) { launch(args); }
}
