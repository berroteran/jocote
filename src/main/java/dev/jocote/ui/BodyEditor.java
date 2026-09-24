package dev.jocote.ui;

import dev.jocote.model.KeyValue;
import dev.jocote.model.RequestDefinition;
import dev.jocote.service.JsonFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

final class BodyEditor extends VBox {
    private final ComboBox<RequestDefinition.BodyType> type = new ComboBox<>();
    private final TextArea body = UiSupport.codeArea(true);
    private final KeyValueEditor form;
    private final StackPane content = new StackPane();

    BodyEditor(RequestDefinition request, Runnable onChange) {
        setSpacing(10); setPadding(new Insets(12));
        type.getItems().setAll(RequestDefinition.BodyType.values());
        type.setConverter(new StringConverter<>() {
            @Override public String toString(RequestDefinition.BodyType value) {
                return switch (value) { case NONE -> "Sin body"; case JSON -> "JSON"; case TEXT -> "Texto"; case XML -> "XML"; case FORM -> "x-www-form-urlencoded"; };
            }
            @Override public RequestDefinition.BodyType fromString(String text) { throw new UnsupportedOperationException(); }
        });
        type.setValue(request.bodyType());
        body.setText(request.body()); body.setPromptText("Escribe el contenido de la petición…");
        form = new KeyValueEditor("Los valores se codifican como formulario", request.formFields(), onChange);
        var format = UiSupport.button("Formatear JSON", "Aplicar formato al JSON", () -> body.setText(JsonFormatter.format(body.getText())));
        format.disableProperty().bind(type.valueProperty().isNotEqualTo(RequestDefinition.BodyType.JSON));
        getChildren().addAll(new HBox(10, type, format), content);
        VBox.setVgrow(content, Priority.ALWAYS);
        type.valueProperty().addListener((obs, old, value) -> { rebuild(); onChange.run(); });
        body.textProperty().addListener((obs, old, value) -> onChange.run());
        rebuild();
    }

    private void rebuild() {
        content.getChildren().setAll(switch (type.getValue()) {
            case NONE -> new Label("Esta petición no envía un body.");
            case FORM -> form;
            default -> body;
        });
    }

    RequestDefinition.BodyType type() { return type.getValue(); }
    String body() { return body.getText(); }
    List<KeyValue> formFields() { return form.values(); }
}
