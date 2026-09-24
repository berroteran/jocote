package dev.jocote.ui;

import dev.jocote.model.UserPreferences;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Window;

final class ProfileDialog extends Dialog<UserPreferences> {
    private UserPreferences edited;

    ProfileDialog(Window owner, UserPreferences current) {
        initOwner(owner); setTitle("Mi perfil"); setHeaderText("Perfil local de Jocote");
        ThemeManager.styleDialog(this);
        var name = new TextField(current.displayName()); name.setId("profile-name");
        var alias = new TextField(current.alias()); alias.setId("profile-alias");
        var email = new TextField(current.email()); email.setId("profile-email");
        name.setPromptText("Nombre visible"); alias.setPromptText("Opcional"); email.setPromptText("Opcional");
        var note = new Label("El avatar usa las iniciales de tu nombre. Estos datos se guardan solo en este equipo y no se envían con tus peticiones.");
        note.setWrapText(true); note.getStyleClass().add("muted");
        note.setMinHeight(Region.USE_PREF_SIZE);
        var error = new Label(); error.setWrapText(true); error.setId("profile-error"); error.getStyleClass().add("validation-error");
        var content = new VBox(10, new Label("Nombre"), name, new Label("Alias"), alias, new Label("Correo"), email, note, error);
        content.setPadding(new Insets(16)); getDialogPane().setContent(content); getDialogPane().setPrefWidth(440);
        content.getStyleClass().add("settings-content");
        var save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        var button = getDialogPane().lookupButton(save); button.setId("save-profile");
        button.addEventFilter(ActionEvent.ACTION, event -> {
            try { edited = current.withProfile(name.getText(), alias.getText(), email.getText()); }
            catch (IllegalArgumentException e) { error.setText(e.getMessage()); event.consume(); }
        });
        setResultConverter(type -> type == save ? edited : null);
        name.textProperty().addListener((obs, old, value) -> error.setText(""));
        alias.textProperty().addListener((obs, old, value) -> error.setText(""));
        email.textProperty().addListener((obs, old, value) -> error.setText(""));
    }
}
