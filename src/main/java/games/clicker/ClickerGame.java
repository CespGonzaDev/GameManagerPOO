package games.clicker;

import model.*;
import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ClickerGame implements IGameFunction {
    private static ClickerGame instance;
    private IGameListener listener;
    private int clickCount = 0;
    private int duration = 10;
    private JFrame frame;
    private JButton btnClick;
    private JLabel lblTiempo;
    private JLabel lblClicks;
    private Timer timer;

    private ClickerGame() {}

    public static ClickerGame getInstance() {
        if (instance == null) instance = new ClickerGame();
        return instance;
    }

    @Override
    public void iniciar() {
        clickCount = 0;
        
        frame = new JFrame("Clicker Game");
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(240, 248, 255));

        // Panel superior con información
        JPanel panelInfo = new JPanel(new GridLayout(2, 1, 5, 5));
        panelInfo.setBackground(new Color(70, 130, 180));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblTiempo = new JLabel("Tiempo restante: " + duration + "s", SwingConstants.CENTER);
        lblTiempo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTiempo.setForeground(Color.WHITE);

        lblClicks = new JLabel("Clics: 0", SwingConstants.CENTER);
        lblClicks.setFont(new Font("Arial", Font.BOLD, 16));
        lblClicks.setForeground(Color.WHITE);

        panelInfo.add(lblTiempo);
        panelInfo.add(lblClicks);

        // Botón central grande
        btnClick = new JButton("CLIC AQUÍ");
        btnClick.setFont(new Font("Arial", Font.BOLD, 28));
        btnClick.setBackground(new Color(60, 179, 113));
        btnClick.setForeground(Color.WHITE);
        btnClick.setFocusPainted(false);
        btnClick.setBorder(BorderFactory.createRaisedBevelBorder());
        btnClick.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnClick.addActionListener(e -> {
            clickCount++;
            lblClicks.setText("Clics: " + clickCount);
            btnClick.setBackground(new Color(46, 139, 87));
        });

        btnClick.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnClick.setBackground(new Color(60, 179, 113));
            }
        });

        frame.add(panelInfo, BorderLayout.NORTH);
        frame.add(btnClick, BorderLayout.CENTER);

        // Iniciar temporizador
        iniciarTemporizador();

        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void iniciarTemporizador() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            int timeLeft = duration;
            @Override
            public void run() {
                timeLeft--;
                lblTiempo.setText("Tiempo restante: " + timeLeft + "s");

                if (timeLeft <= 0) {
                    timer.cancel();
                    SwingUtilities.invokeLater(() -> terminarJuego());
                }
            }
        }, 1000, 1000);
    }

    private void terminarJuego() {
        btnClick.setEnabled(false);
        
        String mensaje = "¡Juego terminado!\n\nTotal de clics: " + clickCount;
        String[] opciones = {"Jugar de nuevo", "Salir"};
        
        int seleccion = JOptionPane.showOptionDialog(
            frame,
            mensaje,
            "Resultado",
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
        return new Stat("clicks", "Clicks totales", clickCount);
    }

    @Override
    public void setGameListener(IGameListener listener) {
        this.listener = listener;
    }
}
