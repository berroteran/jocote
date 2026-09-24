package dev.jocote.ui;

import dev.jocote.model.AppTheme;
import dev.jocote.model.LayoutDensity;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;

import java.util.Objects;
import java.util.List;
import java.util.Locale;

final class ThemeManager {
    private static final String LAYOUT = resource("jocote-layout.css");
    private static final String COLORS = resource("jocote.css");
    private static final String DENSITY = resource("jocote-density.css");

    private ThemeManager() { }

    static String label(AppTheme theme) {
        return switch (theme) {
            case LIGHT -> "Jocote claro";
            case DARK -> "Jocote oscuro";
            case MODENA -> "Modena";
            case CASPIAN -> "Caspian";
        };
    }

    static String label(LayoutDensity density) {
        return switch (density) {
            case COMPACT -> "Compacta";
            case NORMAL -> "Normal";
            case COMFORTABLE -> "Amplia";
        };
    }

    static void apply(Scene scene, AppTheme theme, LayoutDensity density) {
        String nativeTheme = theme == AppTheme.CASPIAN ? Application.STYLESHEET_CASPIAN : Application.STYLESHEET_MODENA;
        if (!nativeTheme.equals(Application.getUserAgentStylesheet())) Application.setUserAgentStylesheet(nativeTheme);
        var styles = theme == AppTheme.LIGHT || theme == AppTheme.DARK
                ? List.of(LAYOUT, COLORS, DENSITY) : List.of(LAYOUT, DENSITY);
        // Keep existing controls and their state when only spacing changes.
        if (!scene.getStylesheets().equals(styles)) scene.getStylesheets().setAll(styles);
        var classes = scene.getRoot().getStyleClass();
        if (theme == AppTheme.DARK) { if (!classes.contains("theme-dark")) classes.add("theme-dark"); }
        else classes.remove("theme-dark");
        String selected = "density-" + density.name().toLowerCase(Locale.ROOT);
        if (!classes.contains(selected)) {
            classes.removeAll("density-compact", "density-normal", "density-comfortable");
            classes.add(selected);
        }
    }

    static void styleDialog(Dialog<?> dialog) {
        var owner = dialog.getOwner();
        if (owner == null || owner.getScene() == null) return;
        dialog.getDialogPane().getStylesheets().setAll(owner.getScene().getStylesheets());
        if (owner.getScene().getRoot().getStyleClass().contains("theme-dark")) {
            dialog.getDialogPane().getStyleClass().add("theme-dark");
        }
        owner.getScene().getRoot().getStyleClass().stream().filter(name -> name.startsWith("density-"))
                .forEach(name -> dialog.getDialogPane().getStyleClass().add(name));
    }

    private static String resource(String name) {
        return Objects.requireNonNull(ThemeManager.class.getResource("/dev/jocote/" + name)).toExternalForm();
    }
}
