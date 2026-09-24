package dev.jocote.ui;

import dev.jocote.model.AppTheme;
import dev.jocote.model.LayoutDensity;
import dev.jocote.model.ResponseData;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/** Runs inside the existing smoke test's JavaFX lifecycle. */
final class ResponseViewerChecks {
    private ResponseViewerChecks() { }

    static void verify() throws Exception {
        var pane = new ResponsePane[1]; var stage = new Stage[1];
        String json = "{\"items\":[{\"name\":\"Jocote\",\"price\":12.5},{\"name\":\"Jocote local\",\"enabled\":true}],\"nothing\":null}";
        try {
            fx(() -> {
                pane[0] = new ResponsePane(); stage[0] = new Stage();
                stage[0].setScene(new Scene(pane[0], 900, 650));
                ThemeManager.apply(stage[0].getScene(), AppTheme.LIGHT, LayoutDensity.NORMAL);
                stage[0].show(); pane[0].show(text("application/json", json)); select(pane[0], 1);
            });
            await(() -> pane[0].lookup("#response-json-tree") != null);
            fx(() -> {
                var tree = (TreeView<?>) pane[0].lookup("#response-json-tree");
                assertEquals(2, tree.getRoot().getChildren().size());
                tree.getRoot().getChildren().getFirst().setExpanded(true);
                assertEquals(2, tree.getRoot().getChildren().getFirst().getChildren().size());
                for (var theme : AppTheme.values()) {
                    ThemeManager.apply(stage[0].getScene(), theme, LayoutDensity.NORMAL);
                    pane[0].applyCss(); pane[0].layout();
                    assertNotNull(pane[0].lookup(".syntax-key"));
                }
                ThemeManager.apply(stage[0].getScene(), AppTheme.DARK, LayoutDensity.COMPACT);
                snapshot(pane[0], "jocote-response-tree");
                @SuppressWarnings("unchecked")
                var mode = (ComboBox<String>) pane[0].lookup("#response-json-mode");
                mode.setValue("Código"); pane[0].applyCss(); pane[0].layout();
                assertNotNull(pane[0].lookup("#response-highlighted"));
                assertNotNull(pane[0].lookup(".syntax-string"));
                assertNotNull(pane[0].lookup(".syntax-number"));
                snapshot(pane[0], "jocote-response-json");
                select(pane[0], 2); query(pane[0], "$.items[1].name");
            });
            await(() -> !((Button) pane[0].lookup("#run-response-query")).isDisabled());
            fx(() -> {
                assertTrue(((TextArea) pane[0].lookup("#response-query-results")).getText().contains("Jocote local"));
                query(pane[0], "$.items[?(@.price>1)]");
            });
            await(() -> !((Button) pane[0].lookup("#run-response-query")).isDisabled());
            fx(() -> {
                assertTrue(((Label) pane[0].lookup("#response-query-status")).getText().contains("no soportado"));
                assertEquals("", ((TextArea) pane[0].lookup("#response-query-results")).getText());
                select(pane[0], 0);
                ((CheckBox) pane[0].lookup("#response-pretty")).fire();
                assertEquals(json, ((TextArea) pane[0].lookup("#response-body")).getText());
                ((TextField) pane[0].lookup("#response-search")).setText("Jocote");
                ((Button) pane[0].lookup("#response-next")).fire();
                assertEquals("Jocote", ((TextArea) pane[0].lookup("#response-body")).getSelectedText());
                assertEquals("1 / 2", ((Label) pane[0].lookup("#response-matches")).getText());
                ((Button) pane[0].lookup("#response-next")).fire();
                assertEquals("2 / 2", ((Label) pane[0].lookup("#response-matches")).getText());
                ((Button) pane[0].lookup("#response-next")).fire();
                assertEquals("1 / 2", ((Label) pane[0].lookup("#response-matches")).getText());
                ((CheckBox) pane[0].lookup("#response-wrap")).fire();
                assertTrue(((TextArea) pane[0].lookup("#response-body")).isWrapText());
                ((CheckBox) pane[0].lookup("#response-pretty")).fire();
                pane[0].show(text("application/xml", "<root><item id=\"1\">uno</item><item id=\"2\">dos</item></root>"));
                select(pane[0], 1);
            });
            await(() -> pane[0].lookup("#response-highlighted") != null);
            fx(() -> { select(pane[0], 2); query(pane[0], "count(//item)"); });
            await(() -> !((Button) pane[0].lookup("#run-response-query")).isDisabled());
            fx(() -> {
                assertEquals("2.0", ((TextArea) pane[0].lookup("#response-query-results")).getText());
                snapshot(pane[0], "jocote-response-xpath");
                pane[0].show(text("text/html", "<h1>Vista HTML</h1><p>Hola <b>Jocote</b></p>"
                        + "<script>alert('NEVER_EXECUTE')</script><img src='https://example.test/remote'>"));
                select(pane[0], 1);
            });
            await(() -> pane[0].lookup("#response-html") != null);
            fx(() -> {
                var html = (TextFlow) pane[0].lookup("#response-html");
                String visible = html.getChildren().stream().map(node -> ((Text) node).getText()).reduce("", String::concat);
                assertTrue(visible.contains("Jocote")); assertFalse(visible.contains("NEVER_EXECUTE"));
                assertTrue(visible.contains("Imagen externa omitida"));
                snapshot(pane[0], "jocote-response-html");
                select(pane[0], 2); assertTrue(((Button) pane[0].lookup("#run-response-query")).isDisabled());
            });
            var image = new BufferedImage(160, 80, BufferedImage.TYPE_INT_RGB);
            var output = new ByteArrayOutputStream(); ImageIO.write(image, "png", output);
            fx(() -> { pane[0].show(binary("image/png", output.toByteArray())); select(pane[0], 1); });
            await(() -> pane[0].lookup("#response-image") != null);
            fx(() -> {
                var view = (ImageView) pane[0].lookup("#response-image");
                assertFalse(view.getImage().isError());
                pane[0].show(text("application/json", "{invalid"));
            });
            await(() -> ((Label) pane[0].lookup("#response-preview-notice")).getText().contains("inválido"));
            fx(() -> {
                select(pane[0], 0);
                assertEquals("{invalid", ((TextArea) pane[0].lookup("#response-body")).getText());
                assertTrue(((CheckBox) pane[0].lookup("#response-pretty")).isDisabled());
                // A previous large parse must not overwrite the latest response.
                pane[0].show(text("application/json", "{\"old\":\"" + "x".repeat(200_000) + "\"}"));
                pane[0].show(text("application/json", "{\"latest\":true}"));
                select(pane[0], 1);
            });
            await(() -> pane[0].lookup("#response-json-tree") != null);
            fx(() -> {
                select(pane[0], 0);
                String body = ((TextArea) pane[0].lookup("#response-body")).getText();
                assertTrue(body.contains("latest")); assertFalse(body.contains("old"));
                pane[0].loading(); pane[0].showError(new java.net.http.HttpTimeoutException("Timeout"));
                assertNull(pane[0].lookup("#response-json-tree"));
            });
        } finally {
            fx(() -> { if (pane[0] != null) pane[0].dispose(); if (stage[0] != null) stage[0].close(); });
        }
    }

    private static void select(ResponsePane pane, int index) {
        ((TabPane) pane.lookup("#response-tabs")).getSelectionModel().select(index); pane.applyCss(); pane.layout();
    }
    private static void query(ResponsePane pane, String expression) {
        ((TextField) pane.lookup("#response-query")).setText(expression);
        ((Button) pane.lookup("#run-response-query")).fire();
    }
    private static ResponseData text(String type, String text) { return binary(type, text.getBytes(StandardCharsets.UTF_8)); }
    private static ResponseData binary(String type, byte[] bytes) {
        return new ResponseData(200, Map.of("Content-Type", List.of(type)), bytes, 1, URI.create("https://example.test/"), "HTTP_1_1");
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); boolean[] done = {false};
        while (!done[0] && System.nanoTime() < deadline) {
            fx(() -> done[0] = condition.getAsBoolean()); if (!done[0]) Thread.sleep(20);
        }
        assertTrue(done[0], "Response viewer must complete background work");
    }
    private static void fx(Runnable action) throws Exception {
        var future = new CompletableFuture<Void>();
        Platform.runLater(() -> { try { action.run(); future.complete(null); } catch (Throwable e) { future.completeExceptionally(e); } });
        future.get(10, TimeUnit.SECONDS);
    }
    private static void snapshot(ResponsePane pane, String name) {
        pane.applyCss(); pane.layout();
        var image = pane.snapshot(null, null);
        var output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) for (int x = 0; x < output.getWidth(); x++) output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
        try { ImageIO.write(output, "png", Path.of("target", name + ".png").toFile()); }
        catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
    }
}
