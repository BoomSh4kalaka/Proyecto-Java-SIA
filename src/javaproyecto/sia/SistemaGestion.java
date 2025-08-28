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

    // Asignar votante a un local por objeto
    public void asignarVotante(LocalVotacion local, Votante votante) {
        if (local.getListaVotantes().size() < local.getCapacidad()) {
            local.agregarVotante(votante);
            votante.setLocalAsignado(local);
        } else {
            System.out.println("No se pudo asignar, local lleno.");
        }
    }

    // Sobrecarga de asignar votantes
    public void asignarVotante(String idLocal, String rutVotante) {
        LocalVotacion localEncontrado = null;
        Votante votanteEncontrado = null;

        for (LocalVotacion local : listaLocales) {
            if (local.getIdLocal().equalsIgnoreCase(idLocal)) {
                localEncontrado = local;
                break;
            }
        }

        for (Votante v : listaVotantes) {
            if (v.getRut().equalsIgnoreCase(rutVotante)) {
                votanteEncontrado = v;
                break;
            }
        }

        if (localEncontrado != null && votanteEncontrado != null) {
            asignarVotante(localEncontrado, votanteEncontrado);
        } else {
            System.out.println("Error: Local o votante no encontrado.");
        }
    }

// Buscar por RUT
        public Votante buscarVotante(String rut) {
            for (Votante v : listaVotantes) {
                if (v.getRut().equalsIgnoreCase(rut)) {
                    return v;
        }
    }
                return null;
}

// Buscar por Nombre y Apellido separados
    public Votante buscarVotante(String nombre, String apellido) {
        for (Votante v : listaVotantes) {
            String[] partes = v.getNombre().split(" ");
            if (partes.length >= 2 && partes[0].equalsIgnoreCase(nombre) && partes[1].equalsIgnoreCase(apellido)) {
                return v;
            }
        }
        return null;
}
}