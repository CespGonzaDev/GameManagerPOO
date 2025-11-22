package games.dice;

import model.*;
import javax.swing.*;
import java.awt.*;

public class Dice implements IGameFunction {
    private static Dice instance;
    private IGameListener listener;
    private int aciertos;
    private int total;
    private int maxJugadas = 5;
    private JFrame frame;
    private JLabel lblResultado;
    private JLabel lblEstadisticas;
    private JButton btnPar;
    private JButton btnImpar;

    private Dice() {}

    public static Dice getInstance() {
        if (instance == null) instance = new Dice();
        return instance;
    }

    @Override
    public void iniciar() {
        aciertos = 0;
        total = 0;

        frame = new JFrame("Juego del Dado");
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(255, 250, 240));

        // Panel superior
        JPanel panelSuperior = new JPanel(new GridLayout(2, 1, 5, 5));
        panelSuperior.setBackground(new Color(220, 20, 60));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Apuesta: ¿Par o Impar?", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        lblEstadisticas = new JLabel("Jugadas: 0/" + maxJugadas + " | Aciertos: 0", SwingConstants.CENTER);
        lblEstadisticas.setFont(new Font("Arial", Font.PLAIN, 14));
        lblEstadisticas.setForeground(Color.WHITE);

        panelSuperior.add(lblTitulo);
        panelSuperior.add(lblEstadisticas);

        // Panel central con resultado
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(new Color(255, 250, 240));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblResultado = new JLabel("¡Haz tu apuesta!", SwingConstants.CENTER);
        lblResultado.setFont(new Font("Arial", Font.BOLD, 18));
        lblResultado.setForeground(new Color(70, 70, 70));

        panelCentral.add(lblResultado, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 15, 0));
        panelBotones.setBackground(new Color(255, 250, 240));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        btnPar = crearBotonApuesta("PAR", new Color(30, 144, 255));
        btnImpar = crearBotonApuesta("IMPAR", new Color(255, 140, 0));

        btnPar.addActionListener(e -> jugar(0));
        btnImpar.addActionListener(e -> jugar(1));

        panelBotones.add(btnPar);
        panelBotones.add(btnImpar);

        frame.add(panelSuperior, BorderLayout.NORTH);
        frame.add(panelCentral, BorderLayout.CENTER);
        frame.add(panelBotones, BorderLayout.SOUTH);

        frame.setSize(450, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private JButton crearBotonApuesta(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void jugar(int apuesta) {
        int dado = 1 + (int)(Math.random() * 6);
        boolean esPar = dado % 2 == 0;
        boolean acierto = (apuesta == 0 && esPar) || (apuesta == 1 && !esPar);
        
        total++;
        if (acierto) aciertos++;

        String resultadoTexto = String.format(
            "Dado: %d (%s) - %s",
            dado,
            esPar ? "Par" : "Impar",
            acierto ? "¡ACERTASTE!" : "Fallaste"
        );
        
        lblResultado.setText(resultadoTexto);
        lblResultado.setForeground(acierto ? new Color(0, 128, 0) : new Color(255, 0, 0));
        lblEstadisticas.setText("Jugadas: " + total + "/" + maxJugadas + " | Aciertos: " + aciertos);

        if (total >= maxJugadas) {
            btnPar.setEnabled(false);
            btnImpar.setEnabled(false);
            terminarJuego();
        }
    }

    private void terminarJuego() {
        String mensaje = String.format(
            "¡Juego terminado!\n\nJugadas: %d\nAciertos: %d\nPorcentaje: %.1f%%",
            total,
            aciertos,
            (aciertos * 100.0 / total)
        );
        
        String[] opciones = {"Jugar de nuevo", "Salir"};
        
        int seleccion = JOptionPane.showOptionDialog(
            frame,
            mensaje,
            "Resultado Final",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
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

    @Override
    public Stat getStats() {
        return new Stat("aciertos", "Aciertos en " + maxJugadas + " tiradas", aciertos);
    }

    @Override
    public void setGameListener(IGameListener listener) {
        this.listener = listener;
    }
}
