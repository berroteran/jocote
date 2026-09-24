package dev.jocote.ui;

import dev.jocote.model.AuthConfig;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

final class AuthEditor extends VBox {
    private final ComboBox<AuthConfig.Type> type = new ComboBox<>();
    private final TextField username = new TextField();
    private final PasswordField secret = new PasswordField();
    private final TextField key = new TextField();
    private final ComboBox<AuthConfig.Placement> placement = new ComboBox<>();
    private final GridPane fields = new GridPane();

    AuthEditor(AuthConfig config, Runnable onChange) {
        setPadding(new Insets(16)); setSpacing(12);
        type.getItems().setAll(AuthConfig.Type.values());
        type.setConverter(new StringConverter<>() {
            @Override public String toString(AuthConfig.Type value) {
                return switch (value) { case NONE -> "Sin autorización"; case BEARER -> "Bearer token"; case BASIC -> "Basic auth"; case API_KEY -> "API key"; };
            }
            @Override public AuthConfig.Type fromString(String value) { throw new UnsupportedOperationException(); }
        });
        type.setValue(config.type()); type.setPrefWidth(240);
        username.setText(config.username()); secret.setText(config.secret()); key.setText(config.key());
        key.setPromptText("X-API-Key"); secret.setPromptText("Credencial");
        placement.getItems().setAll(AuthConfig.Placement.values()); placement.setValue(config.placement());
        fields.setHgap(16); fields.setVgap(12);
        var help = new Label("La autorización configurada reemplaza el encabezado del mismo nombre.\nLas credenciales guardadas forman parte del archivo local de la colección.");
        help.setWrapText(true); help.getStyleClass().add("muted");
        getChildren().addAll(new Label("TIPO DE AUTORIZACIÓN"), type, fields, help);
        type.valueProperty().addListener((obs, old, value) -> { rebuild(); onChange.run(); });
        username.textProperty().addListener((obs, old, value) -> onChange.run());
        secret.textProperty().addListener((obs, old, value) -> onChange.run());
        key.textProperty().addListener((obs, old, value) -> onChange.run());
        placement.valueProperty().addListener((obs, old, value) -> onChange.run());
        rebuild();
    }

    private void rebuild() {
        fields.getChildren().clear();
        switch (type.getValue()) {
            case NONE -> { }
            case BEARER -> fields.addRow(0, new Label("Token"), secret);
            case BASIC -> {
                fields.addRow(0, new Label("Usuario"), username);
                fields.addRow(1, new Label("Contraseña"), secret);
            }
            case API_KEY -> {
                fields.addRow(0, new Label("Nombre"), key);
                fields.addRow(1, new Label("Valor"), secret);
                fields.addRow(2, new Label("Ubicación"), placement);
            }
        }
        GridPane.setHgrow(username, Priority.ALWAYS); GridPane.setHgrow(secret, Priority.ALWAYS); GridPane.setHgrow(key, Priority.ALWAYS);
    }

    AuthConfig value() { return new AuthConfig(type.getValue(), username.getText(), secret.getText(), key.getText(), placement.getValue()); }
}
