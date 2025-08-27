package javaproyecto.sia;

import java.util.ArrayList;
import java.util.List;

public class LocalVotacion {
    private String idLocal;
    private String nombre;
    private String direccion;
    private String comuna;
    private int capacidad;
    private List<Votante> listaVotantes;

    public LocalVotacion(String idLocal, String nombre, String direccion, String comuna, int capacidad) {
        this.idLocal = idLocal;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.capacidad = capacidad;
        this.listaVotantes = new ArrayList<>();
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

    public void agregarVotante(Votante v) {
        if (listaVotantes.size() < capacidad) {
            listaVotantes.add(v);
        } else {
            System.out.println("Capacidad máxima alcanzada en " + nombre);
        }
    }
}
