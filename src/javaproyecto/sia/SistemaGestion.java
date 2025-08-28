package javaproyecto.sia;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class SistemaGestion {
    private List<Votante> listaVotantes;
    private List<LocalVotacion> listaLocales;

    public SistemaGestion() {
        listaVotantes = new ArrayList<>();
        listaLocales = new ArrayList<>();
    }

    public void registrarVotante(Votante v) {
        listaVotantes.add(v);
    }

    public void registrarLocal(LocalVotacion l) {
        listaLocales.add(l);
    }

    public void asignarVotante(LocalVotacion local, Votante votante) {
        if (local.getListaVotantes().size() < local.getCapacidad()) {
            local.agregarVotante(votante);
            votante.setLocalAsignado(local);
        } else {
            System.out.println("No se pudo asignar, local lleno.");
        }
    }

    // Método futuro para autoasignación
    public void autoAsignar() {
        // Lógica para asignar según comuna y capacidad
    }

    public void mostrarReporte() {
        for (LocalVotacion local : listaLocales) {
            System.out.println("Local: " + local.getNombre() + " - Votantes asignados: " + local.getListaVotantes().size());
        }
    }
    
    public List<LocalVotacion> getListaLocales() {
        return Collections.unmodifiableList(listaLocales);
    }
}
