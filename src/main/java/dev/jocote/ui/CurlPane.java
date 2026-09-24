package dev.jocote.ui;

import dev.jocote.service.CurlGenerator;
import dev.jocote.service.RequestPreparer;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

final class CurlPane extends VBox {
    private final TextArea code = UiSupport.codeArea(false);
    private final ComboBox<CurlGenerator.Shell> shell = new ComboBox<>();
    private final Label help = new Label();
    private final javafx.scene.control.Button copy;
    private final CurlGenerator generator = new CurlGenerator();
    private final RequestPreparer preparer;
    private RequestEditor editor;

    CurlPane(RequestPreparer preparer) {
        this.preparer = preparer;
        setPadding(new Insets(18, 14, 14, 14)); setSpacing(14); setMinWidth(220); setPrefWidth(310);
        getStyleClass().add("curl-pane");
        var title = new Label("Código cURL"); title.getStyleClass().add("panel-title");
        var caption = new Label("Tu petición, lista para la terminal."); caption.getStyleClass().add("muted"); caption.setWrapText(true);
        shell.getItems().setAll(CurlGenerator.Shell.values()); shell.setValue(CurlGenerator.Shell.BASH); shell.setMaxWidth(Double.MAX_VALUE);
        shell.setConverter(new StringConverter<>() {
            @Override public String toString(CurlGenerator.Shell value) { return value == CurlGenerator.Shell.BASH ? "Bash / Zsh · macOS, Linux" : "PowerShell 7.3+ · Windows"; }
            @Override public CurlGenerator.Shell fromString(String text) { throw new UnsupportedOperationException(); }
        });
        code.setWrapText(true); code.setId("curl-code");
        copy = UiSupport.button("Copiar comando", "Copiar el comando completo al portapapeles", () -> UiSupport.copy(code.getText()));
        copy.setMaxWidth(Double.MAX_VALUE);
        help.setWrapText(true); help.getStyleClass().add("muted");
        getChildren().addAll(title, caption, shell, code, copy, help); VBox.setVgrow(code, Priority.ALWAYS);
        shell.valueProperty().addListener((obs, old, value) -> update(editor));
        update(null);
    }

    void update(RequestEditor current) {
        editor = current;
        if (editor == null) { code.clear(); copy.setDisable(true); help.setText("Abre una petición para generar su comando."); return; }
        try {
            code.setText(generator.generate(preparer.prepare(editor.definition()), shell.getValue())); copy.setDisable(false);
            help.setText("Se actualiza al editar la petición. El comando incluye la autorización configurada.");
        } catch (IllegalArgumentException e) {
            code.clear(); copy.setDisable(true); help.setText(e.getMessage());
        }
    }
}
