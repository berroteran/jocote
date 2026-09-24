package dev.jocote.ui;

import dev.jocote.model.ResponseData;
import dev.jocote.service.JsonFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

final class ResponsePane extends VBox {
    private static final int PREVIEW_LIMIT = 300_000;
    private final Label status = new Label("Sin ejecutar");
    private final Label timing = new Label();
    private final TextArea body = UiSupport.codeArea(false);
    private final TextArea headers = UiSupport.codeArea(false);
    private final StackPane content = new StackPane();
    private final TabPane tabs = new TabPane();
    private final CheckBox pretty = new CheckBox("Formatear JSON");
    private final Button save;
    private final Button copy;
    private ResponseData response;

    ResponsePane() {
        body.setId("response-body");
        setMinHeight(170); setSpacing(8); setPadding(new Insets(12));
        getStyleClass().add("response-pane");
        var heading = new Label("RESPUESTA"); heading.getStyleClass().add("section-label");
        status.getStyleClass().add("status-badge");
        timing.getStyleClass().add("muted");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        var top = new HBox(12, heading, spacer, status, timing); top.setAlignment(Pos.CENTER_LEFT);
        top.getStyleClass().add("control-row");
        pretty.setSelected(true); pretty.setOnAction(event -> renderBody());
        save = UiSupport.button("Guardar respuesta", "Guardar los bytes originales de la respuesta", this::saveResponse);
        copy = UiSupport.button("Copiar", "Copiar el contenido mostrado", () -> UiSupport.copy(body.getText()));
        save.setDisable(true); copy.setDisable(true);
        var actions = new HBox(12, pretty, copy, save); actions.getStyleClass().add("control-row");
        VBox bodyBox = new VBox(8, actions, body); VBox.setVgrow(body, Priority.ALWAYS);
        bodyBox.getStyleClass().add("content-stack");
        tabs.getTabs().addAll(new Tab("Body", bodyBox), new Tab("Encabezados", headers));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        showEmpty("Tu próxima respuesta empieza aquí", "Configura una URL y pulsa Enviar · Ctrl/⌘ + Enter");
        getChildren().addAll(top, content); VBox.setVgrow(content, Priority.ALWAYS);
    }

    void loading() {
        response = null; save.setDisable(true); copy.setDisable(true);
        status.setText("Enviando…"); status.setStyle(""); timing.setText("");
        showEmpty("Esperando al servidor…", "Puedes cancelar la petición en cualquier momento.");
    }

    void show(ResponseData value) {
        response = value;
        status.setText(value.statusCode() + " " + statusName(value.statusCode()));
        status.setStyle(value.statusCode() < 400 ? "-fx-text-fill: #267454; -fx-background-color: #e3f4eb;" : "-fx-text-fill: #b23c3c; -fx-background-color: #fde8e8;");
        timing.setText(value.elapsedMillis() + " ms   ·   " + String.format(Locale.ROOT, "%.1f KiB", value.body().length / 1024.0));
        headers.setText(value.protocol() + "\n\n" + value.headers().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey()).map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
                .collect(java.util.stream.Collectors.joining("\n")));
        save.setDisable(false); copy.setDisable(false);
        renderBody(); content.getChildren().setAll(tabs);
    }

    void showError(Throwable error) {
        while ((error instanceof CompletionException || error instanceof java.util.concurrent.ExecutionException) && error.getCause() != null) error = error.getCause();
        boolean cancelled = error instanceof CancellationException;
        status.setText(cancelled ? "Cancelada" : "Error");
        status.setStyle(cancelled ? "" : "-fx-text-fill: #b23c3c; -fx-background-color: #fde8e8;"); timing.setText("");
        response = null; save.setDisable(true); copy.setDisable(true);
        String message = error instanceof TimeoutException || error instanceof java.net.http.HttpTimeoutException
                ? "Se agotó el tiempo de espera configurado." : error.getMessage();
        if (message == null || message.isBlank()) message = error.getClass().getSimpleName();
        showEmpty(cancelled ? "Petición cancelada" : "No se pudo completar la petición", message);
    }

    private void renderBody() {
        if (response == null) return;
        String text = response.text();
        boolean truncated = text.length() > PREVIEW_LIMIT;
        if (truncated) text = text.substring(0, PREVIEW_LIMIT);
        else if (pretty.isSelected()) text = JsonFormatter.format(text);
        body.setText(text + (truncated ? "\n\n[Vista limitada a 300 000 caracteres. Guarda la respuesta para obtener el contenido completo.]" : ""));
    }

    private void showEmpty(String title, String subtitle) {
        Label heading = new Label(title); heading.getStyleClass().add("empty-title");
        Label detail = new Label(subtitle); detail.getStyleClass().add("muted"); detail.setWrapText(true);
        VBox box = new VBox(10, heading, detail); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(24));
        content.getChildren().setAll(box);
    }

    private void saveResponse() {
        if (response == null) return;
        var chooser = new FileChooser(); chooser.setTitle("Guardar respuesta");
        chooser.setInitialFileName(response.contentType().contains("json") ? "response.json" : "response.bin");
        var file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try { Files.write(file.toPath(), response.body()); }
            catch (IOException e) { UiSupport.error(getScene().getWindow(), "No se pudo guardar la respuesta", e.getMessage()); }
        }
    }

    private static String statusName(int code) {
        return switch (code) {
            case 200 -> "OK"; case 201 -> "Created"; case 202 -> "Accepted"; case 204 -> "No Content";
            case 301 -> "Moved Permanently"; case 302 -> "Found"; case 304 -> "Not Modified";
            case 400 -> "Bad Request"; case 401 -> "Unauthorized"; case 403 -> "Forbidden"; case 404 -> "Not Found";
            case 405 -> "Method Not Allowed"; case 409 -> "Conflict"; case 422 -> "Unprocessable Content";
            case 429 -> "Too Many Requests"; case 500 -> "Internal Server Error"; case 502 -> "Bad Gateway"; case 503 -> "Service Unavailable";
            default -> "HTTP";
        };
    }
}
