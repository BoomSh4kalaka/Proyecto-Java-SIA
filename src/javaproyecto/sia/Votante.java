package javaproyecto.sia;

public class Votante extends Persona {
    private LocalVotacion localAsignado;

    public Votante(String rut, String nombre, String direccion, String comuna, int edad) {
        super(rut, nombre, direccion, comuna, edad);
    }

    // Getters y Setters

    public LocalVotacion getLocalAsignado() { return localAsignado; }
    public void setLocalAsignado(LocalVotacion localAsignado) { this.localAsignado = localAsignado; }
    
    @Override
    public String identificarse() {
        String base = super.identificarse();
        if (getLocalAsignado() != null) {
            base += "\nLocal asignado: " + getLocalAsignado().getNombre();
        } else {
            base += "\nLocal asignado: PENDIENTE";
        }
        return base;
    }
}
