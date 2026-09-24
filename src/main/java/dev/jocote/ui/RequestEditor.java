package dev.jocote.ui;

import dev.jocote.model.RequestDefinition;
import dev.jocote.model.ResponseData;
import dev.jocote.service.HttpRequestService;
import dev.jocote.service.RequestPreparer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;

public final class RequestEditor extends VBox {
    private final String id;
    private String name;
    private String collectionId;
    private final ComboBox<String> method = new ComboBox<>();
    private final TextField url = new TextField();
    private final Spinner<Integer> timeout = new Spinner<>(1, 600, 30);
    private final KeyValueEditor parameters;
    private final KeyValueEditor headers;
    private final AuthEditor auth;
    private final BodyEditor body;
    private final Button send;
    private final Button cancel;
    private final ResponsePane response = new ResponsePane();
    private final SimpleBooleanProperty dirty = new SimpleBooleanProperty(false);
    private final HttpRequestService http;
    private final RequestPreparer preparer;
    private final Runnable onChange;
    private CompletableFuture<ResponseData> pending;
    private boolean disposed;
    private boolean ready;

    public RequestEditor(RequestDefinition request, String collectionId, HttpRequestService http,
                         RequestPreparer preparer, Runnable onChange, Runnable onSave) {
        this.id = request.id(); this.name = request.name(); this.collectionId = collectionId;
        this.http = http; this.preparer = preparer; this.onChange = onChange;
        getStyleClass().add("request-editor"); setSpacing(12); setPadding(new Insets(16, 14, 0, 14)); setMinWidth(420);
        method.getItems().setAll("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS", "TRACE");
        method.setValue(request.method()); method.setPrefWidth(112); method.getStyleClass().add("method-selector");
        url.setText(request.url()); url.setPromptText("https://api.example.com/v1/resources"); url.setId("request-url");
        url.getStyleClass().add("url-field"); HBox.setHgrow(url, Priority.ALWAYS);
        send = UiSupport.button("Enviar", "Ejecutar petición · Ctrl/⌘ + Enter", this::send); send.setId("send-request"); send.getStyleClass().add("primary-button");
        cancel = UiSupport.button("Cancelar", "Cancelar la petición en curso", this::cancel); cancel.setDisable(true);
        var requestBar = new HBox(8, method, url, send, cancel); requestBar.setAlignment(Pos.CENTER_LEFT);
        requestBar.getStyleClass().add("control-row");
        timeout.getValueFactory().setValue(Math.clamp(request.timeoutSeconds(), 1, 600)); timeout.setPrefWidth(85);
        var timeoutLabel = new Label("Timeout (s)"); timeoutLabel.getStyleClass().add("muted");
        var save = UiSupport.button("Guardar", "Guardar en colección · Ctrl/⌘ + S", onSave);
        var hint = new Label("PETICIÓN REST"); hint.getStyleClass().add("section-label");
        var spacer = new javafx.scene.layout.Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        var options = new HBox(10, hint, spacer, timeoutLabel, timeout, save); options.setAlignment(Pos.CENTER_LEFT);
        options.getStyleClass().add("control-row");
        parameters = new KeyValueEditor("Se agregan a los parámetros existentes en la URL", request.parameters(), this::changed);
        headers = new KeyValueEditor("Desmarca una fila para omitirla del envío", request.headers(), this::changed);
        auth = new AuthEditor(request.auth(), this::changed);
        body = new BodyEditor(request, this::changed);
        var requestTabs = new TabPane(new Tab("Parámetros", parameters), new Tab("Autorización", auth),
                new Tab("Encabezados", headers), new Tab("Body", body));
        requestTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); requestTabs.setMinHeight(160);
        var split = new SplitPane(requestTabs, response); split.setOrientation(Orientation.VERTICAL); split.setDividerPositions(0.48);
        getChildren().addAll(options, requestBar, split); VBox.setVgrow(split, Priority.ALWAYS);
        method.valueProperty().addListener((obs, old, value) -> changed());
        url.textProperty().addListener((obs, old, value) -> changed());
        timeout.valueProperty().addListener((obs, old, value) -> changed());
        ready = true;
    }

    public RequestDefinition definition() {
        return new RequestDefinition(id, name, method.getValue(), url.getText(), parameters.values(), headers.values(),
                auth.value(), body.type(), body.body(), body.formFields(), timeout.getValue());
    }

    private void changed() {
        if (!ready) return;
        dirty.set(true); onChange.run();
    }

    public void send() {
        if (pending != null && !pending.isDone()) return;
        try {
            var prepared = preparer.prepare(definition());
            response.loading(); send.setDisable(true); cancel.setDisable(false);
            pending = http.send(prepared);
            pending.whenComplete((data, error) -> Platform.runLater(() -> {
                if (disposed) return;
                send.setDisable(false); cancel.setDisable(true);
                if (error != null) response.showError(error); else response.show(data);
            }));
        } catch (IllegalArgumentException e) {
            send.setDisable(false); cancel.setDisable(true); response.showError(e);
        }
    }

    public void cancel() { if (pending != null) pending.cancel(true); }
    public void dispose() { disposed = true; cancel(); }
    public boolean isDirty() { return dirty.get(); }
    public void markDirty() { changed(); }
    public ReadOnlyBooleanProperty dirtyProperty() { return dirty; }
    public String collectionId() { return collectionId; }
    public String requestId() { return id; }
    public String title() { return method.getValue() + "  " + name + (dirty.get() ? " •" : ""); }

    public void saved(String updatedName, String updatedCollectionId) {
        name = updatedName; collectionId = updatedCollectionId; dirty.set(false); onChange.run();
    }
}
