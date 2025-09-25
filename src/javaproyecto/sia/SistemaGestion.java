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
    
    
    //SIA 2.9
    // REEMPLAZA tu antiguo registrarVotante con este:
    public void registrarVotante(Votante v) throws RutDuplicadoException {
        // Busca en todo el sistema (locales + pendientes)
        if (buscarVotanteGlobalPorRut(v.getRut()) != null) {
            throw new RutDuplicadoException("El RUT " + v.getRut() + " ya está registrado en el sistema.");
        }
        // Si no lo encuentra, lo agrega a pendientes
        votantesPendientes.add(v);
    }

    // REEMPLAZA tu antiguo autoAsignar con este:
    public void autoAsignar() {
        Iterator<Votante> it = votantesPendientes.iterator();

        while (it.hasNext()) {
            Votante votante = it.next();
            boolean asignado = false;

            for (LocalVotacion local : listaLocales) {
                boolean mismaComuna = votante.getComuna().equalsIgnoreCase(local.getComuna());

                // Ya no es necesario chequear la capacidad aquí, la excepción lo maneja
                if (mismaComuna) {
                    try {
                        local.agregarVotante(votante); // Intenta agregar
                        System.out.println(" > Votante '" + votante.getNombre() + "' asignado a -> " + local.getNombre());
                        it.remove(); 
                        asignado = true;
                        break; // Votante asignado, pasamos al siguiente
                    } catch (CapacidadAgotadaException e) {
                        // Si este local está lleno, el 'for' continuará buscando otro en la misma comuna
                    }
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

    
    // REEMPLAZA tu antiguo eliminarLocalPorId con este:
    public boolean eliminarLocalPorId(String idLocal) {
        LocalVotacion localAEliminar = buscarLocalPorId(idLocal);

        if (localAEliminar != null) {
            // Mover todos los votantes del local a la lista de pendientes
            for (Votante v : localAEliminar.getVotantes()) {
                v.setLocalAsignado(null); // Limpiar la referencia al local
                votantesPendientes.add(v);
            }
            // Eliminar el local de la lista
            listaLocales.remove(localAEliminar);
            return true;
        }
        return false; // No se encontró el local
    }
    
    
    // REEMPLAZA tu antiguo modificarLocal con este:
    public boolean modificarLocal(String idLocal, String nuevoNombre,
                                  String nuevaDireccion, String nuevaComuna,
                                  Integer nuevaCapacidad) {
        LocalVotacion local = buscarLocalPorId(idLocal);
        if (local == null) {
            return false; // No se encontró el local
        }

        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            local.setNombre(nuevoNombre);
        }
        if (nuevaDireccion != null && !nuevaDireccion.isBlank()) {
            local.setDireccion(nuevaDireccion);
        }
        if (nuevaComuna != null && !nuevaComuna.isBlank()) {
            local.setComuna(nuevaComuna);
        }
        if (nuevaCapacidad != null) {
            // El método setCapacidad en LocalVotacion ya valida
            // si la nueva capacidad es menor a los votantes actuales.
            if (!local.setCapacidad(nuevaCapacidad)) {
                return false; // No se pudo cambiar la capacidad
            }
        }
        return true;
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
