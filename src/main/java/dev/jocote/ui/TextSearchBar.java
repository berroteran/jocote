package dev.jocote.ui;

import javafx.animation.PauseTransition;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/** Literal, case-sensitive search over the displayed text, including wrapped lines. */
final class TextSearchBar extends FlowPane {
    private final TextArea target;
    private final TextField search = new TextField();
    private final Label count = new Label();
    private final List<Integer> positions = new ArrayList<>();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(120));
    private int selected = -1;

    TextSearchBar(TextArea target, String id) {
        super(6, 4); this.target = target;
        search.setId(id + "-search"); search.setPromptText("Buscar texto…"); search.setPrefColumnCount(13);
        search.setTooltip(new Tooltip("Búsqueda literal; distingue mayúsculas. Enter: siguiente coincidencia."));
        var previous = UiSupport.button("↑", "Coincidencia anterior", () -> move(-1));
        var next = UiSupport.button("↓", "Siguiente coincidencia", () -> move(1));
        previous.setId(id + "-previous"); next.setId(id + "-next"); count.setId(id + "-matches");
        previous.setAccessibleText("Coincidencia anterior"); next.setAccessibleText("Siguiente coincidencia");
        var wrap = new CheckBox("Ajustar líneas"); wrap.setId(id + "-wrap");
        target.wrapTextProperty().bind(wrap.selectedProperty());
        getChildren().addAll(search, previous, next, count, wrap);
        debounce.setOnFinished(event -> refresh());
        search.textProperty().addListener((obs, old, value) -> debounce.playFromStart());
        target.textProperty().addListener((obs, old, value) -> { debounce.stop(); refresh(); });
        search.setOnAction(event -> { debounce.stop(); refreshIfNeeded(); move(1); });
        previous.disableProperty().bind(count.textProperty().isEqualTo("0 coincidencias").or(search.textProperty().isEmpty()));
        next.disableProperty().bind(previous.disableProperty());
    }

    private String indexedTerm = "";
    private void refreshIfNeeded() { if (!indexedTerm.equals(search.getText())) refresh(); }
    private void refresh() {
        indexedTerm = search.getText(); positions.clear(); selected = -1;
        if (!indexedTerm.isEmpty()) {
            String text = target.getText();
            int at = text.indexOf(indexedTerm);
            while (at >= 0 && positions.size() < 10_001) {
                positions.add(at); at = text.indexOf(indexedTerm, at + 1);
            }
        }
        updateCount();
    }

    private void move(int direction) {
        debounce.stop(); refreshIfNeeded();
        if (positions.isEmpty()) return;
        selected = selected < 0 ? (direction > 0 ? 0 : positions.size() - 1)
                : Math.floorMod(selected + direction, positions.size());
        int start = positions.get(selected);
        target.selectRange(start, start + indexedTerm.length());
        updateCount();
    }

    private void updateCount() {
        count.setText(indexedTerm.isEmpty() ? "" : positions.isEmpty() ? "0 coincidencias"
                : (selected < 0 ? 0 : selected + 1) + " / " + (positions.size() > 10_000 ? "10 000+" : positions.size()));
    }

    void dispose() { debounce.stop(); }
}
