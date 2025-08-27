package javaproyecto.sia;

public class Votante {
    private String rut;
    private String nombre;
    private String direccion;
    private String comuna;
    private int edad;
    private LocalVotacion localAsignado;

    public Votante(String rut, String nombre, String direccion, String comuna, int edad) {
        this.rut = rut;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.edad = edad;
    }

    // Getters y Setters
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public LocalVotacion getLocalAsignado() { return localAsignado; }
    public void setLocalAsignado(LocalVotacion localAsignado) { this.localAsignado = localAsignado; }
}
