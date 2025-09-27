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
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Set;
import java.util.stream.Collectors;


import java.util.ArrayList;
import java.util.List;

public class SIAFxApp extends Application {

    // Instancia única compartida (simple)
    private static final SistemaGestion sistema = new SistemaGestion();

    // Componentes compartidos
    private final Label status = new Label("Listo.");
    
    // Referencias globales para refrescar
    private TableView<Votante> tvVotantes;
    private TableView<LocalVotacion> tvLocales;
    private TableView<Votante> tvPendientes;
    private TableView<LocalVotacion> tvLocalesAsignacion;
    private TableView<Votante> tvFiltros;
    private TextArea areaReporte;
    private ComboBox<String> comboComunas;



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
        root.setTop(buildToolbar());   
        root.setCenter(buildTabs());   
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

        areaReporte = new TextArea();
        areaReporte.setEditable(false);
        areaReporte.setWrapText(false);
        areaReporte.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12;");

        Button refrescar = new Button("Refrescar");
        refrescar.setOnAction(e -> areaReporte.setText(sistema.construirReporteGeneral()));

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
        VBox box = new VBox(8, barra, areaReporte);
        box.setPadding(new Insets(10));

        VBox.setVgrow(areaReporte, Priority.ALWAYS);

        areaReporte.setText(sistema.construirReporteGeneral());

        tab.setContent(box);
        return tab;
    }



    private HBox buildStatusBar() {
        HBox box = new HBox(status);
        box.setPadding(new Insets(6));
        return box;
    }

    private void setStatus(String s) { status.setText(s); }

    // ====== Tab: Locales ======
    private Tab tabLocales() {
        Tab tab = new Tab("Locales");
        tab.setClosable(false);
        
        tvLocales = new TableView<>();
        
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
        tvLocales.getColumns().addAll(cId, cNombre, cComuna, cCap, cAsig);

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

                // Ventana informativa usando toString()
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Local agregado");
                info.setHeaderText("Local agregado con éxito");
                info.setContentText(nuevo.toString()); // uso sobreescritura
                info.showAndWait();

                setStatus("Local agregado: " + nuevo);
                refrescarTablas();
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
            refrescarTablas();
        });

        Button bDel = new Button("Eliminar");
        bDel.setOnAction(e -> {
            LocalVotacion seleccionado = tvLocales.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                setStatus("Seleccione un local primero.");
                return;
            }

            // Confirmación (solo ID del local)
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText("¿Seguro desea eliminar este local?");
            confirm.setContentText("ID: " + seleccionado.getIdLocal());
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean ok = sistema.eliminarLocalPorId(seleccionado.getIdLocal());

                if (ok) {
                    refrescarTablas();

                    // Mensaje de éxito con todos los detalles (toString)
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Local eliminado");
                    info.setHeaderText("El local fue eliminado con éxito");
                    info.setContentText(seleccionado.toString());
                    info.showAndWait();

                    setStatus("Local eliminado: " + seleccionado.getNombre());
                } else {
                    setStatus("No se pudo eliminar el local.");
                }
            }
        });



        HBox form1 = new HBox(8, fId, fNombre, fDir, fComuna, fCap);
        HBox form2 = new HBox(8, bAdd, bMod, bDel);
        form1.setAlignment(Pos.CENTER_LEFT);
        form2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, tvLocales, form1, form2);
        box.setPadding(new Insets(10));

        tvLocales.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> {
            if (b == null) return;
            fId.setText(b.getIdLocal());
            fNombre.setText(b.getNombre());
            fDir.setText(b.getDireccion());
            fComuna.setText(b.getComuna());
            fCap.setText(String.valueOf(b.getCapacidad()));
        });

        // datos iniciales
        tvLocales.getItems().setAll(sistema.getListaLocales());

        tab.setContent(box);
        return tab;
    }

    // ====== Tab: Votantes ======
    private Tab tabVotantes() {
        Tab tab = new Tab("Votantes");
        tab.setClosable(false);

        tvVotantes = new TableView<>();
   
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
        tvVotantes.getColumns().addAll(cRut, cNom, cCom, cEdad, cEst);

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
                refrescarTablas();
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
                refrescarTablas();
            } catch (NumberFormatException ex) {
                setStatus("Edad inválida.");
            }
        });
        
        // === Botón Eliminar con confirmación ===
        Button bDel = new Button("Eliminar");
        bDel.setOnAction(e -> {
        String rut = fRut.getText().trim();
        if (rut.isEmpty()) {
            setStatus("Debe seleccionar un votante para eliminar.");
            return;
        }

        var res = sistema.buscarVotanteGlobalPorRut(rut);
        if (res == null) {
            setStatus("No existe un votante con ese RUT.");
            return;
        }

        Votante votante = res.getVotante();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que deseas eliminar al votante con RUT: " + rut + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = sistema.eliminarVotanteGlobalPorRut(rut); 
                if (ok) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Votante eliminado");
                    info.setHeaderText("Votante eliminado con éxito");
                    info.setContentText(votante.identificarse()); // uso sobreescritura
                    info.showAndWait();

                    setStatus("Votante eliminado.");
                    refrescarTablas();
                } else {
                    setStatus("No se pudo eliminar al votante.");
                }
            }
        });
    });

        tvVotantes.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> {
            if (b == null) return;
            fRut.setText(b.getRut());
            fNom.setText(b.getNombre());
            fDir.setText(b.getDireccion());
            fCom.setText(b.getComuna());
            fEdad.setText(String.valueOf(b.getEdad()));
        });

        HBox form = new HBox(8, fRut, fNom, fDir, fCom, fEdad, bReg, bMod, bDel);
        form.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, tvVotantes, form);
        box.setPadding(new Insets(10));

        tvVotantes.getItems().setAll(vistaVotantesGlobal());
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

        // ComboBox de comunas
        Set<String> setCom = sistema.getListaLocales().stream()
                .map(LocalVotacion::getComuna)
                .collect(Collectors.toSet());

        ObservableList<String> comunas = FXCollections.observableArrayList(setCom);
        comunas.add(0, "Todos"); // opción general

        comboComunas = new ComboBox<>(comunas);
        comboComunas.setValue("Todos");


        // Botón de autoasignar
        Button bAuto = new Button("Autoasignar");
        
        // Tabla de pendientes
        tvPendientes = new TableView<>();
    
        TableColumn<Votante, String> cRut = new TableColumn<>("RUT");
        TableColumn<Votante, String> cNom = new TableColumn<>("Nombre");
        TableColumn<Votante, String> cCom = new TableColumn<>("Comuna");
        TableColumn<Votante, Integer> cEdad = new TableColumn<>("Edad");

        cRut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRut()));
        cNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        cCom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComuna()));
        cEdad.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEdad()).asObject());

        cRut.setPrefWidth(140); cNom.setPrefWidth(200);
        cCom.setPrefWidth(160); cEdad.setPrefWidth(80);
        tvPendientes.getColumns().addAll(cRut, cNom, cCom, cEdad);
        tvPendientes.getItems().setAll(sistema.getVotantesPendientes());

        // Tabla de locales
        tvLocalesAsignacion = new TableView<>();
        
        TableColumn<LocalVotacion, String> cId = new TableColumn<>("ID");
        TableColumn<LocalVotacion, String> cNomLoc = new TableColumn<>("Nombre");
        TableColumn<LocalVotacion, String> cComLoc = new TableColumn<>("Comuna");
        TableColumn<LocalVotacion, Integer> cCap = new TableColumn<>("Capacidad");
        TableColumn<LocalVotacion, Integer> cOcup = new TableColumn<>("Ocupados");

        cId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdLocal()));
        cNomLoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        cComLoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComuna()));
        cCap.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCapacidad()).asObject());
        cOcup.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getVotantes().size()).asObject());

        cId.setPrefWidth(60); cNomLoc.setPrefWidth(200);
        cComLoc.setPrefWidth(160); cCap.setPrefWidth(100); cOcup.setPrefWidth(100);
        tvLocalesAsignacion.getColumns().addAll(cId, cNomLoc, cComLoc, cCap, cOcup);
        tvLocalesAsignacion.getItems().setAll(sistema.getListaLocales());

        // Acción del botón
        bAuto.setOnAction(e -> {
            String comunaSeleccionada = comboComunas.getValue();
            if (comunaSeleccionada == null) {
                setStatus("Seleccione una comuna o 'Todos'.");
                return;
            }

            if ("Todos".equals(comunaSeleccionada)) {
                sistema.autoAsignar();
            } else {
                sistema.autoAsignar(comunaSeleccionada);
            }

            // Refrescar tablas
            refrescarTablas();

            setStatus("Autoasignación completada.");
        });

        HBox top = new HBox(10, new Label("Comuna:"), comboComunas, bAuto);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, top, new Label("Pendientes:"), tvPendientes, new Label("Locales:"), tvLocalesAsignacion);
        box.setPadding(new Insets(10));

        tab.setContent(box);
        return tab;
    }






    // ====== Tab: Filtros (SIA2.5) ======
    private Tab tabFiltros() {
        Tab tab = new Tab("Filtros");
        tab.setClosable(false);

        TextField fRut = new TextField(); fRut.setPromptText("RUT");
        TextField fCom = new TextField(); fCom.setPromptText("Comuna");
        TextField fMin = new TextField(); fMin.setPromptText("Edad min");
        TextField fMax = new TextField(); fMax.setPromptText("Edad max");

        Button bPend = new Button("Buscar PENDIENTES");
        Button bAll = new Button("Buscar TODOS");

        tvFiltros = new TableView<>();
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
        tvFiltros.getColumns().addAll(cRut, cNom, cCom, cEdad, cEst);

        // Botón PENDIENTES
        bPend.setOnAction(e -> {
            try {
                String rut = fRut.getText().trim();
                if (rut.isEmpty()) rut = null;

                String comuna = fCom.getText().trim();
                if (comuna.isEmpty()) comuna = null;

                Integer min = null;
                if (!fMin.getText().trim().isEmpty()) {
                    min = Integer.parseInt(fMin.getText().trim());
                }

                Integer max = null;
                if (!fMax.getText().trim().isEmpty()) {
                    max = Integer.parseInt(fMax.getText().trim());
                }

                List<Votante> resultados = sistema.filtrarVotantes(rut, comuna, min, max, true, false);
                tvFiltros.getItems().setAll(resultados);
                setStatus("Filtro pendientes: " + resultados.size() + " resultado(s).");
            } catch (NumberFormatException ex) {
                setStatus("Edades inválidas.");
            }
        });

        // Botón TODOS
        bAll.setOnAction(e -> {
            try {
                String rut = fRut.getText().trim();
                if (rut.isEmpty()) rut = null;

                String comuna = fCom.getText().trim();
                if (comuna.isEmpty()) comuna = null;

                Integer min = null;
                if (!fMin.getText().trim().isEmpty()) {
                    min = Integer.parseInt(fMin.getText().trim());
                }

                Integer max = null;
                if (!fMax.getText().trim().isEmpty()) {
                    max = Integer.parseInt(fMax.getText().trim());
                }

                List<Votante> resultados = sistema.filtrarVotantes(rut, comuna, min, max, true, true);
                tvFiltros.getItems().setAll(resultados);
                setStatus("Filtro global: " + resultados.size() + " resultado(s).");
            } catch (NumberFormatException ex) {
                setStatus("Edades inválidas.");
            }
        });

        HBox filtros = new HBox(8, fRut, fCom, fMin, fMax, bPend, bAll);
        filtros.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(10, filtros, tvFiltros);
        box.setPadding(new Insets(10));
        tab.setContent(box);
        return tab;
    }
    
    private void refrescarTablas() {
        if (tvVotantes != null) {
            tvVotantes.getItems().setAll(vistaVotantesGlobal());
            tvVotantes.refresh();
        }
        if (tvLocales != null) {
            tvLocales.getItems().setAll(sistema.getListaLocales());
            tvLocales.refresh();
        }
        if (tvPendientes != null) {
            tvPendientes.getItems().setAll(sistema.getVotantesPendientes());
            tvPendientes.refresh();
        }
        if (tvLocalesAsignacion != null) {
            tvLocalesAsignacion.getItems().setAll(sistema.getListaLocales());
            tvLocalesAsignacion.refresh();
        }
        if (areaReporte != null) {
            areaReporte.setText(sistema.construirReporteGeneral());
        }
        
        if (comboComunas != null) {
        Set<String> setCom = sistema.getListaLocales().stream()
                .map(LocalVotacion::getComuna)
                .collect(Collectors.toSet());

            ObservableList<String> comunas = FXCollections.observableArrayList(setCom);
            comunas.add(0, "Todos");

            String seleccionActual = comboComunas.getValue();
            comboComunas.setItems(comunas);

            // Mantener selección si sigue siendo válida, si no volver a "Todos"
            if (seleccionActual != null && comunas.contains(seleccionActual)) {
                comboComunas.setValue(seleccionActual);
            } else {
                comboComunas.setValue("Todos");
            }
        }
    }



    public static void main(String[] args) {
        launch();
    }
}
