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
    
    public void cargarDatosIniciales() {
        // Registrar locales de votación de ejemplo
        LocalVotacion local1 = new LocalVotacion("L001", "Escuela Central", "Av. Principal 123", "Santiago", 3);
        LocalVotacion local2 = new LocalVotacion("L002", "Colegio Nacional", "Calle Secundaria 45", "Providencia", 2);
        this.registrarLocal(local1);
        this.registrarLocal(local2);

        // Registrar votantes de ejemplo
        Votante v1 = new Votante("11111111-1", "Juan Pérez", "Calle 1", "Santiago", 30);
        Votante v2 = new Votante("22222222-2", "María Gómez", "Calle 2", "Santiago", 28);
        Votante v3 = new Votante("33333333-3", "Pedro Torres", "Calle 3", "Providencia", 40);
        this.registrarVotante(v1);
        this.registrarVotante(v2);
        this.registrarVotante(v3);
    }

    public void registrarVotante(Votante v) {
        listaVotantes.add(v);
    }

    public void registrarLocal(LocalVotacion l) {
        listaLocales.add(l);
    }


    // Método futuro para autoasignación
    public void autoAsignar() {
        System.out.println("\nIniciando proceso de asignación automática...");

    
        for (Votante votante : this.listaVotantes) {

            // 2. Solo procesamos a los que todavía no tienen un local asignado
            if (votante.getLocalAsignado() == null) {
                boolean asignado = false; // Una bandera para saber si lo logramos asignar

                // 3. Buscamos un local adecuado para este votante
                for (LocalVotacion local : this.listaLocales) {

                    // 4. Verificamos las dos condiciones clave
                    boolean mismaComuna = votante.getComuna().equalsIgnoreCase(local.getComuna());
                    boolean hayCapacidad = local.getListaVotantes().size() < local.getCapacidad();

                    if (mismaComuna && hayCapacidad) {
                        // ¡Encontramos uno! Lo asignamos.
                        this.asignarVotante(local, votante); // Reutilizamos el método que ya tenías
                        System.out.println(" > Votante '" + votante.getNombre() + "' asignado a -> " + local.getNombre());
                        asignado = true;
                        break; // Importante: Salimos del bucle de locales y pasamos al siguiente votante
                    }
                }

                // 5. Si terminamos de buscar y no encontramos local, informamos.
                if (!asignado) {
                    System.out.println(" ! Votante '" + votante.getNombre() + "' no pudo ser asignado (sin cupo en su comuna).");
                }
            }
        }
        System.out.println("Proceso de asignación finalizado.");
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
        
        public void imprimirReporteGeneral() {
        System.out.println("\n===== REPORTE GENERAL DE LOCALES DE VOTACIÓN =====");
        if (this.listaLocales.isEmpty()) {
            System.out.println("No hay locales registrados en el sistema.");
            return;
        }

        for (LocalVotacion local : this.listaLocales) {
            local.mostrarInfoDetallada(); // Llama al método que creamos en el paso 1
            System.out.println("-------------------------------------------------");
        }
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