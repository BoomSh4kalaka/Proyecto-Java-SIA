package javaproyecto.sia;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import java.util.*;


public class SistemaGestion {
    private List<LocalVotacion> listaLocales = new ArrayList<>();
    private List<Votante> votantesPendientes = new ArrayList<>();
    
        // ===== Resultado de búsqueda (nivel 1 o 2) =====
    public static class ResultadoBusquedaVotante {
        private final Votante votante;
        private final LocalVotacion local; // null si está pendiente
        private final boolean esPendiente;

        public ResultadoBusquedaVotante(Votante votante, LocalVotacion local, boolean esPendiente) {
            this.votante = votante;
            this.local = local;
            this.esPendiente = esPendiente;
        }

        public Votante getVotante() { return votante; }
        public LocalVotacion getLocal() { return local; }
        public boolean isPendiente() { return esPendiente; }
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
    
    public LocalVotacion buscarLocal(String nombre) {
        for (LocalVotacion l : listaLocales) {
            if (l.getNombre().equalsIgnoreCase(nombre)) return l;
        }
        return null;
    }

    public LocalVotacion buscarLocal(String nombre, String comuna) {
    for (LocalVotacion l : listaLocales) {
        if (l.getNombre().equalsIgnoreCase(nombre) &&
            l.getComuna().equalsIgnoreCase(comuna)) {
            return l;
        }
    }
    return null;

}
    public LocalVotacion buscarLocalPorId(String id) {
        for (LocalVotacion l : listaLocales) {
            if (l.getIdLocal().equalsIgnoreCase(id)) return l;
        }
        return null;
    }
    
    public List<Votante> getVotantesPendientes() {
        return Collections.unmodifiableList(votantesPendientes);
    }
     // ====== FILTROS SIA2.5 ======
    // Votantes PENDIENTES por comuna y rango de edad [min, max]
    public List<Votante> filtrarPendientesPorComunaYEdad(String comuna, int edadMin, int edadMax) {
        List<Votante> res = new ArrayList<>();
        String c = comuna.toLowerCase();
        for (Votante v : votantesPendientes) {
            if (v.getComuna().equalsIgnoreCase(comuna)
                    && v.getEdad() >= edadMin
                    && v.getEdad() <= edadMax) {
                res.add(v);
            }
        }
        return res;
    }
    // Votantes (ASIGNADOS en todos los locales + PENDIENTES) por comuna y rango [min, max]
    public List<Votante> filtrarTodosPorComunaYEdad(String comuna, int edadMin, int edadMax) {
        List<Votante> res = new ArrayList<>();
        String c = comuna.toLowerCase();

        // 1) Asignados: recorrer todos los locales y sus mapas de votantes
        for (LocalVotacion l : listaLocales) { // listaLocales ya existe
            for (Votante v : l.getVotantes()) { // acceso a colección interna del local
                if (v.getComuna().equalsIgnoreCase(comuna)
                        && v.getEdad() >= edadMin
                        && v.getEdad() <= edadMax) {
                    res.add(v);
                }
            }
        }

        // 2) Pendientes
        for (Votante v : votantesPendientes) {
            if (v.getComuna().equalsIgnoreCase(comuna)
                    && v.getEdad() >= edadMin
                    && v.getEdad() <= edadMax) {
                res.add(v);
            }
        }
        return res;
    }

    
    public boolean eliminarLocalPorId(String idLocal) {
        for (Iterator<LocalVotacion> it = listaLocales.iterator(); it.hasNext(); ) {
            LocalVotacion l = it.next();
            if (l.getIdLocal().equalsIgnoreCase(idLocal)) {
                // mover asignados a pendientes
                for (Votante v : l.getVotantes()) { // ya tienes getVotantes() 
                    v.setLocalAsignado(null);       // limpiar referencia
                    votantesPendientes.add(v);      // vuelve a pendientes
                }
                it.remove();
                return true;
            }
        }
        return false;
    }
    public boolean modificarLocal(String idLocal, String nuevoNombre,
                              String nuevaDireccion, String nuevaComuna,
                              Integer nuevaCapacidad) {
        for (LocalVotacion l : listaLocales) {
            if (l.getIdLocal().equalsIgnoreCase(idLocal)) {
                if (nuevoNombre != null && !nuevoNombre.isBlank()) l.setNombre(nuevoNombre);
                if (nuevaDireccion != null && !nuevaDireccion.isBlank()) l.setDireccion(nuevaDireccion);
                if (nuevaComuna != null && !nuevaComuna.isBlank()) l.setComuna(nuevaComuna);
                if (nuevaCapacidad != null) {
                    if (!l.setCapacidad(nuevaCapacidad)) {
                        // no se puede bajar por debajo de asignados
                        return false;
                    }
                }
                return true;
            }
        }
    return false;
}
        // ===== BUSCAR EN 1+ NIVELES: VOTANTE POR RUT =====
    public ResultadoBusquedaVotante buscarVotanteGlobalPorRut(String rut) {
        // 1) Buscar en todos los locales (nivel 2)
        for (LocalVotacion l : listaLocales) {
            Votante v = l.buscarVotante(rut);
            if (v != null) {
                return new ResultadoBusquedaVotante(v, l, false);
            }
        }
        // 2) Buscar en pendientes (no asignados)
        for (Votante vp : votantesPendientes) {
            if (vp.getRut().equalsIgnoreCase(rut)) {
                return new ResultadoBusquedaVotante(vp, null, true);
            }
        }
        return null;
    }
        // ===== BUSCAR EN 1+ NIVELES: VOTANTE POR NOMBRE/APELLIDO =====
    public ResultadoBusquedaVotante buscarVotanteGlobalPorNombre(String nombre, String apellido) {
        // 1) Buscar en todos los locales (nivel 2)
        for (LocalVotacion l : listaLocales) {
            Votante v = l.buscarVotante(nombre, apellido); // ya implementado en LocalVotacion
            if (v != null) {
                return new ResultadoBusquedaVotante(v, l, false);
            }
        }
        // 2) Buscar en pendientes
        for (Votante vp : votantesPendientes) {
            String[] partes = vp.getNombre().split("\\s+");
            if (partes.length >= 2 &&
                partes[0].equalsIgnoreCase(nombre) &&
                partes[1].equalsIgnoreCase(apellido)) {
                return new ResultadoBusquedaVotante(vp, null, true);
            }
        }
        return null;
    }


    public List<LocalVotacion> getListaLocales() {
        return Collections.unmodifiableList(listaLocales);
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
    
    
    public String eliminarVotanteGlobal(String rut) {
        ResultadoBusquedaVotante res = buscarVotanteGlobalPorRut(rut);

        if (res == null) {
            return "Error: Votante con RUT " + rut + " no fue encontrado.";
        }

        if (res.isPendiente()) {
            votantesPendientes.remove(res.getVotante());
            return "¡Éxito! Votante pendiente '" + res.getVotante().getNombre() + "' ha sido eliminado.";
        } else {
            LocalVotacion local = res.getLocal();
            boolean eliminado = local.eliminarVotante(rut);
            if (eliminado) {
                // Este es el punto clave para la reorganización
                return "¡Éxito! Votante '" + res.getVotante().getNombre() + "' eliminado del local '" + local.getNombre() + "'. Se ha liberado un cupo.";
            } else {
                // Esto no debería ocurrir si la búsqueda funcionó, pero es una buena práctica
                return "Error inesperado: No se pudo eliminar el votante del local.";
            }
        }
    }
    
    
    
    
}
