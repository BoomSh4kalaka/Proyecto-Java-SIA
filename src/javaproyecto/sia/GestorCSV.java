package javaproyecto.sia;

import java.io.*;
import java.util.*;

public class GestorCSV {

    private static final String rutaLocales = "data/locales.csv";
    private static final String rutaVotantes = "data/votantes.csv";


    /* ------------------ Escritura ------------------ */

    // Guarda todos los locales (todos sus atributos)
    public static void guardarLocales(SistemaGestion sistema) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaLocales))) {
            // Cabecera
            pw.println("Id,Nombre,Direccion,Comuna,Capacidad");

            for (LocalVotacion local : sistema.getListaLocales()) {
                pw.printf("%s,%s,%s,%s,%d%n",
                    local.getIdLocal(),
                    local.getNombre(),
                    local.getDireccion() != null ? local.getDireccion() : "",
                    local.getComuna(),
                    local.getCapacidad()
                );
            }

            System.out.println("Locales guardados en " + rutaLocales);

        } catch (IOException e) {
            System.err.println("Error al guardar locales: " + e.getMessage());
        }
    }

    // Guarda todos los votantes (los asignados y los pendientes)
    public static void guardarVotantes(SistemaGestion sistema) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaVotantes))) {
            // Cabecera
            pw.println("Rut,Nombre,Direccion,Comuna,Edad,IdLocal");

            // Recorremos TODOS los votantes, tanto asignados como pendientes
            for (LocalVotacion local : sistema.getListaLocales()) {
                for (Votante v : local.getVotantes()) {
                    pw.printf("%s,%s,%s,%s,%d,%s%n",
                        v.getRut(),
                        v.getNombre(),
                        v.getDireccion(),
                        v.getComuna(),
                        v.getEdad(),
                        local.getIdLocal() // siempre tiene local asignado
                    );
                }
            }

            // Votantes pendientes (sin local asignado)
            for (Votante v : sistema.getVotantesPendientes()) {
                pw.printf("%s,%s,%s,%s,%d,%s%n",
                    v.getRut(),
                    v.getNombre(),
                    v.getDireccion(),
                    v.getComuna(),
                    v.getEdad(),
                    ""  // sin local asignado
                );
            }

            System.out.println("Votantes guardados en " + rutaVotantes);

        } catch (IOException e) {
            System.err.println("Error al guardar votantes: " + e.getMessage());
        }
    }
    // Conveniencia: guarda ambos archivos
    public void guardarTodo(SistemaGestion sistema) {
        guardarLocales(sistema);
        guardarVotantes(sistema);
    }

    /* ------------------ Lectura ------------------ */

    // Cargar locales desde locales.csv
    public static void cargarLocales(SistemaGestion sistema) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaLocales))) {
            String linea;
            boolean primera = true;

            while ((linea = br.readLine()) != null) {
                if (primera) {
                    primera = false; // saltamos la cabecera "Id,Nombre,Comuna,Capacidad"
                    continue;
                }

                String[] partes = linea.split(",", -1); // <-- EL -1 es CLAVE, conserva columnas vacías
                if (partes.length != 5) {
                    System.err.println("Línea inválida en locales.csv: " + linea);
                    continue;
                }

                String id = partes[0].trim();
                String nombre = partes[1].trim();
                String direccion = partes[2].trim(); // puede ser vacío
                String comuna = partes[3].trim();
                int capacidad;
                try {
                    capacidad = Integer.parseInt(partes[4].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Capacidad inválida en línea: " + linea);
                    continue;
                }
                LocalVotacion local = new LocalVotacion(id, nombre, direccion, comuna, capacidad);
                sistema.registrarLocal(local);
                System.out.println("  + Local cargado: " + id + " - " + nombre);
            }

            System.out.println("Locales cargados desde " + rutaLocales);

        } catch (IOException e) {
            System.err.println("Error al leer " + rutaLocales + ": " + e.getMessage());
        }
    }

    // Cargar votantes desde votantes.csv (debe llamarse DESPUÉS de cargarLocales)
    public static void cargarVotantes(SistemaGestion sistema) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaVotantes))) {
            String linea;
            boolean primera = true;

            while ((linea = br.readLine()) != null) {
                if (primera) {
                    primera = false; // saltamos cabecera
                    continue;
                }

                String[] partes = linea.split(",", -1); // mantiene campos vacíos, incluso el último
                if (partes.length < 6) {
                    System.err.println("Línea inválida en votantes.csv: " + linea);
                    continue;
                }

                // Limpieza de cada campo
                for (int i = 0; i < partes.length; i++) {
                    partes[i] = partes[i].trim().replaceAll("^\"|\"$", "");
                }

                String rut = partes[0];
                String nombre = partes[1];
                String direccion = partes[2];
                String comuna = partes[3];
                int edad;
                try {
                    edad = Integer.parseInt(partes[4]);
                } catch (NumberFormatException e) {
                    System.err.println("Edad inválida en línea: " + linea);
                    continue;
                }

                String idLocal = partes[5];

                // Crear el votante
                Votante v = new Votante(rut, nombre, direccion, comuna, edad);

                if (!idLocal.isEmpty()) {
                    // Buscar local por ID
                    LocalVotacion local = null;
                    for (LocalVotacion l : sistema.getListaLocales()) {
                        if (l.getIdLocal().equalsIgnoreCase(idLocal)) {
                            local = l;
                            break;
                        }
                    }

                    if (local == null) {
                        // Si el local no existía, crearlo "fantasma"
                        System.out.println("Aviso: local referenciado no existía. Se creó " + idLocal);
                        // Usamos capacidad 0; si no alcanza, caerá en catch y lo mandaremos a pendientes
                        local = new LocalVotacion(idLocal, "Desconocido", "", comuna, 0);
                        sistema.registrarLocal(local);
                    }

                    // Intentar asignar al local (puede lanzar CapacidadAgotadaException)
                    try {
                        local.agregarVotante(v);
                    } catch (CapacidadAgotadaException e) {
                        System.out.println("Capacidad llena en '" + local.getNombre() + "'. Se enviará a pendientes: " + v.getRut());
                        // Si no pudo entrar al local, lo registramos como pendiente
                        try {
                            sistema.registrarVotante(v);
                        } catch (RutDuplicadoException exDup) {
                            System.out.println("Duplicado detectado (pendientes) para RUT " + v.getRut() + ". Se omite.");
                        }
                    }

                } else {
                    // Sin local asignado → va a pendientes (puede lanzar RutDuplicadoException)
                    try {
                        sistema.registrarVotante(v);
                    } catch (RutDuplicadoException exDup) {
                        System.out.println("Duplicado detectado (pendientes) para RUT " + v.getRut() + ". Se omite.");
                    }
                }

            }

            System.out.println("Votantes cargados desde " + rutaVotantes);

        } catch (IOException e) {
            System.err.println("Error al leer " + rutaVotantes + ": " + e.getMessage());
        }
    }

    // Conveniencia: carga ambos (locales primero, luego votantes)
    public void cargarTodo(SistemaGestion sistema) {
        cargarLocales(sistema);
        System.out.println("Locales en memoria: " + sistema.getListaLocales().size());
        cargarVotantes(sistema);
    }

    /* ------------------ Helpers CSV ------------------ */

    // arma una línea CSV con comillas y comillas escapadas
    private String csvLine(String... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeForCSV(campos[i] == null ? "" : campos[i]));
        }
        return sb.toString();
    }

    // siempre ponemos comillas y escapamos comillas internas
    private String escapeForCSV(String campo) {
        String s = campo.replace("\"", "\"\""); // doble comilla interna
        return "\"" + s + "\"";
    }

    // parsea correctamente una línea CSV con comillas y "" como escape
    private String[] parseCSVLine(String line) {
        List<String> campos = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // si la siguiente es quote -> es comilla escapada
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++; // saltamos el segundo quote
                    } else {
                        inQuotes = false; // cierre
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    campos.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        campos.add(cur.toString());
        return campos.toArray(new String[0]);
    }
}