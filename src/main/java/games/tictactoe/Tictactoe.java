/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package games.tictactoe;
import model.*;
/**
 *
 * @author danie
 */
import javax.swing.*;
import java.awt.*;

public class Tictactoe implements IGameFunction {
    private static Tictactoe instance;
    private IGameListener listener;
    private int resultado;

    private JButton[] botones = new JButton[9];
    private char[] tablero = new char[9]; // 'X', 'O', ' '
    private boolean turnoJugador = true;

    private Tictactoe() {}

    public static Tictactoe getInstance() {
        if (instance == null) instance = new Tictactoe();
        return instance;
    }

    @Override
    public void iniciar() {
        JFrame frame = new JFrame("Tres en Raya");
        frame.setLayout(new GridLayout(3,3));
        for (int i=0; i<9; i++) {
            botones[i] = new JButton(" ");
            final int idx = i;
            botones[i].addActionListener(e -> turno(idx));
            frame.add(botones[i]);
            tablero[i] = ' ';
        }
        frame.setSize(250,250);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void turno(int idx) {
        if (!turnoJugador || tablero[idx] != ' ') return;
        botones[idx].setText("X");
        tablero[idx] = 'X';
        if (finJuego()) terminar();
        else {
            turnoJugador = false;
            jugadaMaquina();
        }
    }

    private void jugadaMaquina() {
        for (int i=0; i<9; i++) {
            if (tablero[i] == ' ') {
                botones[i].setText("O");
                tablero[i] = 'O';
                break;
            }
        }
        if (finJuego()) terminar();
        turnoJugador = true;
    }

    private boolean finJuego() {
        char[][] lines = {
            {tablero[0],tablero[1],tablero[2]},
            {tablero[3],tablero[4],tablero[5]},
            {tablero[6],tablero[7],tablero[8]},
            {tablero[0],tablero[3],tablero[6]},
            {tablero[1],tablero[4],tablero[7]},
            {tablero[2],tablero[5],tablero[8]},
            {tablero[0],tablero[4],tablero[8]},
            {tablero[2],tablero[4],tablero[6]}
        };
        for (char[] line : lines) {
            if (line[0] != ' ' && line[0] == line[1] && line[1] == line[2])
                return true;
        }
        // empate
        for (char c: tablero) if (c == ' ') return false;
        return true;
    }

    private void terminar() {
        char ganador = obtenerGanador();
        if (ganador == 'X') resultado = 1;
        else if (ganador == 'O') resultado = -1;
        else resultado = 0;
        JOptionPane.showMessageDialog(null, "Resultado: " + resultado);
        if (listener != null) listener.onGameFinished(getStats());
    }

    private char obtenerGanador() {
        char[][] lines = {
            {tablero[0],tablero[1],tablero[2]},
            {tablero[3],tablero[4],tablero[5]},
            {tablero[6],tablero[7],tablero[8]},
            {tablero[0],tablero[3],tablero[6]},
            {tablero[1],tablero[4],tablero[7]},
            {tablero[2],tablero[5],tablero[8]},
            {tablero[0],tablero[4],tablero[8]},
            {tablero[2],tablero[4],tablero[6]}
        };
        for (char[] line : lines)
            if (line[0] != ' ' && line[0] == line[1] && line[1] == line[2]) return line[0];
        return ' ';
    }

    @Override
    public Stat getStats() {
        return new Stat("resultado", "Resultado", resultado);
    }

    @Override
    public void setGameListener(IGameListener listener) {
        this.listener = listener;
    }
}
