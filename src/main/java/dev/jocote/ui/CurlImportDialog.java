package dev.jocote.ui;

import dev.jocote.service.CurlGenerator;
import dev.jocote.service.CurlImporter;
import dev.jocote.service.RequestPreparer;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

final class CurlImportDialog extends Dialog<CurlImporter.Result> {
    private CurlImporter.Result parsed;

    CurlImportDialog(Window owner, RequestPreparer preparer) {
        initOwner(owner);
        setTitle("Importar cURL");
        setHeaderText("Pega un comando para abrir una petición editable");
        setResizable(true);
        var shell = new ComboBox<CurlGenerator.Shell>();
        shell.setId("curl-import-shell");
        shell.getItems().setAll(CurlGenerator.Shell.values());
        shell.setConverter(new StringConverter<>() {
            @Override public String toString(CurlGenerator.Shell value) {
                return value == CurlGenerator.Shell.POWERSHELL ? "PowerShell 7.3+ · Windows" : "Bash · macOS / Linux / Git Bash";
            }
            @Override public CurlGenerator.Shell fromString(String text) { throw new UnsupportedOperationException(); }
        });
        shell.setValue(CurlGenerator.Shell.BASH);
        var command = UiSupport.codeArea(true);
        command.setId("curl-import-command");
        command.setPromptText("curl 'https://api.github.com/repos/berroteran/jocote' -H 'Accept: application/json'");
        command.setPrefRowCount(10);
        var note = new Label("Selecciona el shell de origen. No se ejecuta el comando ni se envía la petición al importar. "
                + "Se conservan los parámetros en la URL y el formulario como texto codificado. "
                + "Sin --max-time se aplica el timeout de Jocote (30 s).");
        note.setWrapText(true);
        var error = new Label(); error.setId("curl-import-error"); error.setWrapText(true);
        error.getStyleClass().add("validation-error");
        var content = new VBox(12, new Label("Sintaxis del comando"), shell, command, note, error);
        content.setPadding(new Insets(12));
        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(720);
        var importButton = new ButtonType("Importar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(importButton, ButtonType.CANCEL);
        var button = getDialogPane().lookupButton(importButton);
        button.setId("confirm-curl-import");
        button.disableProperty().bind(command.textProperty().isEmpty());
        var importer = new CurlImporter(preparer);
        button.addEventFilter(ActionEvent.ACTION, event -> {
            try { parsed = importer.parse(command.getText(), shell.getValue()); }
            catch (IllegalArgumentException e) { error.setText(e.getMessage()); event.consume(); }
        });
        setResultConverter(type -> type == importButton ? parsed : null);
        command.textProperty().addListener((obs, old, value) -> error.setText(""));
        shell.valueProperty().addListener((obs, old, value) -> error.setText(""));
        setOnShown(event -> command.requestFocus());
    }
}
