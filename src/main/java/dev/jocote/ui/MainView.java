package dev.jocote.ui;

import dev.jocote.model.RequestCollection;
import dev.jocote.model.RequestDefinition;
import dev.jocote.model.AppTheme;
import dev.jocote.model.LayoutDensity;
import dev.jocote.model.UserPreferences;
import dev.jocote.service.PreferencesService;
import dev.jocote.service.HttpRequestService;
import dev.jocote.service.RequestPreparer;
import dev.jocote.service.WorkspaceService;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class MainView extends BorderPane {
    private final WorkspaceService workspace;
    private final HttpRequestService http;
    private final PreferencesService preferences;
    private Button cycleTheme;
    private MenuButton profile;
    private final Label avatar = new Label();
    private final MenuItem profileSummary = new MenuItem();
    private final Menu appearance = new Menu("Apariencia");
    private final Menu densityMenu = new Menu("Densidad visual");
    private boolean disposed;
    private final RequestPreparer preparer = new RequestPreparer();
    private final TabPane requests = new TabPane();
    private final SplitPane layout = new SplitPane();
    private final TreeView<Entry> tree = new TreeView<>();
    private final TextField filter = new TextField();
    private final Label footer = new Label("Listo · Los cambios se guardan con Ctrl/⌘ + S");
    private final VBox collections;
    private final CurlPane curl = new CurlPane(preparer);
    private final ToggleButton showCollections = new ToggleButton("Colecciones");
    private final ToggleButton showCurl = new ToggleButton("cURL");

    public MainView(WorkspaceService workspace, HttpRequestService http, PreferencesService preferences) {
        this.workspace = workspace; this.http = http; this.preferences = preferences;
        collections = createCollections();
        requests.setId("request-tabs"); requests.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        requests.setMinWidth(420);
        requests.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> curl.update(active()));
        setTop(createHeader());
        showCollections.setSelected(true); showCurl.setSelected(true);
        showCollections.setOnAction(event -> updatePanels()); showCurl.setOnAction(event -> updatePanels());
        setCenter(layout); updatePanels();
        footer.getStyleClass().add("footer"); footer.setMaxWidth(Double.MAX_VALUE); footer.setWrapText(true); setBottom(footer);
        if (!preferences.loadWarning().isEmpty()) footer.setText(preferences.loadWarning());
        rebuildTree();
        open(RequestDefinition.blank(), null);
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                ThemeManager.apply(scene, preferences.current().theme(), preferences.current().density());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN), () -> { if (active() != null) active().send(); });
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), () -> { if (active() != null) save(active()); });
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), () -> open(RequestDefinition.blank(), selectedCollectionId()));
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), this::closeActive);
            }
        });
    }

    private HBox createHeader() {
        var mark = new Label("J"); mark.getStyleClass().add("brand-mark");
        var title = new Label("Jocote"); title.getStyleClass().add("brand-title");
        var subtitle = new Label("REST CLIENT"); subtitle.getStyleClass().add("brand-subtitle");
        var brand = new HBox(10, mark, title, subtitle); brand.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        var add = UiSupport.button("+ Nueva petición", "Abrir una pestaña · Ctrl/⌘ + N", () -> open(RequestDefinition.blank(), selectedCollectionId()));
        add.setId("new-request");
        var importCurl = UiSupport.button("Importar cURL", "Pegar un comando cURL como una petición editable", this::importCurl);
        importCurl.setId("import-curl");
        cycleTheme = UiSupport.button("", "Cambiar tema", () -> changeTheme(preferences.current().theme().next()));
        cycleTheme.setId("cycle-theme"); cycleTheme.getStyleClass().add("theme-button");
        var outline = new Circle(8, 8, 7); outline.getStyleClass().add("theme-icon-outline");
        var half = new SVGPath(); half.setContent("M 8 1 A 7 7 0 0 0 8 15 Z"); half.getStyleClass().add("theme-icon-half");
        cycleTheme.setGraphic(new Group(outline, half));
        profile = createProfileMenu();
        refreshProfile();
        var bar = new HBox(10, brand, spacer, showCollections, showCurl, importCurl, add, cycleTheme, profile);
        bar.setAlignment(Pos.CENTER_LEFT); bar.setPadding(new Insets(16, 20, 16, 20)); bar.getStyleClass().add("app-header");
        return bar;
    }

    private MenuButton createProfileMenu() {
        avatar.getStyleClass().add("profile-avatar"); avatar.setId("profile-avatar");
        var button = new MenuButton(); button.setGraphic(avatar); button.setId("profile-menu");
        button.getStyleClass().add("profile-button"); button.setMinWidth(Region.USE_PREF_SIZE);
        profileSummary.setDisable(true);
        var edit = new MenuItem("Mi perfil…"); edit.setId("edit-profile");
        edit.setOnAction(event -> new ProfileDialog(window(), preferences.current()).showAndWait().ifPresent(value ->
                savePreferences(preferences.setProfile(value.displayName(), value.alias(), value.email()), "Perfil guardado en este equipo.")));
        var group = new ToggleGroup();
        for (AppTheme theme : AppTheme.values()) {
            var item = new RadioMenuItem(ThemeManager.label(theme)); item.setUserData(theme); item.setToggleGroup(group);
            item.setOnAction(event -> changeTheme(theme)); appearance.getItems().add(item);
        }
        densityMenu.setId("density-menu");
        var densities = new ToggleGroup();
        for (LayoutDensity density : LayoutDensity.values()) {
            var item = new RadioMenuItem(ThemeManager.label(density)); item.setUserData(density); item.setToggleGroup(densities);
            item.setOnAction(event -> {
                if (density != preferences.current().density()) savePreferences(preferences.setDensity(density), "Densidad visual: " + ThemeManager.label(density));
            });
            densityMenu.getItems().add(item);
        }
        button.getItems().addAll(profileSummary, new SeparatorMenuItem(), edit, densityMenu, appearance);
        return button;
    }

    private void changeTheme(AppTheme theme) {
        if (theme == preferences.current().theme()) return;
        savePreferences(preferences.setTheme(theme), "Tema: " + ThemeManager.label(theme));
    }

    private void savePreferences(CompletableFuture<UserPreferences> save, String success) {
        cycleTheme.setDisable(true); profile.setDisable(true);
        save.whenComplete((value, error) -> Platform.runLater(() -> {
            if (disposed) return;
            cycleTheme.setDisable(false); profile.setDisable(false);
            if (error == null) {
                refreshProfile(); ThemeManager.apply(getScene(), value.theme(), value.density()); footer.setText(success);
            } else {
                refreshProfile(); footer.setText("No se guardaron los cambios de preferencias.");
                UiSupport.error(window(), "No se pudieron guardar las preferencias", preferences.loadWarning().isEmpty()
                        ? "Revisa los permisos y el espacio disponible. Se conserva la configuración anterior." : preferences.loadWarning());
            }
        }));
    }

    private void refreshProfile() {
        var value = preferences.current();
        avatar.setText(value.initials());
        profileSummary.setText(value.displayName() + " · Perfil local");
        profile.setAccessibleText("Perfil de " + value.displayName());
        profile.setTooltip(new Tooltip(value.displayName() + " · Abrir menú de perfil"));
        cycleTheme.setAccessibleText("Tema actual: " + ThemeManager.label(value.theme()) + ". Cambiar al siguiente tema");
        cycleTheme.setTooltip(new Tooltip("Actual: " + ThemeManager.label(value.theme()) + " · Siguiente: " + ThemeManager.label(value.theme().next())));
        appearance.getItems().forEach(item -> ((RadioMenuItem) item).setSelected(item.getUserData() == value.theme()));
        densityMenu.getItems().forEach(item -> ((RadioMenuItem) item).setSelected(item.getUserData() == value.density()));
    }

    private void importCurl() {
        new CurlImportDialog(window(), preparer).showAndWait().ifPresent(result -> {
            open(result.request(), selectedCollectionId());
            active().markDirty();
            footer.setText("cURL importado · Revisa la petición y pulsa Enviar o Guardar. " + String.join(" ", result.notices()));
        });
    }

    private VBox createCollections() {
        var title = new Label("Colecciones"); title.getStyleClass().add("panel-title");
        var add = UiSupport.button("+", "Crear colección", this::addCollection);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        var heading = new HBox(8, title, spacer, add); heading.setAlignment(Pos.CENTER_LEFT);
        filter.setPromptText("Buscar peticiones…"); filter.textProperty().addListener((obs, old, value) -> rebuildTree());
        tree.setShowRoot(false); tree.setId("collections-tree");
        tree.setCellFactory(view -> new TreeCell<>() {
            @Override protected void updateItem(Entry entry, boolean empty) {
                super.updateItem(entry, empty);
                setText(null); setGraphic(null); setContextMenu(null);
                if (empty || entry == null) return;
                if (entry.request() == null) {
                    setText(entry.collection().name()); getStyleClass().remove("request-cell");
                } else {
                    var method = new Label(entry.request().method()); method.getStyleClass().add("tree-method");
                    if (!entry.request().method().equals("GET")) method.setStyle("-fx-text-fill: #b87924;");
                    var label = new Label(entry.request().name()); label.setMaxWidth(160);
                    var row = new HBox(8, method, label); row.setAlignment(Pos.CENTER_LEFT); setGraphic(row);
                }
                setContextMenu(contextMenu(entry));
            }
        });
        tree.setOnMouseClicked(event -> { if (event.getClickCount() == 2) openSelected(); });
        tree.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) openSelected(); });
        var caption = new Label("Doble clic para abrir.\nClic derecho para organizar."); caption.getStyleClass().add("muted"); caption.setWrapText(true);
        var demo = UiSupport.button("Probar GitHub", "Cargar la colección de ejemplo y ejecutar su primera petición pública", this::runGitHubDemo);
        demo.setId("github-demo");
        demo.setMaxWidth(Double.MAX_VALUE);
        var box = new VBox(14, heading, filter, demo, tree, caption); box.setPadding(new Insets(18, 12, 18, 12));
        box.setMinWidth(175); box.setPrefWidth(230); box.getStyleClass().add("collections-pane"); VBox.setVgrow(tree, Priority.ALWAYS);
        return box;
    }

    private ContextMenu contextMenu(Entry entry) {
        var menu = new ContextMenu();
        if (entry.request() == null) {
            var add = new MenuItem("Nueva petición"); add.setOnAction(e -> open(RequestDefinition.blank(), entry.collection().id()));
            var rename = new MenuItem("Renombrar colección"); rename.setOnAction(e -> prompt("Renombrar colección", entry.collection().name()).ifPresent(name -> mutate(() -> workspace.renameCollection(entry.collection().id(), name))));
            var delete = new MenuItem("Eliminar colección"); delete.setOnAction(e -> {
                if (confirm("Eliminar «" + entry.collection().name() + "» y sus peticiones guardadas?")) mutate(() -> workspace.deleteCollection(entry.collection().id()));
            });
            menu.getItems().addAll(add, rename, delete);
        } else {
            var open = new MenuItem("Abrir petición"); open.setOnAction(e -> open(entry.request(), entry.collection().id()));
            var duplicate = new MenuItem("Duplicar petición"); duplicate.setOnAction(e -> {
                var copy = entry.request().duplicate();
                if (mutate(() -> workspace.saveRequest(entry.collection().id(), copy))) open(copy, entry.collection().id());
            });
            var delete = new MenuItem("Eliminar petición guardada"); delete.setOnAction(e -> {
                if (confirm("Eliminar «" + entry.request().name() + "» de la colección?")) mutate(() -> workspace.deleteRequest(entry.request().id()));
            });
            menu.getItems().addAll(open, duplicate, delete);
        }
        return menu;
    }

    private void updatePanels() {
        layout.getItems().clear();
        if (showCollections.isSelected()) layout.getItems().add(collections);
        layout.getItems().add(requests);
        if (showCurl.isSelected()) layout.getItems().add(curl);
        SplitPane.setResizableWithParent(collections, false); SplitPane.setResizableWithParent(curl, false);
        if (showCollections.isSelected() && showCurl.isSelected()) layout.setDividerPositions(0.17, 0.77);
        else if (showCollections.isSelected()) layout.setDividerPositions(0.20);
        else if (showCurl.isSelected()) layout.setDividerPositions(0.76);
    }

    private void rebuildTree() {
        String query = filter.getText().trim().toLowerCase(Locale.ROOT);
        var root = new TreeItem<Entry>();
        for (var collection : workspace.workspace().collections()) {
            var item = new TreeItem<>(new Entry(collection, null)); item.setExpanded(true);
            for (var request : collection.requests()) {
                if (query.isEmpty() || (collection.name() + " " + request.name() + " " + request.url() + " " + request.method()).toLowerCase(Locale.ROOT).contains(query)) {
                    item.getChildren().add(new TreeItem<>(new Entry(collection, request)));
                }
            }
            if (query.isEmpty() || !item.getChildren().isEmpty() || collection.name().toLowerCase(Locale.ROOT).contains(query)) root.getChildren().add(item);
        }
        tree.setRoot(root);
    }

    private void openSelected() {
        var item = tree.getSelectionModel().getSelectedItem();
        if (item != null && item.getValue().request() != null) open(item.getValue().request(), item.getValue().collection().id());
    }

    public void runGitHubDemo() {
        try {
            var demo = workspace.addGitHubDemo();
            filter.clear();
            rebuildTree();
            if (!showCollections.isSelected()) {
                showCollections.setSelected(true);
                updatePanels();
            }
            if (demo.requests().isEmpty()) {
                footer.setText("Demo GitHub está vacía. Elimina esa colección y pulsa Probar GitHub para restaurarla.");
                return;
            }
            open(demo.requests().getFirst(), demo.id());
            footer.setText("Demo GitHub · Abre otra petición con doble clic y pulsa Enviar.");
            active().send();
        } catch (IOException e) {
            UiSupport.error(window(), "No se pudo cargar la colección de demostración", e.getMessage());
        }
    }

    private String selectedCollectionId() {
        var item = tree.getSelectionModel().getSelectedItem();
        return item == null ? null : item.getValue().collection().id();
    }

    public void open(RequestDefinition definition, String collectionId) {
        for (Tab tab : requests.getTabs()) {
            if (((RequestEditor) tab.getContent()).requestId().equals(definition.id())) { requests.getSelectionModel().select(tab); return; }
        }
        var tab = new Tab();
        var editor = new RequestEditor(definition, collectionId, http, preparer, () -> {
            if (tab.getContent() instanceof RequestEditor current) {
                tab.setText(current.title());
                if (active() == current) curl.update(current);
            }
        }, () -> save((RequestEditor) tab.getContent()));
        tab.setContent(editor); tab.setText(editor.title());
        tab.setOnCloseRequest(event -> { if (!canClose(editor)) event.consume(); else editor.dispose(); });
        tab.setOnClosed(event -> { if (requests.getTabs().isEmpty()) open(RequestDefinition.blank(), null); });
        requests.getTabs().add(tab); requests.getSelectionModel().select(tab);
    }

    private void closeActive() {
        var tab = requests.getSelectionModel().getSelectedItem();
        if (tab != null && canClose((RequestEditor) tab.getContent())) {
            ((RequestEditor) tab.getContent()).dispose(); requests.getTabs().remove(tab);
            if (requests.getTabs().isEmpty()) open(RequestDefinition.blank(), null);
        }
    }

    private RequestEditor active() {
        var tab = requests.getSelectionModel().getSelectedItem();
        return tab == null ? null : (RequestEditor) tab.getContent();
    }

    private boolean save(RequestEditor editor) {
        if (editor == null) return false;
        if (workspace.workspace().collections().isEmpty()) {
            addCollection();
            if (workspace.workspace().collections().isEmpty()) return false;
        }
        var dialog = new Dialog<SaveTarget>(); dialog.initOwner(window()); ThemeManager.styleDialog(dialog); dialog.setTitle("Guardar petición");
        var name = new TextField(editor.definition().name()); name.setPrefWidth(310);
        var collection = new ComboBox<RequestCollection>(); collection.getItems().setAll(workspace.workspace().collections());
        collection.setMaxWidth(Double.MAX_VALUE);
        collection.setConverter(new StringConverter<>() {
            @Override public String toString(RequestCollection value) { return value == null ? "" : value.name(); }
            @Override public RequestCollection fromString(String text) { throw new UnsupportedOperationException(); }
        });
        collection.setValue(collection.getItems().stream().filter(c -> c.id().equals(editor.collectionId())).findFirst().orElse(collection.getItems().getFirst()));
        var grid = new GridPane(); grid.setHgap(12); grid.setVgap(14); grid.setPadding(new Insets(16));
        grid.getStyleClass().add("settings-grid");
        grid.addRow(0, new Label("Nombre"), name); grid.addRow(1, new Label("Colección"), collection);
        var note = new Label("Se guarda localmente en texto plano, incluida la autorización."); note.setWrapText(true); note.getStyleClass().add("muted"); grid.add(note, 0, 2, 2, 1);
        dialog.getDialogPane().setContent(grid);
        var save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(save).disableProperty().bind(name.textProperty().isEmpty().or(collection.valueProperty().isNull()));
        dialog.setResultConverter(button -> button == save ? new SaveTarget(name.getText().trim(), collection.getValue().id()) : null);
        var result = dialog.showAndWait();
        if (result.isEmpty() || result.get().name().isBlank()) return false;
        var target = result.get();
        if (!mutate(() -> workspace.saveRequest(target.collectionId(), editor.definition().withName(target.name())))) return false;
        editor.saved(target.name(), target.collectionId()); footer.setText("Guardado · " + target.name()); return true;
    }

    private void addCollection() { prompt("Nueva colección", "Mi colección").ifPresent(name -> mutate(() -> workspace.addCollection(name))); }

    private Optional<String> prompt(String title, String initial) {
        var dialog = new TextInputDialog(initial); dialog.initOwner(window()); ThemeManager.styleDialog(dialog); dialog.setTitle("Jocote"); dialog.setHeaderText(title); dialog.setContentText("Nombre:");
        return dialog.showAndWait().map(String::trim).filter(name -> !name.isBlank());
    }

    private boolean confirm(String message) {
        var dialog = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        dialog.initOwner(window()); ThemeManager.styleDialog(dialog); dialog.setTitle("Jocote"); dialog.setHeaderText("Confirmar eliminación");
        return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private boolean mutate(IoAction action) {
        try { action.run(); rebuildTree(); return true; }
        catch (IOException e) { UiSupport.error(window(), "No se pudieron guardar los cambios", e.getMessage()); return false; }
    }

    private boolean canClose(RequestEditor editor) {
        if (!editor.isDirty()) return true;
        var save = new ButtonType("Guardar", ButtonBar.ButtonData.YES);
        var discard = new ButtonType("Descartar", ButtonBar.ButtonData.NO);
        var alert = new Alert(Alert.AlertType.CONFIRMATION, "Hay cambios sin guardar en «" + editor.definition().name() + "».", save, discard, ButtonType.CANCEL);
        alert.initOwner(window()); ThemeManager.styleDialog(alert); alert.setTitle("Jocote"); alert.setHeaderText("Cambios sin guardar");
        var result = alert.showAndWait().orElse(ButtonType.CANCEL);
        return result == discard || result == save && save(editor);
    }

    public boolean requestClose() {
        for (var tab : new ArrayList<>(requests.getTabs())) if (!canClose((RequestEditor) tab.getContent())) return false;
        dispose(); return true;
    }

    public void dispose() { disposed = true; requests.getTabs().forEach(tab -> ((RequestEditor) tab.getContent()).dispose()); }
    private Window window() { return getScene() == null ? null : getScene().getWindow(); }
    private record Entry(RequestCollection collection, RequestDefinition request) { }
    private record SaveTarget(String name, String collectionId) { }
    @FunctionalInterface private interface IoAction { void run() throws IOException; }
}
