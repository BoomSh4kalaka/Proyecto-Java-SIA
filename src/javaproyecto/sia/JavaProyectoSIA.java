package javaproyecto.sia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;   


public class JavaProyectoSIA {

    public static void main(String[] args) {
        // 1. Crear los objetos principales (el "modelo" y los "servicios")
        SistemaGestion sistema = new SistemaGestion();
        GestorCSV gestor = new GestorCSV();
        gestor.cargarTodo(sistema);

        // 2. Crear la "vista" (el menú de consola) y pasarle los objetos que necesita
        MenuConsola menu = new MenuConsola(sistema, gestor);

        // 3. Iniciar la aplicación
        menu.iniciar();
        
        //Salir y guardar
        gestor.guardarTodo(sistema);
    }
}
