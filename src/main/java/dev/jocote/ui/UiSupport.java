package dev.jocote.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Window;

final class UiSupport {
    private UiSupport() { }

    static Button button(String text, String tooltip, Runnable action) {
        var button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(event -> action.run());
        return button;
    }

    static TextArea codeArea(boolean editable) {
        var area = new TextArea();
        area.setEditable(editable);
        area.setWrapText(false);
        area.getStyleClass().add("code-area");
        return area;
    }

    static void copy(String text) {
        var content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    static void error(Window owner, String title, String message) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        ThemeManager.styleDialog(alert);
        alert.setTitle("Jocote");
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "Error inesperado." : message);
        alert.showAndWait();
    }
}
