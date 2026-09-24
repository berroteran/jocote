package dev.jocote.ui;

import dev.jocote.model.ResponseData;
import dev.jocote.model.ResponsePreview;
import dev.jocote.model.ResponsePreview.Format;
import dev.jocote.model.ResponsePreview.ValueNode;
import dev.jocote.model.ResponsePreview.QueryResult;
import dev.jocote.service.ResponseInspector;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

/** Owns cancellable presentation work. Only completion callbacks touch scene graph state. */
final class ResponsePreviewPane extends BorderPane {
    private final ResponseInspector inspector = new ResponseInspector();
    private final Consumer<ResponsePreview> onReady;
    private final Label notice = new Label();
    private final TextField expression = new TextField();
    private final TextArea results = UiSupport.codeArea(false);
    private final Label queryStatus = new Label();
    private final Label queryHelp = new Label();
    private final Button run = UiSupport.button("Consultar", "Evaluar sobre la respuesta completa admitida", this::query);
    private final TextSearchBar resultSearch = new TextSearchBar(results, "query");
    private final VBox queryPane;
    private ResponseData response;
    private Format format = Format.TEXT;
    private Task<?> previewTask, queryTask;
    private long generation;

    private record Rendered(ResponsePreview preview, Image image) { }

    ResponsePreviewPane(Consumer<ResponsePreview> onReady) {
        this.onReady = onReady; setId("response-preview");
        notice.setId("response-preview-notice"); notice.setWrapText(true); notice.getStyleClass().add("muted");
        setBottom(notice);
        expression.setId("response-query"); expression.setPromptText("JSONPath o XPath"); expression.setPrefColumnCount(16);
        HBox.setHgrow(expression, Priority.ALWAYS); run.setId("run-response-query");
        var queryRow = new HBox(6, expression, run); queryRow.getStyleClass().add("control-row");
        results.setId("response-query-results"); results.setPrefRowCount(4); VBox.setVgrow(results, Priority.ALWAYS);
        queryHelp.setWrapText(true); queryHelp.getStyleClass().add("muted");
        queryStatus.setId("response-query-status"); queryStatus.setWrapText(true);
        var copy = UiSupport.button("Copiar resultados", "Copiar el resultado de la consulta", () -> UiSupport.copy(results.getText()));
        copy.disableProperty().bind(results.textProperty().isEmpty());
        var controls = new FlowPane(6, 4, copy, queryStatus);
        queryPane = new VBox(6, queryRow, queryHelp, resultSearch, results, controls);
        queryPane.getStyleClass().add("content-stack");
        expression.setOnAction(event -> query());
        expression.textProperty().addListener((obs, old, value) -> {
            cancelQuery(); results.clear(); queryStatus.setText(""); run.setDisable(!queryable());
        });
        clear();
    }

    Node queryPane() { return queryPane; }

    void show(ResponseData value) {
        clear(); response = value; long revision = generation;
        setCenter(new Label("Preparando vista…"));
        var task = new Task<Rendered>() {
            @Override protected Rendered call() throws Exception {
                ResponsePreview preview = inspector.inspect(value);
                Image image = null;
                if (preview.format() == Format.IMAGE) {
                    // Decode the first frame only; conversion cannot follow links or animate GIF.
                    try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(value.body()));
                         var png = new ByteArrayOutputStream(); var encoded = new MemoryCacheImageOutputStream(png)) {
                        var readers = ImageIO.getImageReaders(input);
                        if (!readers.hasNext()) throw new IllegalArgumentException("Imagen inválida.");
                        var reader = readers.next();
                        java.awt.image.BufferedImage decoded;
                        try {
                            reader.setInput(input, true, true);
                            decoded = reader.read(0);
                        } finally { reader.dispose(); }
                        ImageIO.write(decoded, "png", encoded); encoded.flush();
                        image = new Image(new ByteArrayInputStream(png.toByteArray()), 1_600, 1_600, true, true);
                        if (image.isError()) throw new IllegalArgumentException("Imagen inválida.");
                    }
                }
                return new Rendered(preview, image);
            }
        };
        previewTask = task;
        task.setOnSucceeded(event -> {
            if (revision != generation) return;
            var rendered = task.getValue(); var preview = rendered.preview(); format = preview.format();
            notice.setText(preview.notice()); setCenter(render(preview, rendered.image()));
            expression.setDisable(!queryable()); run.setDisable(!queryable());
            queryHelp.setText(format == Format.JSON
                    ? "JSONPath: $, $.nombre, $['clave'], $[0], $[-1], $[*], $..nombre. Sin filtros, slices, uniones ni funciones."
                    : format == Format.XML
                    ? "XPath 1.0: //elemento, /raíz/@atributo, count(//elemento). Prefijos declarados en la raíz; para namespace por defecto usa local-name()."
                    : "Las consultas requieren una respuesta JSON o XML válida dentro de los límites de vista.");
            onReady.accept(preview);
        });
        task.setOnFailed(event -> {
            if (revision != generation) return;
            setCenter(new Label("No se pudo generar la vista. El contenido original sigue disponible en Body."));
            notice.setText("Documento o imagen inválidos para la previsualización.");
        });
        Thread.ofVirtual().name("jocote-response-preview").start(task);
    }

    void clear() {
        generation++; if (previewTask != null) previewTask.cancel(true); cancelQuery();
        response = null; format = Format.TEXT; results.clear(); expression.clear();
        run.setDisable(true); expression.setDisable(true); queryStatus.setText(""); queryHelp.setText("");
        notice.setText(""); setCenter(new Label("Sin respuesta."));
    }

    void dispose() { clear(); resultSearch.dispose(); }

    private boolean queryable() { return response != null && (format == Format.JSON || format == Format.XML); }
    private void cancelQuery() { if (queryTask != null) queryTask.cancel(true); queryTask = null; }

    private void query() {
        if (!queryable()) return;
        cancelQuery(); long revision = generation; ResponseData value = response; Format kind = format;
        String query = expression.getText(); results.clear(); queryStatus.setText("Consultando…"); run.setDisable(true);
        var task = new Task<QueryResult>() {
            @Override protected QueryResult call() { return inspector.query(kind, value.text(), query); }
        };
        queryTask = task;
        task.setOnSucceeded(event -> {
            if (revision != generation || queryTask != task) return;
            results.setText(task.getValue().text()); queryStatus.setText(task.getValue().matches() + " resultado(s)"); run.setDisable(false);
        });
        task.setOnFailed(event -> {
            if (revision != generation || queryTask != task) return;
            queryStatus.setText(task.getException() instanceof IllegalArgumentException
                    ? task.getException().getMessage() : "No se pudo evaluar la consulta.");
            run.setDisable(false);
        });
        Thread.ofVirtual().name("jocote-response-query").start(task);
    }

    private Node render(ResponsePreview preview, Image image) {
        return switch (preview.format()) {
            case JSON -> jsonView(preview);
            case XML -> new HighlightedCodeView(preview.formattedText(), false);
            case HTML -> htmlView(preview);
            case IMAGE -> imageView(preview, image);
            case TEXT -> new Label("Consulta el contenido en Body.");
        };
    }

    private Node jsonView(ResponsePreview preview) {
        var tree = new TreeView<ValueNode>(new ValueItem(preview.tree())); tree.setId("response-json-tree");
        tree.getRoot().setExpanded(true); tree.getStyleClass().add("response-tree");
        tree.setCellFactory(view -> new TreeCell<>() {
            @Override protected void updateItem(ValueNode node, boolean empty) {
                super.updateItem(node, empty); setText(null); setGraphic(null); setTooltip(null);
                if (empty || node == null) return;
                var key = new Text(shortText(node.name(), 160) + ": "); key.getStyleClass().add("syntax-key");
                var value = new Text(shortText(node.value(), 400)); value.getStyleClass().add("syntax-" + node.type());
                var flow = new TextFlow(key, value); flow.getStyleClass().add("syntax-line"); setGraphic(flow);
                setTooltip(new Tooltip(shortText(node.name() + ": " + node.value(), 2_000)));
            }
        });
        var selector = new ComboBox<String>(); selector.getItems().addAll("Árbol", "Código"); selector.setValue("Árbol");
        selector.setId("response-json-mode"); selector.setAccessibleText("Vista JSON");
        var copy = UiSupport.button("Copiar valor", "Copiar el valor de la hoja seleccionada", () -> {
            var selected = tree.getSelectionModel().getSelectedItem();
            if (selected != null) UiSupport.copy(selected.getValue().value());
        });
        tree.getSelectionModel().selectedItemProperty().addListener((obs, old, item) ->
                copy.setDisable(item == null || item.getValue().type().equals("object") || item.getValue().type().equals("array")));
        copy.setDisable(true);
        var area = new BorderPane(tree);
        selector.valueProperty().addListener((obs, old, value) -> {
            area.setCenter(value.equals("Árbol") ? tree : new HighlightedCodeView(preview.formattedText(), true));
            copy.setVisible(value.equals("Árbol")); copy.setManaged(copy.isVisible());
        });
        var box = new VBox(6, new FlowPane(6, 4, selector, copy), area); VBox.setVgrow(area, Priority.ALWAYS);
        return box;
    }

    private Node htmlView(ResponsePreview preview) {
        var flow = new TextFlow(); flow.setId("response-html"); flow.getStyleClass().add("html-preview");
        for (var span : preview.html()) {
            var text = new Text(span.text()); text.getStyleClass().add("html-text");
            if (span.bold() || span.heading() > 0) text.getStyleClass().add("html-bold");
            if (span.italic()) text.getStyleClass().add("html-italic");
            if (span.code()) text.getStyleClass().add("html-code");
            if (span.heading() > 0) text.getStyleClass().add("html-heading-" + span.heading());
            flow.getChildren().add(text);
        }
        var scroll = new ScrollPane(flow); scroll.setFitToWidth(true); return scroll;
    }

    private Node imageView(ResponsePreview preview, Image image) {
        var view = new ImageView(image); view.setId("response-image"); view.setPreserveRatio(true); view.setSmooth(true);
        var scroll = new ScrollPane(view); scroll.setPannable(true);
        scroll.viewportBoundsProperty().addListener((obs, old, bounds) -> view.setFitWidth(Math.min(image.getWidth(), Math.max(1, bounds.getWidth() - 4))));
        var label = new Label(preview.image().format() + " · " + preview.image().width() + " × " + preview.image().height() + " px");
        var box = new VBox(6, label, scroll); VBox.setVgrow(scroll, Priority.ALWAYS); return box;
    }

    private static String shortText(String text, int limit) { return text.length() <= limit ? text : text.substring(0, limit) + "…"; }

    private static final class ValueItem extends TreeItem<ValueNode> {
        private boolean loaded;
        ValueItem(ValueNode value) { super(value); }
        @Override public boolean isLeaf() { return getValue().children().isEmpty(); }
        @Override public javafx.collections.ObservableList<TreeItem<ValueNode>> getChildren() {
            if (!loaded) {
                loaded = true;
                for (var child : getValue().children()) super.getChildren().add(new ValueItem(child));
            }
            return super.getChildren();
        }
    }
}
