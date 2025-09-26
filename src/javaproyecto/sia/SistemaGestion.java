package javaproyecto.sia;


import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

import java.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;


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

    public void registrarLocal(LocalVotacion l) throws IdLocalDuplicadoException {
        for (LocalVotacion existente : listaLocales) {
            if (existente.getIdLocal().equalsIgnoreCase(l.getIdLocal())) {
                throw new IdLocalDuplicadoException("El ID de local '" + l.getIdLocal() + "' ya está registrado.");
            }
        }
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
    
    public void autoAsignar(String comuna) {
        Iterator<Votante> it = votantesPendientes.iterator();

        while (it.hasNext()) {
            Votante votante = it.next();

            // saltar votantes que no son de la comuna seleccionada
            if (!votante.getComuna().equalsIgnoreCase(comuna)) {
                continue;
            }

            boolean asignado = false;

            for (LocalVotacion local : listaLocales) {
                boolean mismaComuna = votante.getComuna().equalsIgnoreCase(local.getComuna());

                if (mismaComuna) {
                    try {
                        local.agregarVotante(votante);
                        System.out.println(" > Votante '" + votante.getNombre() + "' asignado a -> " + local.getNombre());
                        it.remove();
                        asignado = true;
                        break;
                    } catch (CapacidadAgotadaException e) {
                        // Si este local está lleno, sigue buscando en otros de la misma comuna
                    }
                }
            }

            if (!asignado) {
                System.out.println(" ! Votante '" + votante.getNombre() + "' no pudo ser asignado en comuna " + comuna + " (sin cupo).");
            }
        }
        System.out.println("Proceso de asignación finalizado para comuna: " + comuna);
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
    /**
 * Modifica los datos de un votante identificado por su RUT.
 * - Si está ASIGNADO a un local y cambia su comuna a una distinta del local,
 *   se REMUEVE del local y pasa a PENDIENTES (manteniendo el RUT).
 * - No permite cambiar el RUT (es la clave).
 * @return true si se encontró y actualizó; false si no existe.
 */
public boolean modificarVotantePorRut(String rut,
                                      String nuevoNombre,
                                      String nuevaDireccion,
                                      String nuevaComuna,
                                      Integer nuevaEdad) {
    // 1) Buscar en asignados (todos los locales)
    for (LocalVotacion l : listaLocales) {
        Votante v = l.buscarVotante(rut);
        if (v != null) {
            // actualizar campos
            if (nuevoNombre != null && !nuevoNombre.isBlank()) v.setNombre(nuevoNombre);
            if (nuevaDireccion != null && !nuevaDireccion.isBlank()) v.setDireccion(nuevaDireccion);
            String comunaActual = v.getComuna();
            if (nuevaComuna != null && !nuevaComuna.isBlank()) v.setComuna(nuevaComuna);
            if (nuevaEdad != null) v.setEdad(nuevaEdad);

            // regla: si la comuna cambió y ya no coincide con el local → mover a pendientes
            if (nuevaComuna != null && !nuevaComuna.isBlank()
                    && !nuevaComuna.equalsIgnoreCase(l.getComuna())) {
                l.eliminarVotantePorRut(rut);
                votantesPendientes.add(v);
            }
            return true;
        }
    }
    // 2) Buscar en pendientes
    for (Votante v : votantesPendientes) {
        if (v.getRut().equalsIgnoreCase(rut)) {
            if (nuevoNombre != null && !nuevoNombre.isBlank()) v.setNombre(nuevoNombre);
            if (nuevaDireccion != null && !nuevaDireccion.isBlank()) v.setDireccion(nuevaDireccion);
            if (nuevaComuna != null && !nuevaComuna.isBlank()) v.setComuna(nuevaComuna);
            if (nuevaEdad != null) v.setEdad(nuevaEdad);
            return true;
        }
    }
    return false;
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
    public boolean eliminarVotanteGlobalPorRut(String rut) {
    // en asignados
    for (LocalVotacion l : listaLocales) {
        if (l.eliminarVotantePorRut(rut)) return true;
    }
    // en pendientes
    return votantesPendientes.removeIf(v -> v.getRut().equalsIgnoreCase(rut));
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

    public List<Votante> filtrarVotantes(String rut, String comuna, Integer minEdad, Integer maxEdad, boolean incluirPendientes, boolean incluirAsignados) {
        List<Votante> all = new ArrayList<>();

        if (incluirPendientes) all.addAll(votantesPendientes);
        if (incluirAsignados) {
            for (LocalVotacion l : listaLocales) {
                all.addAll(l.getVotantes());
            }
        }

        return all.stream()
                .filter(v -> (rut == null || v.getRut().equalsIgnoreCase(rut)))
                .filter(v -> (comuna == null || v.getComuna().equalsIgnoreCase(comuna)))
                .filter(v -> (minEdad == null || v.getEdad() >= minEdad))
                .filter(v -> (maxEdad == null || v.getEdad() <= maxEdad))
                .collect(Collectors.toList());
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
    // === Reporte en String (para UI) ===
public String construirReporteGeneral() {
    StringBuilder sb = new StringBuilder("\n===== REPORTE GENERAL DE LOCALES DE VOTACIÓN =====\n");
    if (listaLocales.isEmpty()) {
        return sb.append("No hay locales registrados.\n").toString();
    }
    for (LocalVotacion local : listaLocales) {
        sb.append("Local: ").append(local.getNombre()).append(" (ID: ").append(local.getIdLocal()).append(")\n")
          .append("  Comuna: ").append(local.getComuna()).append("\n")
          .append("  Capacidad: ").append(local.getCantidadVotantes()).append(" / ").append(local.getCapacidad()).append("\n");
        var vs = local.getVotantes();
        if (vs.isEmpty()) sb.append("  (Aún no hay votantes asignados)\n");
        else {
            sb.append("  Votantes Asignados:\n");
            for (Votante v : vs) {
                sb.append("    - ").append(v.getNombre()).append(" (").append(v.getRut()).append(")\n");
            }
        }
        sb.append("-------------------------------------------------\n");
    }
    if (!votantesPendientes.isEmpty()) {
        sb.append("===== VOTANTES PENDIENTES DE ASIGNACIÓN =====\n");
        for (Votante v : votantesPendientes) {
            sb.append(" - ").append(v.getNombre()).append(" (").append(v.getRut()).append(")\n");
        }
    }
    return sb.toString();
}
// Guarda el reporte general a un archivo de texto (UTF-8).
public void guardarReporteTxt(Path destino) throws IOException {
    if (destino == null) {
        throw new IllegalArgumentException("Ruta destino nula.");
    }
    // Asegura que exista la carpeta
    if (destino.getParent() != null) {
        Files.createDirectories(destino.getParent());
    }
    // Volcamos el mismo string que se muestra en la UI
    String contenido = construirReporteGeneral();
    Files.writeString(destino, contenido, StandardCharsets.UTF_8);
}

// Versión cómoda con ruta por defecto: data/reporte_sia.txt
public void guardarReporteTxt() throws IOException {
    guardarReporteTxt(Path.of("data", "reporte_sia.txt"));
}
    
    
    
}
