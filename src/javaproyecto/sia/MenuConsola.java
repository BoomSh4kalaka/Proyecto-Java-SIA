/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaproyecto.sia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class MenuConsola {

    private final SistemaGestion sistema;
    private final GestorCSV gestor;
    private final BufferedReader reader;

    public MenuConsola(SistemaGestion sistema, GestorCSV gestor) {
        this.sistema = sistema;
        this.gestor = gestor;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void iniciar() {
        int opcion = -1;
        do {
            imprimirMenuPrincipal();
            try {
                String linea = reader.readLine();
                if (linea == null) break; // Fin de la entrada
                opcion = Integer.parseInt(linea);
                procesarOpcionPrincipal(opcion);
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
            } catch (IOException e) {
                System.out.println("Error de entrada/salida: " + e.getMessage());
                break; // Salir en caso de error grave
            }
        } while (opcion != 0);

        try {
            reader.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar el lector de entrada.");
        }
    }

    private void imprimirMenuPrincipal() {
        System.out.println("\n===== MENÚ PRINCIPAL =====");
        System.out.println("1. Gestión de Locales");
        System.out.println("2. Gestión de Votantes");
        System.out.println("3. Reportes y Filtros");
        System.out.println("4. Realizar Asignación Automática");
        System.out.println("0. Salir y Guardar");
        System.out.print("Seleccione una opción: ");
    }

    private void procesarOpcionPrincipal(int opcion) throws IOException {
        switch (opcion) {
            case 1:
                mostrarSubMenuLocales();
                break;
            case 2:
                mostrarSubMenuVotantes();
                break;
            case 3:
                sistema.imprimirReporteGeneral();
                break;
            case 4:
                System.out.println("\nIniciando asignación automática...");
                sistema.autoAsignar();
                break;
            case 0:
                System.out.println("Saliendo del sistema. ¡Adiós!");
                break;
            default:
                System.out.println("Opción no válida.");
        }
    }

    // --- SUB-MENÚS ---

    private void mostrarSubMenuLocales() throws IOException {
        int opcion;
        do {
            System.out.println("\n--- Gestión de Locales ---");
            System.out.println("1. Agregar Nuevo Local");
            System.out.println("2. Modificar Local Existente");
            System.out.println("3. Eliminar Local");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            opcion = leerOpcion();
            
            switch (opcion) {
                case 1: agregarLocal(); break;
                case 2: modificarLocal(); break;
                case 3: eliminarLocal(); break;
                case 0: System.out.println("Volviendo..."); break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void mostrarSubMenuVotantes() throws IOException {
         int opcion;
        do {
            System.out.println("\n--- Gestión de Votantes ---");
            System.out.println("1. Registrar Nuevo Votante (a pendientes)");
            System.out.println("2. Modificar Votante Existente");
            System.out.println("3. Eliminar Votante");
            System.out.println("4. Filtrar Votantes por Comuna y Edad");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            opcion = leerOpcion();
            
            switch (opcion) {
                case 1: agregarVotante(); break;
                case 2: modificarVotante(); break;
                case 3: eliminarVotante(); break;
                case 4: filtrarVotantesGlobal(); break;
                case 0: System.out.println("Volviendo..."); break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }
    
    // --- MÉTODOS DE LÓGICA DE INTERFAZ ---

    private int leerOpcion() {
        try {
            return Integer.parseInt(reader.readLine());
        } catch (Exception e) {
            System.out.println("Entrada inválida.");
            return -1;
        }
    }
    
    private void agregarVotante() throws IOException {
        System.out.println("\n--- Registro de Nuevo Votante ---");
        try {
            System.out.print("RUT: "); String rut = reader.readLine();
            System.out.print("Nombre Completo: "); String nombre = reader.readLine();
            System.out.print("Dirección: "); String direccion = reader.readLine();
            System.out.print("Comuna: "); String comuna = reader.readLine();
            System.out.print("Edad: "); int edad = Integer.parseInt(reader.readLine());
            
            Votante nuevoVotante = new Votante(rut, nombre, direccion, comuna, edad);
            sistema.registrarVotante(nuevoVotante);
            System.out.println("¡Votante registrado con éxito (pendiente de asignación)!");
        } catch (NumberFormatException e) {
            System.out.println("Error: la edad debe ser un número.");
        } catch (RutDuplicadoException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void modificarVotante() throws IOException {
        System.out.println("\n--- Modificación de Votante ---");
        System.out.print("Ingrese el RUT del votante a modificar: ");
        String rut = reader.readLine();
        // La lógica de modificación se mantiene en SistemaGestion para reutilización
        // (No implementada en este ejemplo, pero seguiría el mismo patrón)
        System.out.println("Funcionalidad de modificar votante no implementada en este menú.");
    }
    
    private void eliminarVotante() throws IOException {
        System.out.println("\n--- Eliminación de Votante ---");
        System.out.print("Ingrese el RUT del votante a eliminar: ");
        String rut = reader.readLine();
        String mensaje = sistema.eliminarVotanteGlobal(rut);
        System.out.println(mensaje);
    }
    
    private void agregarLocal() throws IOException {
         System.out.println("\n--- Registro de Nuevo Local ---");
        try {
            System.out.print("ID (ej: L003): "); String id = reader.readLine();
            System.out.print("Nombre: "); String nombre = reader.readLine();
            System.out.print("Dirección: "); String direccion = reader.readLine();
            System.out.print("Comuna: "); String comuna = reader.readLine();
            System.out.print("Capacidad máxima: "); int capacidad = Integer.parseInt(reader.readLine());
            
            LocalVotacion nuevoLocal = new LocalVotacion(id, nombre, direccion, comuna, capacidad);
            try {
                sistema.registrarLocal(nuevoLocal);
                System.out.println("¡Local '" + nombre + "' registrado con éxito!");
            } catch (IdLocalDuplicadoException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: la capacidad debe ser un número.");
        }
    }

    private void modificarLocal() throws IOException {
        System.out.println("\n--- Modificar Local ---");
        System.out.print("Ingrese ID del Local a modificar: ");
        String idMod = reader.readLine().trim();

        System.out.print("Nuevo nombre (Enter para mantener): "); String nNombre = reader.readLine();
        System.out.print("Nueva dirección (Enter para mantener): "); String nDir = reader.readLine();
        System.out.print("Nueva comuna (Enter para mantener): "); String nCom = reader.readLine();
        System.out.print("Nueva capacidad (Enter para mantener): "); String capStr = reader.readLine().trim();
        Integer nCap = capStr.isBlank() ? null : Integer.parseInt(capStr);

        if (sistema.modificarLocal(idMod, nNombre, nDir, nCom, nCap)) {
            System.out.println("Local modificado con éxito.");
        } else {
            System.out.println("No se pudo modificar (ID no existe o capacidad inválida).");
        }
    }
    
    private void eliminarLocal() throws IOException {
        System.out.println("\n--- Eliminar Local ---");
        System.out.print("Ingrese ID del Local a eliminar: ");
        String idDel = reader.readLine().trim();
        if (sistema.eliminarLocalPorId(idDel)) {
            System.out.println("Local eliminado. Sus votantes fueron movidos a pendientes.");
        } else {
            System.out.println("No se encontró un local con ese ID.");
        }
    }
    
    private void filtrarVotantesGlobal() throws IOException {
        System.out.println("\n--- Filtro GLOBAL de Votantes ---");
        try {
            System.out.print("Comuna: "); String comuna = reader.readLine().trim();
            System.out.print("Edad mínima: "); int min = Integer.parseInt(reader.readLine().trim());
            System.out.print("Edad máxima: "); int max = Integer.parseInt(reader.readLine().trim());
            
            List<Votante> resultado = sistema.filtrarTodosPorComunaYEdad(comuna, min, max);
            if (resultado.isEmpty()) {
                System.out.println("No se encontraron votantes con ese criterio.");
            } else {
                System.out.println("Votantes encontrados (" + resultado.size() + "):");
                resultado.forEach(v -> {
                    String estado = v.getLocalAsignado() == null ? "PENDIENTE" : "Asignado a " + v.getLocalAsignado().getNombre();
                    System.out.println(" - " + v.getNombre() + ", " + v.getEdad() + " años (" + estado + ")");
                });
            }
        } catch(NumberFormatException e) {
            System.out.println("Error: la edad debe ser un número.");
        }
    }
}