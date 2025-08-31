package javaproyecto.sia;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import java.util.*;


public class SistemaGestion {
    private List<LocalVotacion> listaLocales = new ArrayList<>();
    private List<Votante> votantesPendientes = new ArrayList<>();

    public void cargarDatosIniciales() {
        // Registrar locales de votación de ejemplo
        LocalVotacion local1 = new LocalVotacion("L001", "Escuela Central", "Av. Principal 123", "Santiago", 3);
        LocalVotacion local2 = new LocalVotacion("L002", "Colegio Nacional", "Calle Secundaria 45", "Providencia", 2);
        this.registrarLocal(local1);
        this.registrarLocal(local2);

        // Registrar votantes de ejemplo (se van como pendientes)
        Votante v1 = new Votante("11111111-1", "Juan Pérez", "Calle 1", "Santiago", 30);
        Votante v2 = new Votante("22222222-2", "María Gómez", "Calle 2", "Santiago", 28);
        Votante v3 = new Votante("33333333-3", "Pedro Torres", "Calle 3", "Providencia", 40);
        this.registrarVotante(v1);
        this.registrarVotante(v2);
        this.registrarVotante(v3);
    }

    public void registrarLocal(LocalVotacion l) {
        listaLocales.add(l);
    }

    public void registrarVotante(Votante v) {
        votantesPendientes.add(v);
    }

    public void autoAsignar() {
        Iterator<Votante> it = votantesPendientes.iterator();

        while (it.hasNext()) {
            Votante votante = it.next();
            boolean asignado = false;

            for (LocalVotacion local : listaLocales) {
                boolean mismaComuna = votante.getComuna().equalsIgnoreCase(local.getComuna());
                boolean hayCapacidad = local.getCantidadVotantes() < local.getCapacidad();

                if (mismaComuna && hayCapacidad) {
                    local.agregarVotante(votante);
                    System.out.println(" > Votante '" + votante.getNombre() + "' asignado a -> " + local.getNombre());
                    it.remove(); // lo sacamos de pendientes
                    asignado = true;
                    break;
                }
            }

            if (!asignado) {
                System.out.println(" ! Votante '" + votante.getNombre() + "' no pudo ser asignado (sin cupo en su comuna).");
            }
        }
        System.out.println("Proceso de asignación finalizado.");
    }

    public void imprimirReporteGeneral() {
        System.out.println("\n===== REPORTE GENERAL DE LOCALES DE VOTACIÓN =====");
        if (listaLocales.isEmpty()) {
            System.out.println("No hay locales registrados en el sistema.");
            return;
        }

        for (LocalVotacion local : listaLocales) {
            local.mostrarInfoDetallada();
            System.out.println("-------------------------------------------------");
        }

        if (!votantesPendientes.isEmpty()) {
            System.out.println("===== VOTANTES PENDIENTES DE ASIGNACIÓN =====");
            for (Votante v : votantesPendientes) {
                System.out.println(" - " + v.getNombre() + " (" + v.getRut() + ")");
            }
        }
    }
}
