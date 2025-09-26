

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

    // Getters y setters
    public String getIdLocal() { return idLocal; }
    public void setIdLocal(String idLocal) { this.idLocal = idLocal; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) {this.direccion = direccion;}
    
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) {this.comuna = comuna;}
    
    public int getCapacidad() { return capacidad; }
    
    public boolean setCapacidad(int nuevaCapacidad) {
        if (nuevaCapacidad >= getCantidadVotantes()) {
            this.capacidad = nuevaCapacidad;
            return true;
        }
        return false;
    }

    // Métodos principales
    //SIA 2.9
    public void agregarVotante(Votante v) throws CapacidadAgotadaException {
        if (mapaVotantes.size() >= capacidad) {
            throw new CapacidadAgotadaException("El local '" + nombre + "' ya alcanzó su capacidad máxima.");
        }
        mapaVotantes.put(v.getRut(), v);
        v.setLocalAsignado(this);
    }


    public Votante buscarVotante(String rut) {
        return mapaVotantes.get(rut);
    }

    public Votante buscarVotante(String nombre, String apellido) {
        for (Votante v : mapaVotantes.values()) {
            String[] partes = v.getNombre().split(" ");
            if (partes.length >= 2 && partes[0].equalsIgnoreCase(nombre) && partes[1].equalsIgnoreCase(apellido)) {
                return v;
            }
        }
        return null;
    }
    
    public Collection<Votante> getVotantes() {
        return mapaVotantes.values();
    }

    public int getCantidadVotantes() {
        return mapaVotantes.size();
    }
public boolean eliminarVotantePorRut(String rut) {
    Votante v = mapaVotantes.remove(rut);
    if (v != null) {
        v.setLocalAsignado(null);
        return true;
    }
    return false;
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
    
    public boolean eliminarVotante(String rut) {
        // El método remove de un Map devuelve el valor asociado a la clave 
        // si existía, o null si no. Comprobamos si no fue null.
        return mapaVotantes.remove(rut) != null; 
    }
    
}
