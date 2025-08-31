package javaproyecto.sia;

import java.util.*;

public class LocalVotacion {
    private String idLocal;
    private String nombre;
    private String direccion;
    private String comuna;
    private int capacidad;

    // Colección anidada (lista)
    private List<Votante> listaVotantes;

    // Mapa para buscar votantes rápidamente por RUT
    private Map<String, Votante> mapaVotantes;

    public LocalVotacion(String idLocal, String nombre, String direccion, String comuna, int capacidad) {
        this.idLocal = idLocal;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.capacidad = capacidad;
        this.listaVotantes = new ArrayList<>();
        this.mapaVotantes = new HashMap<>();
    }

    // Getters y Setters
    public String getIdLocal() { return idLocal; }
    public void setIdLocal(String idLocal) { this.idLocal = idLocal; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public List<Votante> getListaVotantes() { return listaVotantes; }
    public Map<String, Votante> getMapaVotantes() { return mapaVotantes; }

    // Agregar votante (a lista y mapa)
    public void agregarVotante(Votante v) {
        if (listaVotantes.size() < capacidad) {
            listaVotantes.add(v);
            mapaVotantes.put(v.getRut(), v);
        } else {
            System.out.println("Capacidad máxima alcanzada en " + nombre);
        }
    }

    // Buscar por RUT usando el mapa (rápido)
    public Votante buscarPorRut(String rut) {
        return mapaVotantes.get(rut);
    }
    
    public void mostrarInfoDetallada() {
        System.out.println("Local: " + this.nombre + " (ID: " + this.idLocal + ")");
        System.out.println("  Comuna: " + this.comuna);
        System.out.println("  Capacidad: " + this.listaVotantes.size() + " / " + this.capacidad);

        if (this.listaVotantes.isEmpty()) {
            System.out.println("  (Aún no hay votantes asignados)");
        } else {
            System.out.println("  Votantes Asignados:");
            for (Votante votante : this.listaVotantes) {
                // Imprime el nombre y RUT de cada votante
                System.out.println("    - " + votante.getNombre() + " (" + votante.getRut() + ")");
            }
        }
    }
}
