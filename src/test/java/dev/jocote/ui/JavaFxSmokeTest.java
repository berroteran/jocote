package dev.jocote.ui;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jocote.model.AuthConfig;
import dev.jocote.model.KeyValue;
import dev.jocote.model.RequestDefinition;
import dev.jocote.repository.WorkspaceRepository;
import dev.jocote.service.HttpRequestService;
import dev.jocote.service.WorkspaceService;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "jocote.uiTest", matches = "true")
class JavaFxSmokeTest {
    @TempDir Path directory;

    @Test void rendersDesktopAndExecutesRequestFromJavaFxControls() throws Exception {
        var started = new CompletableFuture<Void>();
        Platform.startup(() -> { Platform.setImplicitExit(false); started.complete(null); });
        started.get(10, TimeUnit.SECONDS);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/health", exchange -> {
            byte[] bytes = "{\"service\":\"Jocote\",\"status\":\"ok\",\"platforms\":[\"Windows\",\"macOS\",\"Linux\"],\"message\":\"Tu cliente REST está listo.\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        var http = new HttpRequestService();
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
                view[0] = new MainView(service, http); stage[0] = new Stage();
                var scene = new Scene(view[0], 1440, 900);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/dev/jocote/jocote.css")).toExternalForm());
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
            if (Boolean.getBoolean("jocote.githubTest")) verifyGitHubDemo(view[0], service);
        } finally {
            onFx(() -> { if (view[0] != null) view[0].dispose(); if (stage[0] != null) stage[0].close(); });
            http.close(); server.stop(0); Platform.exit();
        }
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
