/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

/**
 *
 * @author danie
 */


import games.clicker.ClickerGame;
import games.dice.Dice;
import games.tictactoe.Tictactoe;
import model.IGameListener;
import model.Stat;
import records.RecordsManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Interfaz gráfica principal del gestor de juegos.
 * Muestra menú para lanzar juegos, consulta de records y gestiona las ventanas internas.
 */
public class UI extends JFrame {
    private JDesktopPane desktopPane;
    private RecordsManager recordsManager;

    public UI() {
        super("Gestor de Juegos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        recordsManager = new RecordsManager();
        desktopPane = new JDesktopPane();
        setContentPane(desktopPane);

        // Crear barra de menú
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Menú Juegos
        JMenu menuJuegos = new JMenu("Juegos");
        menuBar.add(menuJuegos);

        JMenuItem juegoClicker = new JMenuItem("Clicker");
        JMenuItem juegoDado = new JMenuItem("Dado");
        JMenuItem juegoTicTacToe = new JMenuItem("Tres en Raya");
        menuJuegos.add(juegoClicker);
        menuJuegos.add(juegoDado);
        menuJuegos.add(juegoTicTacToe);

        // Menú Records
        JMenu menuRecords = new JMenu("Records");
        menuBar.add(menuRecords);

        JMenuItem verRecords = new JMenuItem("Ver records");
        menuRecords.add(verRecords);

        // Acciones para lanzar juegos
        juegoClicker.addActionListener(e -> lanzarJuego("Clicker", ClickerGame.getInstance()));
        juegoDado.addActionListener(e -> lanzarJuego("Dado", Dice.getInstance()));
        juegoTicTacToe.addActionListener(e -> lanzarJuego("Tres en Raya", Tictactoe.getInstance()));

        // Acción para mostrar records
        verRecords.addActionListener(e -> mostrarRecords());

        setVisible(true);
    }

    /**
     * Lanza un juego en un JInternalFrame.
     *
     * @param nombre Nombre del juego.
     * @param juego  Instancia que implementa IGameFunction.
     */
    private void lanzarJuego(String nombre, model.IGameFunction juego) {
        JInternalFrame frame = new JInternalFrame(nombre, true, true, true, true);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        JLabel status = new JLabel("Jugando: " + nombre);
        frame.add(status, BorderLayout.SOUTH);

        // Integrar el listener para guardar el resultado al terminar la partida
        juego.setGameListener(new IGameListener() {
            @Override
            public void onGameFinished(Stat stats) {
                recordsManager.registrarRecord(nombre, stats);
                status.setText("Partida finalizada. Resultado: " + stats.getValor());
            }
        });

        // Iniciar el juego (la lógica de cada juego abre su propia ventana gráfica)
        juego.iniciar();

        desktopPane.add(frame);
        frame.setVisible(true);
    }

    /**
     * Muestra los records en un JInternalFrame utilizando una JTable.
     */
    private void mostrarRecords() {
        JInternalFrame frame = new JInternalFrame("Records", true, true, true, true);
        frame.setSize(450, 300);

        String[] nombresJuegos = {"Clicker", "Dado", "Tres en Raya"};
        String[] columnas = {"Juego", "Clave", "Nombre", "Valor"};
        Object[][] datos = new Object[9][4];
        int index = 0;

        for (String nombre : nombresJuegos) {
            List<Stat> records = recordsManager.getMejoresRecords(nombre);
            for (Stat stat : records) {
                datos[index][0] = nombre;
                datos[index][1] = stat.getClave();
                datos[index][2] = stat.getNombre();
                datos[index][3] = stat.getValor();
                index++;
            }
        }

        JTable table = new JTable(datos, columnas);
        frame.add(new JScrollPane(table));
        desktopPane.add(frame);
        frame.setVisible(true);
    }
}
