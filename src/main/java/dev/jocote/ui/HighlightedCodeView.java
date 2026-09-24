package dev.jocote.ui;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/** Virtualized read-only syntax view; the original TextArea remains available for selection. */
final class HighlightedCodeView extends ListView<String> {
    HighlightedCodeView(String code, boolean json) {
        setId("response-highlighted"); getStyleClass().add("syntax-view");
        getItems().setAll(code.split("\\R", -1)); setFixedCellSize(22);
        setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(String line, boolean empty) {
                super.updateItem(line, empty); setText(null); setGraphic(null);
                if (empty || line == null) return;
                var flow = new TextFlow(); flow.getStyleClass().add("syntax-line"); flow.setMinWidth(Region.USE_PREF_SIZE);
                String visible = line.length() > 8_192 ? line.substring(0, 8_192) + " … [línea limitada]" : line;
                add(flow, String.format("%4d  ", getIndex() + 1), "syntax-line-number");
                int start = 0, tokens = 0;
                while (start < visible.length()) {
                    if (++tokens > 512) { add(flow, visible.substring(start), "syntax-plain"); break; }
                    char c = visible.charAt(start);
                    int end = start + 1;
                    String style = "syntax-plain";
                    if (c == '"' || !json && c == '\'') {
                        char quote = c;
                        while (end < visible.length()) {
                            char next = visible.charAt(end++);
                            if (json && next == '\\' && end < visible.length()) end++;
                            else if (next == quote) break;
                        }
                        int next = end;
                        while (next < visible.length() && Character.isWhitespace(visible.charAt(next))) next++;
                        style = json && next < visible.length() && visible.charAt(next) == ':' ? "syntax-key" : "syntax-string";
                    } else if (json && (c == '-' || Character.isDigit(c))) {
                        while (end < visible.length() && "0123456789.eE+-".indexOf(visible.charAt(end)) >= 0) end++;
                        style = "syntax-number";
                    } else if (Character.isLetter(c) || !json && "<>/!?".indexOf(c) >= 0) {
                        while (end < visible.length() && (Character.isLetterOrDigit(visible.charAt(end)) || "_:-".indexOf(visible.charAt(end)) >= 0)) end++;
                        style = json ? "syntax-literal" : "syntax-key";
                    } else {
                        while (end < visible.length() && Character.isWhitespace(visible.charAt(end))) end++;
                    }
                    add(flow, visible.substring(start, end), style); start = end;
                }
                setGraphic(flow);
            }
        });
    }

    private static void add(TextFlow flow, String value, String style) {
        var text = new Text(value); text.getStyleClass().add(style); flow.getChildren().add(text);
    }
}
