/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javaproyecto.sia;

/**
 *
 * @author franc
 */
public class JavaProyectoSIA {

    public static void main(String[] args) {
        // Crear el sistema de gestión 
        SistemaGestion sistema = new SistemaGestion();
        
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
    }
}
