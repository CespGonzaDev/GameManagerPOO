/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;
import GUI.UI;
/**
 *
 * @author danie
 * Punto de entrada principal del sistema de gestión de juegos.
 * Inicializa el GameManager y lanza la interfaz gráfica.
 */
public class Launcher {
    
    /**
     * Método main - punto de entrada de la aplicación.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        System.out.println("=== Iniciando Sistema de Gestión de Juegos ===");
        
        // Inicializar el gestor de juegos (carga juegos internos y externos)
        GameManager gameManager = GameManager.getInstance();
        System.out.println("GameManager inicializado con " + gameManager.getCantidadJuegos() + " juegos.");
        
        // Lanzar la interfaz gráfica en el hilo de eventos de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            new UI();
            System.out.println("Interfaz gráfica iniciada correctamente.");
        });
        
        System.out.println("=== Sistema iniciado exitosamente ===");
    }
}

