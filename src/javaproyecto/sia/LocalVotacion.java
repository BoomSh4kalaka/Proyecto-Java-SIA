package javaproyecto.sia;

import java.util.*;

public class LocalVotacion {
    private String idLocal;
    private String nombre;
    private String direccion;
    private String comuna;
    private int capacidad;

    // Mapa de votantes (clave = rut)
    private Map<String, Votante> mapaVotantes = new HashMap<>();

    public LocalVotacion(String idLocal, String nombre, String direccion, String comuna, int capacidad) {
        this.idLocal = idLocal;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.capacidad = capacidad;
    }

    // Getters
    public String getIdLocal() { return idLocal; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getComuna() { return comuna; }
    public int getCapacidad() { return capacidad; }

    // Métodos principales
    public boolean agregarVotante(Votante v) {
        if (mapaVotantes.size() < capacidad) {
            mapaVotantes.put(v.getRut(), v);
            v.setLocalAsignado(this);
            return true;
        }
        return false;
    }

    public Votante buscarPorRut(String rut) {
        return mapaVotantes.get(rut);
    }

    public Collection<Votante> getVotantes() {
        return mapaVotantes.values();
    }

    public int getCantidadVotantes() {
        return mapaVotantes.size();
    }

    public void mostrarInfoDetallada() {
        System.out.println("Local: " + this.nombre + " (ID: " + this.idLocal + ")");
        System.out.println("  Comuna: " + this.comuna);
        System.out.println("  Capacidad: " + this.getCantidadVotantes() + " / " + this.capacidad);

        if (mapaVotantes.isEmpty()) {
            System.out.println("  (Aún no hay votantes asignados)");
        } else {
            System.out.println("  Votantes Asignados:");
            for (Votante votante : mapaVotantes.values()) {
                System.out.println("    - " + votante.getNombre() + " (" + votante.getRut() + ")");
            }
        }
    }
}
