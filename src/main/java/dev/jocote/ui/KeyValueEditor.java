package dev.jocote.ui;

import dev.jocote.model.KeyValue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

final class KeyValueEditor extends VBox {
    private final TableView<Row> table = new TableView<>();
    private final Runnable onChange;

    KeyValueEditor(String hint, List<KeyValue> values, Runnable onChange) {
        this.onChange = onChange;
        setSpacing(8);
        getStyleClass().add("editor-pane");
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Agrega una fila para comenzar"));
        table.setMinHeight(90);
        var enabled = new TableColumn<Row, Boolean>("✓");
        enabled.setCellValueFactory(cell -> cell.getValue().enabled);
        enabled.setCellFactory(CheckBoxTableCell.forTableColumn(enabled));
        enabled.setMinWidth(42); enabled.setMaxWidth(42);
        var key = new TableColumn<Row, String>("Nombre");
        key.setCellValueFactory(cell -> cell.getValue().key);
        key.setCellFactory(TextFieldTableCell.forTableColumn());
        key.setOnEditCommit(event -> event.getRowValue().key.set(event.getNewValue()));
        key.setPrefWidth(220);
        var value = new TableColumn<Row, String>("Valor");
        value.setCellValueFactory(cell -> cell.getValue().value);
        value.setCellFactory(TextFieldTableCell.forTableColumn());
        value.setOnEditCommit(event -> event.getRowValue().value.set(event.getNewValue()));
        value.setPrefWidth(360);
        table.getColumns().add(enabled); table.getColumns().add(key); table.getColumns().add(value);
        values.forEach(v -> table.getItems().add(new Row(v)));
        if (table.getItems().isEmpty()) table.getItems().add(new Row(new KeyValue(true, "", "")));
        table.getItems().addListener((ListChangeListener<Row>) change -> onChange.run());
        var add = UiSupport.button("+ Fila", "Agregar un par nombre / valor", () -> {
            Row row = new Row(new KeyValue(true, "", ""));
            table.getItems().add(row);
            table.getSelectionModel().select(row);
            table.scrollTo(row);
            table.requestFocus();
            table.edit(table.getItems().size() - 1, key);
        });
        var remove = UiSupport.button("Eliminar fila", "Eliminar la fila seleccionada", () -> {
            Row selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) table.getItems().remove(selected);
        });
        remove.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        var help = new Label(hint + " · Doble clic para editar; Enter para confirmar.");
        help.getStyleClass().add("muted"); help.setWrapText(true);
        getChildren().addAll(new HBox(8, add, remove), table, help);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    List<KeyValue> values() {
        return table.getItems().stream().map(row -> new KeyValue(row.enabled.get(), row.key.get(), row.value.get()))
                .filter(row -> !row.key().isBlank() || !row.value().isBlank()).toList();
    }

    private final class Row {
        private final BooleanProperty enabled;
        private final StringProperty key;
        private final StringProperty value;

        private Row(KeyValue field) {
            enabled = new SimpleBooleanProperty(field.enabled());
            key = new SimpleStringProperty(field.key());
            value = new SimpleStringProperty(field.value());
            enabled.addListener((obs, old, updated) -> onChange.run());
            key.addListener((obs, old, updated) -> onChange.run());
            value.addListener((obs, old, updated) -> onChange.run());
        }
    }
}
