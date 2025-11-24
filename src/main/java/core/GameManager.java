/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 *
 * @author danie
 */


import model.IGameFunction;
import games.clicker.ClickerGame;
import games.dice.Dice;
import games.tictactoe.Tictactoe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor central del sistema de juegos.
 * Coordina la carga de juegos internos y externos, y proporciona acceso
 * unificado a todos los juegos disponibles.
 * Implementa el patrón Singleton.
 */
public class GameManager {
    private static GameManager instance;
    private List<IGameFunction> juegosDisponibles;
    private PluginLoader pluginLoader;

    /**
     * Constructor privado para patrón Singleton.
     */
    private GameManager() {
        juegosDisponibles = new ArrayList<>();
        cargarJuegosInternos();
        cargarJuegosExternos();
    }

    /**
     * Obtiene la instancia única del GameManager (patrón Singleton).
     *
     * @return Instancia del GameManager.
     */
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Carga los juegos internos del sistema (incluidos en el proyecto).
     */
    private void cargarJuegosInternos() {
        juegosDisponibles.add(ClickerGame.getInstance());
        juegosDisponibles.add(Dice.getInstance());
        juegosDisponibles.add(Tictactoe.getInstance());
        System.out.println("Juegos internos cargados: " + juegosDisponibles.size());
    }

    /**
     * Carga juegos externos desde archivos JAR usando el PluginLoader.
     */
    private void cargarJuegosExternos() {
        File pluginsDir = new File("plugins");
        pluginLoader = new PluginLoader(pluginsDir);
        List<IGameFunction> pluginsExternos = pluginLoader.cargarPlugins();
        juegosDisponibles.addAll(pluginsExternos);
        System.out.println("Juegos externos cargados: " + pluginsExternos.size());
    }

    /**
     * Obtiene la lista de todos los juegos disponibles (internos y externos).
     *
     * @return Lista de juegos disponibles.
     */
    public List<IGameFunction> getJuegosDisponibles() {
        return new ArrayList<>(juegosDisponibles);
    }

    /**
     * Recarga los juegos externos desde el directorio de plugins.
     * Útil si se agregan nuevos JARs sin reiniciar la aplicación.
     */
    public void recargarPlugins() {
        // Remover SOLO los juegos externos (no los internos)
        juegosDisponibles.removeIf(juego -> !esJuegoInterno(juego));
        
        // Cargar nuevamente los plugins
        List<IGameFunction> pluginsExternos = pluginLoader.cargarPlugins();
        juegosDisponibles.addAll(pluginsExternos);
        
        System.out.println("Plugins recargados. Total de juegos: " + juegosDisponibles.size());
    }

    private boolean esJuegoInterno(IGameFunction juego) {
        String nombreClase = juego.getClass().getName();
        // Verificar si pertenece a los paquetes internos del proyecto
        return nombreClase.startsWith("games.clicker") || 
            nombreClase.startsWith("games.dice") || 
            nombreClase.startsWith("games.tictactoe");
    }


    

    /**
     * Obtiene la cantidad total de juegos disponibles.
     *
     * @return Número de juegos disponibles.
     */
    public int getCantidadJuegos() {
        return juegosDisponibles.size();
    }
}

