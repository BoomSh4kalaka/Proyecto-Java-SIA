package javaproyecto.sia;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SIAFxApp extends Application {

    // Instancia única compartida (simple)
    private static final SistemaGestion sistema = new SistemaGestion();

    // Componentes compartidos
    private final Label status = new Label("Listo.");

    // ====== Arranque ======
    @Override
    public void start(Stage stage) {
        // 1) CARGA AUTOMÁTICA desde CSV al iniciar
        try {
            GestorCSV.cargarLocales(sistema);
            GestorCSV.cargarVotantes(sistema);
            setStatus("Datos cargados al iniciar.");
        } catch (Exception e) {
            setStatus("No se pudieron cargar datos iniciales: " + e.getMessage());
        }

        BorderPane root = new BorderPane();
        root.setTop(buildToolbar());   // veremos abajo que la toolbar ya NO tendrá botones CSV
        root.setCenter(buildTabs());   // las pestañas se construyen con datos ya cargados
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("SIA – Sistema de Locales y Votantes (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // 2) GUARDADO AUTOMÁTICO al cerrar
        try {
            GestorCSV.guardarLocales(sistema);
            GestorCSV.guardarVotantes(sistema);
            System.out.println("Datos guardados al salir.");
        } catch (Exception e) {
            System.err.println("No se pudieron guardar los datos al salir: " + e.getMessage());
        }
    }

    private ToolBar buildToolbar() {
        Button btnSalir = new Button("Salir");
        btnSalir.setOnAction(e -> {
            // Al cerrar, automáticamente se llamará a stop() y se guardarán los CSV
            ((Stage) btnSalir.getScene().getWindow()).close();
        });

        return new ToolBar(
                new Label("SIA – Gestión"),
                new Separator(),
                btnSalir
        );
    }

    private TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(tabLocales());
        tabs.getTabs().add(tabVotantes());
        tabs.getTabs().add(tabAsignacion());
        tabs.getTabs().add(tabFiltros());
        tabs.getTabs().add(tabReporte());
        return tabs;
    }

    private Tab tabReporte() {
    Tab tab = new Tab("Reporte");
    tab.setClosable(false);

    TextArea area = new TextArea();
    area.setEditable(false);
    area.setWrapText(false);
    area.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12;");

    Button refrescar = new Button("Refrescar");
    refrescar.setOnAction(e -> area.setText(sistema.construirReporteGeneral()));

    Button guardar = new Button("Guardar TXT");
// (Alternativa con FileChooser)
    guardar.setOnAction(e -> {
        try {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Guardar reporte TXT");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Texto (*.txt)", "*.txt"));
            fc.setInitialFileName("reporte_sia.txt");
            var file = fc.showSaveDialog(guardar.getScene().getWindow());
            if (file != null) {
                sistema.guardarReporteTxt(file.toPath());
                setStatus("Reporte guardado en: " + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            setStatus("No se pudo guardar el reporte: " + ex.getMessage());
        }
    });


    HBox barra = new HBox(8, refrescar, guardar);
    VBox box = new VBox(8, barra, area);
    box.setPadding(new Insets(10));

    // Que el área crezca y no tengas que scrollear tanto
    VBox.setVgrow(area, Priority.ALWAYS);

    // Mostrar de inmediato
    area.setText(sistema.construirReporteGeneral());

    tab.setContent(box);
    return tab;
}



    private HBox buildStatusBar() {
        HBox box = new HBox(status);
        box.setPadding(new Insets(6));
        return box;
    }

    private void setStatus(String s) { status.setText(s); }

    // ====== Tab: Locales (CRUD nivel 1) ======
    private Tab tabLocales() {
        Tab tab = new Tab("Locales");
        tab.setClosable(false);

        TableView<LocalVotacion> tv = new TableView<>();
        TableColumn<LocalVotacion, String> cId = new TableColumn<>("ID");
        TableColumn<LocalVotacion, String> cNombre = new TableColumn<>("Nombre");
        TableColumn<LocalVotacion, String> cComuna = new TableColumn<>("Comuna");
        TableColumn<LocalVotacion, Integer> cCap = new TableColumn<>("Capacidad");
        TableColumn<LocalVotacion, Integer> cAsig = new TableColumn<>("Asignados");
        cId.setPrefWidth(140); cNombre.setPrefWidth(240); cComuna.setPrefWidth(160);
        cCap.setPrefWidth(120); cAsig.setPrefWidth(120);

        cId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdLocal()));
        cNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        cComuna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComuna()));
        cCap.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCapacidad()).asObject());
        cAsig.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidadVotantes()).asObject());
        tv.getColumns().addAll(cId, cNombre, cComuna, cCap, cAsig);

        // Formulario
        TextField fId = new TextField(); fId.setPromptText("ID");
        TextField fNombre = new TextField(); fNombre.setPromptText("Nombre");
        TextField fDir = new TextField(); fDir.setPromptText("Dirección");
        TextField fComuna = new TextField(); fComuna.setPromptText("Comuna");
        TextField fCap = new TextField(); fCap.setPromptText("Capacidad (entero)");

        Button bAdd = new Button("Agregar");
        bAdd.setOnAction(e -> {
            try {
                String id = fId.getText().trim();
                String nom = fNombre.getText().trim();
                String dir = fDir.getText().trim();
                String com = fComuna.getText().trim();
                int cap = Integer.parseInt(fCap.getText().trim());
                LocalVotacion nuevo = new LocalVotacion(id, nom, dir, com, cap);
                sistema.registrarLocal(nuevo); // puede lanzar IdLocalDuplicadoException
                setStatus("Local agregado.");
                tv.getItems().setAll(sistema.getListaLocales());
            } catch (NumberFormatException ex) {
                setStatus("Capacidad inválida.");
            } catch (IdLocalDuplicadoException ex) {
                setStatus(ex.getMessage());
            }
        });

        Button bMod = new Button("Modificar");
        bMod.setOnAction(e -> {
            String id = fId.getText().trim();
            Integer nCap = null;
            if (!fCap.getText().trim().isEmpty()) {
                try { nCap = Integer.parseInt(fCap.getText().trim()); }
                catch (NumberFormatException ex) { setStatus("Capacidad inválida."); return; }
            }
            boolean ok = sistema.modificarLocal(id, fNombre.getText(), fDir.getText(), fComuna.getText(), nCap);
            setStatus(ok ? "Local modificado." : "No se pudo modificar (ID inex. o capacidad < asignados).");
            tv.getItems().setAll(sistema.getListaLocales());
        });

        Button bDel = new Button("Eliminar");
        bDel.setOnAction(e -> {
            boolean ok = sistema.eliminarLocalPorId(fId.getText().trim());
            setStatus(ok ? "Local eliminado (votantes a pendientes)." : "ID no encontrado.");
            tv.getItems().setAll(sistema.getListaLocales());
        });

        HBox form1 = new HBox(8, fId, fNombre, fDir, fComuna, fCap);
        HBox form2 = new HBox(8, bAdd, bMod, bDel);
        form1.setAlignment(Pos.CENTER_LEFT);
        form2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, tv, form1, form2);
        box.setPadding(new Insets(10));

        // Selección → llena form
        tv.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> {
            if (b == null) return;
            fId.setText(b.getIdLocal());
            fNombre.setText(b.getNombre());
            fDir.setText(b.getDireccion());
            fComuna.setText(b.getComuna());
            fCap.setText(String.valueOf(b.getCapacidad()));
        });

        // datos iniciales
        tv.getItems().setAll(sistema.getListaLocales());

        tab.setContent(box);
        return tab;
    }

    // ====== Tab: Votantes (altas y vista global) ======
    private Tab tabVotantes() {
        Tab tab = new Tab("Votantes");
        tab.setClosable(false);

        TableView<Votante> tv = new TableView<>();
        TableColumn<Votante, String> cRut = new TableColumn<>("RUT");
        TableColumn<Votante, String> cNom = new TableColumn<>("Nombre");
        TableColumn<Votante, String> cCom = new TableColumn<>("Comuna");
        TableColumn<Votante, Integer> cEdad = new TableColumn<>("Edad");
        TableColumn<Votante, String> cEst = new TableColumn<>("Estado");
        cRut.setPrefWidth(140); cNom.setPrefWidth(260); cCom.setPrefWidth(160); cEdad.setPrefWidth(80); cEst.setPrefWidth(220);

        cRut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRut()));
        cNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        cCom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComuna()));
        cEdad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEdad()).asObject());
        cEst.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLocalAsignado() == null ? "PENDIENTE" : "Asignado a " + c.getValue().getLocalAsignado().getNombre()
        ));
        tv.getColumns().addAll(cRut, cNom, cCom, cEdad, cEst);

        TextField fRut = new TextField(); fRut.setPromptText("RUT (no se modifica)");
        TextField fNom = new TextField(); fNom.setPromptText("Nombre completo");
        TextField fDir = new TextField(); fDir.setPromptText("Dirección");
        TextField fCom = new TextField(); fCom.setPromptText("Comuna");
        TextField fEdad = new TextField(); fEdad.setPromptText("Edad");

        Button bReg = new Button("Registrar (pendiente)");
        bReg.setOnAction(e -> {
            try {
                int edad = Integer.parseInt(fEdad.getText().trim());
                Votante v = new Votante(
                        fRut.getText().trim(),
                        fNom.getText().trim(),
                        fDir.getText().trim(),
                        fCom.getText().trim(),
                        edad
                );
                sistema.registrarVotante(v); // puede lanzar RutDuplicadoException
                setStatus("Votante registrado (pendiente).");
                tv.getItems().setAll(vistaVotantesGlobal());
            } catch (NumberFormatException ex) {
                setStatus("Edad inválida.");
            } catch (RutDuplicadoException ex) {
                setStatus(ex.getMessage());
            }
        });

        Button bMod = new Button("Modificar");
        bMod.setOnAction(e -> {
            try {
                Integer nEdad = fEdad.getText().trim().isEmpty() ? null : Integer.parseInt(fEdad.getText().trim());
                boolean ok = sistema.modificarVotantePorRut(
                        fRut.getText().trim(),
                        fNom.getText(),
                        fDir.getText(),
                        fCom.getText(),
                        nEdad
                );
                setStatus(ok ? "Votante modificado." : "No existe un votante con ese RUT.");
                tv.getItems().setAll(vistaVotantesGlobal());
            } catch (NumberFormatException ex) {
                setStatus("Edad inválida.");
            }
        });

        // Selección de fila → carga al formulario
        tv.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> {
            if (b == null) return;
            fRut.setText(b.getRut());
            fNom.setText(b.getNombre());
            fDir.setText(b.getDireccion());
            fCom.setText(b.getComuna());
            fEdad.setText(String.valueOf(b.getEdad()));
        });

        HBox form = new HBox(8, fRut, fNom, fDir, fCom, fEdad, bReg, bMod);
        form.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, tv, form);
        box.setPadding(new Insets(10));

        tv.getItems().setAll(vistaVotantesGlobal());
        tab.setContent(box);
        return tab;
    }

    private List<Votante> vistaVotantesGlobal() {
        List<Votante> all = new ArrayList<>();
        for (LocalVotacion l : sistema.getListaLocales()) all.addAll(l.getVotantes());
        all.addAll(sistema.getVotantesPendientes());
        return all;
    }

    // ====== Tab: Asignación ======
    private Tab tabAsignacion() {
        Tab tab = new Tab("Asignación");
        tab.setClosable(false);
        Button b = new Button("Autoasignar pendientes por comuna");
        Label msg = new Label();
        b.setOnAction(e -> {
            sistema.autoAsignar(); // dentro ya manejas CapacidadAgotadaException en try–catch
            msg.setText("Autoasignación completada.");
            setStatus("Autoasignación completada.");
        });
        VBox box = new VBox(10, b, msg);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);
        tab.setContent(box);
        return tab;
    }

    // ====== Tab: Filtros (SIA2.5) ======
    private Tab tabFiltros() {
        Tab tab = new Tab("Filtros");
        tab.setClosable(false);

        TextField fCom = new TextField(); fCom.setPromptText("Comuna");
        TextField fMin = new TextField(); fMin.setPromptText("Edad min");
        TextField fMax = new TextField(); fMax.setPromptText("Edad max");
        Button bPend = new Button("Buscar PENDIENTES");
        Button bAll = new Button("Buscar TODOS");

        TableView<Votante> tv = new TableView<>();
        TableColumn<Votante, String> cRut = new TableColumn<>("RUT");
        TableColumn<Votante, String> cNom = new TableColumn<>("Nombre");
        TableColumn<Votante, String> cCom = new TableColumn<>("Comuna");
        TableColumn<Votante, Integer> cEdad = new TableColumn<>("Edad");
        TableColumn<Votante, String> cEst = new TableColumn<>("Estado");
        cRut.setPrefWidth(140); cNom.setPrefWidth(260); cCom.setPrefWidth(160); cEdad.setPrefWidth(80); cEst.setPrefWidth(220);

        cRut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRut()));
        cNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        cCom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComuna()));
        cEdad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEdad()).asObject());
        cEst.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLocalAsignado() == null ? "PENDIENTE" : "Asignado a " + c.getValue().getLocalAsignado().getNombre()
        ));
        tv.getColumns().addAll(cRut, cNom, cCom, cEdad, cEst);

        bPend.setOnAction(e -> {
            try {
                String comuna = fCom.getText().trim();
                int min = Integer.parseInt(fMin.getText().trim());
                int max = Integer.parseInt(fMax.getText().trim());
                tv.getItems().setAll(sistema.filtrarPendientesPorComunaYEdad(comuna, min, max));
                setStatus("Filtro pendientes: " + tv.getItems().size() + " resultado(s).");
            } catch (NumberFormatException ex) {
                setStatus("Edades inválidas.");
            }
        });

        bAll.setOnAction(e -> {
            try {
                String comuna = fCom.getText().trim();
                int min = Integer.parseInt(fMin.getText().trim());
                int max = Integer.parseInt(fMax.getText().trim());
                tv.getItems().setAll(sistema.filtrarTodosPorComunaYEdad(comuna, min, max));
                setStatus("Filtro global: " + tv.getItems().size() + " resultado(s).");
            } catch (NumberFormatException ex) {
                setStatus("Edades inválidas.");
            }
        });

        HBox filtros = new HBox(8, fCom, fMin, fMax, bPend, bAll);
        filtros.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(10, filtros, tv);
        box.setPadding(new Insets(10));
        tab.setContent(box);
        return tab;
    }

    public static void main(String[] args) {
        launch();
    }
}
