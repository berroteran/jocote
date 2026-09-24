package dev.jocote.ui;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jocote.model.AuthConfig;
import dev.jocote.model.AppTheme;
import dev.jocote.model.LayoutDensity;
import dev.jocote.model.KeyValue;
import dev.jocote.model.RequestDefinition;
import dev.jocote.repository.WorkspaceRepository;
import dev.jocote.repository.PreferencesRepository;
import dev.jocote.service.PreferencesService;
import dev.jocote.service.HttpRequestService;
import dev.jocote.service.CurlGenerator;
import dev.jocote.service.WorkspaceService;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.application.Application;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.PopupWindow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "jocote.uiTest", matches = "true")
class JavaFxSmokeTest {
    @TempDir Path directory;

    @Test void rendersDesktopAndExecutesRequestFromJavaFxControls() throws Exception {
        var started = new CompletableFuture<Void>();
        Platform.startup(() -> { Platform.setImplicitExit(false); started.complete(null); });
        started.get(10, TimeUnit.SECONDS);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var importedCalls = new AtomicInteger();
        server.createContext("/curl", exchange -> {
            importedCalls.incrementAndGet();
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.createContext("/v1/health", exchange -> {
            byte[] bytes = "{\"service\":\"Jocote\",\"status\":\"ok\",\"platforms\":[\"Windows\",\"macOS\",\"Linux\"],\"message\":\"Tu cliente REST está listo.\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        var http = new HttpRequestService();
        var settingsFile = directory.resolve("preferences.json");
        var preferences = new PreferencesService(new PreferencesRepository(settingsFile));
        var stage = new Stage[1]; var view = new MainView[1];
        try {
            var service = new WorkspaceService(new WorkspaceRepository(directory.resolve("workspace.json")));
            String collectionId = service.workspace().collections().getFirst().id();
            service.renameCollection(collectionId, "Jocote · API local");
            var request = new RequestDefinition(null, "Estado del servicio", "GET", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/health",
                    List.of(new KeyValue(true, "environment", "local")), List.of(new KeyValue(true, "Accept", "application/json")),
                    AuthConfig.none(), RequestDefinition.BodyType.NONE, "", List.of(), 30);
            service.saveRequest(collectionId, request);
            service.saveRequest(collectionId, new RequestDefinition(null, "Crear recurso", "POST", "http://localhost:8080/v1/resources", List.of(), List.of(), AuthConfig.none(), RequestDefinition.BodyType.JSON, "{\"name\":\"Jocote\"}", List.of(), 30));
            onFx(() -> {
                view[0] = new MainView(service, http, preferences); stage[0] = new Stage();
                var scene = new Scene(view[0], 1440, 900);
                stage[0].setScene(scene); stage[0].setTitle("Jocote · Verificación de interfaz"); stage[0].show();
                view[0].open(request, collectionId); view[0].applyCss(); view[0].layout();
                var tabs = (TabPane) view[0].lookup("#request-tabs");
                assertEquals(2, tabs.getTabs().size());
                var editor = (RequestEditor) tabs.getSelectionModel().getSelectedItem().getContent();
                assertEquals(request, editor.definition());
                assertTrue(((TextArea) view[0].lookup("#curl-code")).getText().contains("environment=local"));
                for (var node : view[0].lookupAll(".toggle-button")) {
                    var toggle = (ToggleButton) node; toggle.fire(); assertFalse(toggle.isSelected()); toggle.fire(); assertTrue(toggle.isSelected());
                }
                ((Button) editor.lookup("#send-request")).fire();
            });
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            boolean[] received = {false};
            while (!received[0] && System.nanoTime() < deadline) {
                onFx(() -> received[0] = view[0].lookupAll(".status-badge").stream().anyMatch(n -> n instanceof Label label && label.getText().equals("200 OK")));
                if (!received[0]) Thread.sleep(50);
            }
            assertTrue(received[0], "The JavaFX request must render a successful HTTP response");
            onFx(() -> {
                view[0].applyCss(); view[0].layout();
                WritableImage image = view[0].snapshot(null, null);
                var buffered = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < buffered.getHeight(); y++) for (int x = 0; x < buffered.getWidth(); x++) buffered.setRGB(x, y, image.getPixelReader().getArgb(x, y));
                try { Files.createDirectories(Path.of("target")); ImageIO.write(buffered, "png", Path.of("target/jocote-preview.png").toFile()); }
                catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
            });
            verifyCurlImport(view[0], "http://127.0.0.1:" + server.getAddress().getPort() + "/curl", importedCalls);
            verifyThemesAndProfile(view[0], preferences, stage[0]);
            verifyDensity(view[0], preferences, stage[0]);
            assertEquals(preferences.current(), new PreferencesRepository(settingsFile).load());
            if (Boolean.getBoolean("jocote.githubTest")) verifyGitHubDemo(view[0], service);
        } finally {
            onFx(() -> { if (view[0] != null) view[0].dispose(); if (stage[0] != null) stage[0].close(); });
            preferences.close(); http.close(); server.stop(0); Platform.exit();
        }
    }

    private static void verifyThemesAndProfile(MainView view, PreferencesService preferences, Stage stage) throws Exception {
        onFx(() -> {
            var header = (HBox) view.getTop();
            int index = header.getChildren().indexOf(view.lookup("#new-request"));
            assertEquals("cycle-theme", header.getChildren().get(index + 1).getId());
            assertEquals("profile-menu", header.getChildren().getLast().getId());
            stage.setWidth(1060);
            view.applyCss(); view.layout();
            var last = view.lookup("#profile-menu");
            assertTrue(last.getBoundsInParent().getMaxX() <= header.getWidth(), "Profile must fit the minimum window width");
            stage.setWidth(1456);
        });
        for (var theme : List.of(AppTheme.DARK, AppTheme.MODENA, AppTheme.CASPIAN, AppTheme.LIGHT)) {
            onFx(() -> ((Button) view.lookup("#cycle-theme")).fire());
            waitForPreferences(view);
            onFx(() -> {
                assertEquals(theme, preferences.current().theme());
                assertEquals(theme == AppTheme.CASPIAN ? Application.STYLESHEET_CASPIAN : Application.STYLESHEET_MODENA,
                        Application.getUserAgentStylesheet());
                assertEquals(theme == AppTheme.DARK, view.getStyleClass().contains("theme-dark"));
                assertEquals(theme == AppTheme.LIGHT || theme == AppTheme.DARK,
                        view.getScene().getStylesheets().stream().anyMatch(path -> path.endsWith("/jocote.css")));
                view.applyCss(); view.layout();
                snapshot(view, "jocote-theme-" + theme.name().toLowerCase(java.util.Locale.ROOT));
            });
        }
        onFx(() -> {
            var profile = (MenuButton) view.lookup("#profile-menu");
            profile.show(); assertTrue(profile.isShowing()); profile.hide();
            var appearance = (Menu) profile.getItems().getLast();
            appearance.getItems().stream().filter(item -> item.getUserData() == AppTheme.DARK).findFirst().orElseThrow().fire();
        });
        waitForPreferences(view);
        onFx(() -> {
            var profile = (MenuButton) view.lookup("#profile-menu");
            Platform.runLater(() -> profile.getItems().stream().filter(item -> "edit-profile".equals(item.getId())).findFirst().orElseThrow().fire());
        });
        onFx(() -> {
            var pane = profilePane();
            assertTrue(pane.getStyleClass().contains("theme-dark"));
            ((TextField) pane.lookup("#profile-name")).setText("Ana Pérez");
            ((TextField) pane.lookup("#profile-email")).setText("invalid");
            ((Button) pane.lookup("#save-profile")).fire();
            assertFalse(((Label) pane.lookup("#profile-error")).getText().isEmpty());
            assertEquals("Usuario local", preferences.current().displayName());
            ((TextField) pane.lookup("#profile-email")).setText("ana@example.test");
            ((TextField) pane.lookup("#profile-alias")).setText("ana");
            snapshot(pane, "jocote-profile-dialog");
            ((Button) pane.lookup("#save-profile")).fire();
        });
        waitForPreferences(view);
        onFx(() -> {
            assertEquals("Ana Pérez", preferences.current().displayName());
            assertEquals("AP", ((Label) view.lookup("#profile-avatar")).getText());
            assertEquals(AppTheme.DARK, preferences.current().theme());
            var appearance = (Menu) ((MenuButton) view.lookup("#profile-menu")).getItems().getLast();
            assertTrue(((RadioMenuItem) appearance.getItems().get(1)).isSelected());
            snapshot(view, "jocote-profile-preview");
            var profile = (MenuButton) view.lookup("#profile-menu");
            profile.show();
            Window.getWindows().stream().filter(window -> window instanceof PopupWindow && window.isShowing())
                    .filter(window -> window.getScene().getRoot().lookup(".context-menu") != null)
                    .findFirst().ifPresent(window -> snapshot(window.getScene().getRoot(), "jocote-profile-menu"));
            profile.hide();
            Platform.runLater(() -> profile.getItems().stream().filter(item -> "edit-profile".equals(item.getId())).findFirst().orElseThrow().fire());
        });
        onFx(() -> {
            var pane = profilePane();
            ((TextField) pane.lookup("#profile-name")).setText("Discarded");
            ((Button) pane.lookupButton(ButtonType.CANCEL)).fire();
        });
        assertEquals("Ana Pérez", preferences.current().displayName());
    }

    private static void waitForPreferences(MainView view) throws Exception {
        boolean[] complete = {false};
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!complete[0] && System.nanoTime() < deadline) {
            onFx(() -> complete[0] = !view.lookup("#cycle-theme").isDisabled());
            if (!complete[0]) Thread.sleep(20);
        }
        assertTrue(complete[0], "Preferences save must finish without blocking JavaFX");
    }

    private static void verifyDensity(MainView view, PreferencesService preferences, Stage stage) throws Exception {
        var editor = new RequestEditor[1];
        var definition = new RequestDefinition[1];
        var response = new String[1];
        double[] fontSize = {0};
        onFx(() -> {
            editor[0] = (RequestEditor) ((TabPane) view.lookup("#request-tabs")).getSelectionModel().getSelectedItem().getContent();
            definition[0] = editor[0].definition();
            response[0] = ((TextArea) editor[0].lookup("#response-body")).getText();
        });
        for (AppTheme theme : AppTheme.values()) {
            onFx(() -> {
                var appearance = (Menu) ((MenuButton) view.lookup("#profile-menu")).getItems().getLast();
                appearance.getItems().stream().filter(item -> item.getUserData() == theme).findFirst().orElseThrow().fire();
            });
            waitForPreferences(view);
            double[] heights = new double[3];
            for (LayoutDensity density : List.of(LayoutDensity.NORMAL, LayoutDensity.COMPACT, LayoutDensity.COMFORTABLE)) {
                onFx(() -> densityMenu(view).getItems().stream().filter(item -> item.getUserData() == density).findFirst().orElseThrow().fire());
                waitForPreferences(view);
                onFx(() -> {
                    view.applyCss(); view.layout();
                    assertEquals(density, preferences.current().density());
                    assertEquals(theme, preferences.current().theme());
                    assertSame(editor[0], ((TabPane) view.lookup("#request-tabs")).getSelectionModel().getSelectedItem().getContent());
                    assertEquals(definition[0], editor[0].definition());
                    assertEquals(response[0], ((TextArea) editor[0].lookup("#response-body")).getText());
                    var send = (Button) editor[0].lookup("#send-request");
                    if (density == LayoutDensity.NORMAL) fontSize[0] = send.getFont().getSize();
                    else assertEquals(fontSize[0], send.getFont().getSize(), "Density must not change font size");
                    assertEquals(density == LayoutDensity.COMPACT ? 8 : density == LayoutDensity.NORMAL ? 16 : 20, editor[0].getPadding().getTop());
                    heights[density.ordinal()] = view.getTop().getBoundsInParent().getHeight();
                    stage.setWidth(1060); view.applyCss(); view.layout();
                    assertTrue(view.lookup("#profile-menu").getBoundsInParent().getMaxX() <= view.getTop().getBoundsInParent().getWidth(), "Header must fit at minimum width for " + theme + "/" + density);
                    stage.setWidth(1456); view.applyCss(); view.layout();
                    if (theme == AppTheme.DARK) snapshot(view, "jocote-density-" + density.name().toLowerCase(java.util.Locale.ROOT));
                });
            }
            assertTrue(heights[LayoutDensity.COMPACT.ordinal()] < heights[LayoutDensity.NORMAL.ordinal()]);
            assertTrue(heights[LayoutDensity.NORMAL.ordinal()] < heights[LayoutDensity.COMFORTABLE.ordinal()]);
        }
        onFx(() -> densityMenu(view).getItems().getFirst().fire());
        waitForPreferences(view);
        onFx(() -> {
            view.open(RequestDefinition.blank().withName("Densidad heredada"), null);
            view.applyCss(); view.layout();
            var newEditor = (RequestEditor) ((TabPane) view.lookup("#request-tabs")).getSelectionModel().getSelectedItem().getContent();
            assertEquals(8, newEditor.getPadding().getTop(), "New tabs inherit the selected density");
            var profile = (MenuButton) view.lookup("#profile-menu");
            Platform.runLater(() -> profile.getItems().stream().filter(item -> "edit-profile".equals(item.getId())).findFirst().orElseThrow().fire());
        });
        onFx(() -> {
            var pane = profilePane(); pane.applyCss(); pane.layout();
            assertTrue(pane.getStyleClass().contains("density-compact"));
            assertEquals(6, ((javafx.scene.layout.VBox) pane.lookup(".settings-content")).getPadding().getTop());
            ((Button) pane.lookupButton(ButtonType.CANCEL)).fire();
        });
    }

    private static Menu densityMenu(MainView view) {
        return (Menu) ((MenuButton) view.lookup("#profile-menu")).getItems().stream()
                .filter(item -> "density-menu".equals(item.getId())).findFirst().orElseThrow();
    }

    private static DialogPane profilePane() {
        return Window.getWindows().stream().filter(Window::isShowing)
                .map(window -> window.getScene().getRoot()).filter(root -> root instanceof DialogPane)
                .map(root -> (DialogPane) root).filter(pane -> pane.lookup("#profile-name") != null).findFirst().orElseThrow();
    }

    private static void snapshot(Node node, String name) {
        node.applyCss();
        WritableImage image = node.snapshot(null, null);
        var buffered = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < buffered.getHeight(); y++) for (int x = 0; x < buffered.getWidth(); x++) buffered.setRGB(x, y, image.getPixelReader().getArgb(x, y));
        try { ImageIO.write(buffered, "png", Path.of("target", name + ".png").toFile()); }
        catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
    }

    private static void verifyCurlImport(MainView view, String url, AtomicInteger calls) throws Exception {
        int[] initialTabs = {0};
        onFx(() -> {
            initialTabs[0] = ((TabPane) view.lookup("#request-tabs")).getTabs().size();
            Platform.runLater(() -> ((Button) view.lookup("#import-curl")).fire());
        });
        onFx(() -> {
            var pane = importPane();
            var command = (TextArea) pane.lookup("#curl-import-command");
            var confirm = (Button) pane.lookup("#confirm-curl-import");
            assertTrue(confirm.isDisabled());
            command.setText("curl '" + url + "' --insecure");
            confirm.fire();
            assertTrue(pane.getScene().getWindow().isShowing());
            assertTrue(((Label) pane.lookup("#curl-import-error")).getText().contains("--insecure"));
            assertEquals(initialTabs[0], ((TabPane) view.lookup("#request-tabs")).getTabs().size());
            ((Button) pane.lookupButton(ButtonType.CANCEL)).fire();
        });
        onFx(() -> {
            assertEquals(initialTabs[0], ((TabPane) view.lookup("#request-tabs")).getTabs().size());
            Platform.runLater(() -> ((Button) view.lookup("#import-curl")).fire());
        });
        String body = "{\"message\":\"Jocote ñ O'Reilly\"}";
        onFx(() -> {
            var pane = importPane();
            @SuppressWarnings("unchecked")
            var shell = (ComboBox<CurlGenerator.Shell>) pane.lookup("#curl-import-shell");
            shell.setValue(CurlGenerator.Shell.POWERSHELL);
            ((TextArea) pane.lookup("#curl-import-command")).setText("curl.exe `\n '" + url
                    + "' --json '" + body.replace("'", "''") + "'");
            ((Button) pane.lookup("#confirm-curl-import")).fire();
        });
        onFx(() -> {
            view.applyCss(); view.layout();
            var tabs = (TabPane) view.lookup("#request-tabs");
            assertEquals(initialTabs[0] + 1, tabs.getTabs().size());
            var editor = (RequestEditor) tabs.getSelectionModel().getSelectedItem().getContent();
            assertEquals("POST", editor.definition().method());
            assertEquals(body, editor.definition().body());
            assertTrue(editor.isDirty(), "Imported requests must warn before discarding unsaved work");
            assertEquals(0, calls.get(), "Importing must not send the request");
            assertTrue(((TextArea) view.lookup("#curl-code")).getText().contains("--data-raw"));
            ((Button) editor.lookup("#send-request")).fire();
        });
        boolean[] received = {false};
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!received[0] && System.nanoTime() < deadline) {
            onFx(() -> {
                var tabs = (TabPane) view.lookup("#request-tabs");
                var editor = (RequestEditor) tabs.getSelectionModel().getSelectedItem().getContent();
                var status = (Label) editor.lookup(".status-badge");
                received[0] = status != null && status.getText().equals("200 OK");
                if (received[0]) assertTrue(((TextArea) editor.lookup("#response-body")).getText().contains("Jocote ñ O'Reilly"));
            });
            if (!received[0]) Thread.sleep(50);
        }
        assertTrue(received[0], "Imported cURL must execute and display the local echo response");
        assertEquals(1, calls.get());
    }

    private static DialogPane importPane() {
        return Window.getWindows().stream().filter(Window::isShowing)
                .map(window -> window.getScene().getRoot()).filter(root -> root instanceof DialogPane)
                .map(root -> (DialogPane) root).filter(pane -> pane.lookup("#curl-import-command") != null)
                .findFirst().orElseThrow();
    }

    private static void verifyGitHubDemo(MainView view, WorkspaceService service) throws Exception {
        onFx(() -> ((Button) view.lookup("#github-demo")).fire());
        var demo = service.workspace().collections().stream().filter(c -> c.id().equals("jocote-demo-github")).findFirst().orElseThrow();
        var mapper = new ObjectMapper();
        var report = new StringBuilder();
        for (int index = 0; index < demo.requests().size(); index++) {
            var request = demo.requests().get(index);
            if (index > 0) onFx(() -> {
                view.open(request, demo.id());
                view.applyCss(); view.layout();
                var tabs = (TabPane) view.lookup("#request-tabs");
                ((Button) tabs.getSelectionModel().getSelectedItem().getContent().lookup("#send-request")).fire();
            });
            boolean[] finished = {false};
            String[] status = {""};
            String[] body = {""};
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(35);
            while (!finished[0] && System.nanoTime() < deadline) {
                onFx(() -> {
                    view.applyCss(); view.layout();
                    var tabs = (TabPane) view.lookup("#request-tabs");
                    var editor = tabs.getSelectionModel().getSelectedItem().getContent();
                    finished[0] = !((Button) editor.lookup("#send-request")).isDisabled();
                    if (finished[0]) {
                        status[0] = ((Label) editor.lookup(".status-badge")).getText();
                        var response = (TextArea) editor.lookup("#response-body");
                        body[0] = response == null ? "" : response.getText();
                    }
                });
                if (!finished[0]) Thread.sleep(50);
            }
            assertTrue(finished[0], "GitHub request timed out: " + request.url());
            assertEquals("200 OK", status[0], () -> request.url() + "\n" + body[0]);
            var json = mapper.readTree(body[0]);
            switch (index) {
                case 0 -> assertEquals("berroteran/jocote", json.path("full_name").asText());
                case 1 -> assertEquals("berroteran", json.path("login").asText());
                case 2 -> {
                    assertTrue(json.isArray() && !json.isEmpty() && json.size() <= 5);
                    json.forEach(repo -> assertEquals("berroteran", repo.path("owner").path("login").asText()));
                }
                case 3 -> assertTrue(json.has("Java"));
                default -> fail("Unexpected demo request");
            }
            String result = request.method() + " " + request.url() + " -> " + status[0] + " (contenido verificado)";
            report.append(result).append(System.lineSeparator());
            System.out.println(result);
            if (index == 0) {
                Files.writeString(Path.of("target/github-demo-response.json"), body[0], StandardCharsets.UTF_8);
                onFx(() -> {
                    WritableImage image = view.snapshot(null, null);
                    var buffered = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < buffered.getHeight(); y++) for (int x = 0; x < buffered.getWidth(); x++) buffered.setRGB(x, y, image.getPixelReader().getArgb(x, y));
                    try { ImageIO.write(buffered, "png", Path.of("target/jocote-github-demo.png").toFile()); }
                    catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
                });
            }
        }
        Files.writeString(Path.of("target/github-demo-results.txt"), report, StandardCharsets.UTF_8);
    }

    private static void onFx(Runnable task) throws Exception {
        var future = new CompletableFuture<Void>();
        Platform.runLater(() -> { try { task.run(); future.complete(null); } catch (Throwable e) { future.completeExceptionally(e); } });
        future.get(15, TimeUnit.SECONDS);
    }
}
