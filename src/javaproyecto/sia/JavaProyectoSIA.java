package javaproyecto.sia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;   


public class JavaProyectoSIA {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        SistemaGestion sistema = new SistemaGestion();
        
        sistema.cargarDatosIniciales();
                
        int opcion;
        
        do {
            System.out.println("\n===== MENÚ GESTIÓN DE ELECCIONES =====");
            System.out.println("1. Agregar Votante (pendiente de asignar)");
            System.out.println("2. Agregar Local de Votación");
            System.out.println("3. Realizar Asignación Automática de Votantes");
            System.out.println("4. Mostrar Reporte de Locales y Votantes");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
                opcion = 0;
            }

            switch (opcion) {
                case 1:
                    System.out.println("\n--- Registro de Nuevo Votante ---");
                    System.out.print("RUT: ");
                    String rut = reader.readLine();
                    
                    System.out.print("Nombre Completo: ");
                    String nombre = reader.readLine();
                    
                    System.out.print("Dirección: ");
                    String direccion = reader.readLine();
                    
                    System.out.print("Comuna: ");
                    String comuna = reader.readLine();
                    
                    System.out.print("Edad: ");
                    int edad = Integer.parseInt(reader.readLine());
                    
                    Votante nuevoVotante = new Votante(rut, nombre, direccion, comuna, edad);
                    
                    sistema.registrarVotante(nuevoVotante);
                    
                    System.out.println("¡Votante '" + nombre + "' registrado con éxito (pendiente de asignación)!");
                    break;
                case 2:
                    System.out.println("\n--- Registro de Nuevo Local de Votación ---");

                    System.out.print("Ingrese ID del Local (ej: L003): ");
                    String id = reader.readLine();

                    System.out.print("Ingrese Nombre del local: ");
                    String nombreLocal = reader.readLine();

                    System.out.print("Ingrese Dirección: ");
                    String direccionLocal = reader.readLine();

                    System.out.print("Ingrese Comuna: ");
                    String comunaLocal = reader.readLine();

                    System.out.print("Ingrese Capacidad máxima: ");
                    int capacidad = Integer.parseInt(reader.readLine());

                    LocalVotacion nuevoLocal = new LocalVotacion(id, nombreLocal, direccionLocal, comunaLocal, capacidad);

                    sistema.registrarLocal(nuevoLocal);

                    System.out.println("¡Local '" + nombreLocal + "' registrado con éxito!");
                    break;
                case 3:
                    System.out.println("\nIniciando asignación automática...");
                    sistema.autoAsignar();
                    break;
                case 4:
                    sistema.imprimirReporteGeneral();
                    break;
                case 5:
                    System.out.println("Saliendo del sistema. ¡Adiós!");
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, intente de nuevo.");
            }
        } while (opcion != 5); 

        reader.close(); 
    }
}



        /*     
        // Registrar locales de votación
        LocalVotacion local1 = new LocalVotacion("L001", "Escuela Central", "Av. Principal 123", "Santiago", 3);
        LocalVotacion local2 = new LocalVotacion("L002", "Colegio Nacional", "Calle Secundaria 45", "Providencia", 2);

        sistema.registrarLocal(local1);
        sistema.registrarLocal(local2);

        // Registrar votantes
        Votante v1 = new Votante("11111111-1", "Juan Pérez", "Calle 1", "Santiago", 30);
        Votante v2 = new Votante("22222222-2", "María Gómez", "Calle 2", "Santiago", 28);
        Votante v3 = new Votante("33333333-3", "Pedro Torres", "Calle 3", "Providencia", 40);
        Votante v4 = new Votante("44444444-4", "Lucía Rojas", "Calle 4", "Providencia", 22);
        Votante v5 = new Votante("55555555-5", "Carlos Díaz", "Calle 5", "Santiago", 50);

        sistema.registrarVotante(v1);
        sistema.registrarVotante(v2);
        sistema.registrarVotante(v3);
        sistema.registrarVotante(v4);
        sistema.registrarVotante(v5);

        // Asignar votantes a locales manualmente
        sistema.asignarVotante(local1, v1);
        sistema.asignarVotante(local1, v2);
        sistema.asignarVotante(local2, v3);
        sistema.asignarVotante(local2, v4);

        // Intentar asignar a local lleno
        sistema.asignarVotante(local2, v5); // Este debería dar mensaje de capacidad llena

        // Mostrar reporte
        System.out.println("\n===== REPORTE DE LOCALES =====");
        sistema.mostrarReporte();

        // Mostrar votantes por local
        System.out.println("\n===== DETALLE POR LOCAL =====");
        for (LocalVotacion local : sistema.getListaLocales()) {
            System.out.println("\nLocal: " + local.getNombre());
            for (Votante vot : local.getListaVotantes()) {
                System.out.println(" - " + vot.getNombre() + " (" + vot.getRut() + ")");
            }
        }

        // ========================
        // DEMOSTRACIÓN SOBRECARGA
        // ========================
        System.out.println("\n===== BÚSQUEDA DE VOTANTES (SOBRECARGA) =====");

        // Buscar por RUT
        Votante buscadoRut = sistema.buscarVotante("22222222-2");
        if (buscadoRut != null) {
            System.out.println("Encontrado por RUT: " + buscadoRut.getNombre());
        } else {
            System.out.println("No se encontró el votante por RUT.");
        }

        // Buscar por Nombre y Apellido
        Votante buscadoNombre = sistema.buscarVotante("Pedro", "Torres");
        if (buscadoNombre != null) {
            System.out.println("Encontrado por Nombre: " + buscadoNombre.getNombre() + " (" + buscadoNombre.getRut() + ")");
        } else {
            System.out.println("No se encontró el votante por Nombre.");
        }
    }

*/
