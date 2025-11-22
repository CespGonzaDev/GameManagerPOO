package games.tictactoe;

import model.*;
import javax.swing.*;
import java.awt.*;

public class Tictactoe implements IGameFunction {
    private static Tictactoe instance;
    private IGameListener listener;
    private int resultado;

    private JButton[] botones = new JButton[9];
    private char[] tablero = new char[9];
    private boolean turnoJugador = true;
    private JFrame frame;
    private JLabel lblTurno;

    private Tictactoe() {}

    public static Tictactoe getInstance() {
        if (instance == null) instance = new Tictactoe();
        return instance;
    }

    @Override
    public void iniciar() {
        frame = new JFrame("Tres en Raya");
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        // Panel superior con indicador de turno
        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(138, 43, 226));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblTurno = new JLabel("Tu turno (X)", SwingConstants.CENTER);
        lblTurno.setFont(new Font("Arial", Font.BOLD, 18));
        lblTurno.setForeground(Color.WHITE);
        panelSuperior.add(lblTurno);

        // Panel del tablero
        JPanel panelTablero = new JPanel(new GridLayout(3, 3, 8, 8));
        panelTablero.setBackground(new Color(200, 200, 200));
        panelTablero.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int i = 0; i < 9; i++) {
            botones[i] = new JButton(" ");
            botones[i].setFont(new Font("Arial", Font.BOLD, 48));
            botones[i].setBackground(Color.WHITE);
            botones[i].setFocusPainted(false);
            botones[i].setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
            botones[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            final int idx = i;
            botones[i].addActionListener(e -> turno(idx));
            
            panelTablero.add(botones[i]);
            tablero[i] = ' ';
        }

        frame.add(panelSuperior, BorderLayout.NORTH);
        frame.add(panelTablero, BorderLayout.CENTER);

        turnoJugador = true;
        frame.setSize(400, 450);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void turno(int idx) {
        if (!turnoJugador || tablero[idx] != ' ') return;
        
        botones[idx].setText("X");
        botones[idx].setForeground(new Color(30, 144, 255));
        tablero[idx] = 'X';

        if (finJuego()) {
            terminar();
        } else {
            turnoJugador = false;
            lblTurno.setText("Turno de la máquina (O)");
            
            Timer timer = new Timer(500, e -> {
                jugadaMaquina();
                ((Timer)e.getSource()).stop();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void jugadaMaquina() {
        for (int i = 0; i < 9; i++) {
            if (tablero[i] == ' ') {
                botones[i].setText("O");
                botones[i].setForeground(new Color(255, 69, 0));
                tablero[i] = 'O';
                break;
            }
        }
        
        if (finJuego()) {
            terminar();
        } else {
            turnoJugador = true;
            lblTurno.setText("Tu turno (X)");
        }
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

        for (char c: tablero) if (c == ' ') return false;
        return true;
    }

    private void terminar() {
        char ganador = obtenerGanador();
        String mensaje;
        int tipoMensaje;

        if (ganador == 'X') {
            resultado = 1;
            mensaje = "¡FELICIDADES!\n\n¡Ganaste la partida!";
            tipoMensaje = JOptionPane.INFORMATION_MESSAGE;
        } else if (ganador == 'O') {
            resultado = -1;
            mensaje = "¡JUEGO TERMINADO!\n\nLa máquina ganó esta vez.";
            tipoMensaje = JOptionPane.WARNING_MESSAGE;
        } else {
            resultado = 0;
            mensaje = "¡EMPATE!\n\nNadie ganó esta vez.";
            tipoMensaje = JOptionPane.PLAIN_MESSAGE;
        }

        String[] opciones = {"Jugar de nuevo", "Salir"};
        
        int seleccion = JOptionPane.showOptionDialog(
            frame,
            mensaje,
            "Resultado",
            JOptionPane.YES_NO_OPTION,
            tipoMensaje,
            null,
            opciones,
            opciones[0]
        );

        if (listener != null) {
            listener.onGameFinished(getStats());
        }

        frame.dispose();

        if (seleccion == 0) {
            iniciar();
        }
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

        for (char[] line : lines) {
            if (line[0] != ' ' && line[0] == line[1] && line[1] == line[2]) {
                return line[0];
            }
        }
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
