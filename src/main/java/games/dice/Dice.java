package games.dice;

import model.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Dice implements IGameFunction {
    private static Dice instance;
    private IGameListener listener;
    private int puntosTotal;
    private int ronda;
    private int maxRondas = 5;
    private JFrame frame;
    private JLabel lblDado1;
    private JLabel lblDado2;
    private JLabel lblPuntos;
    private JLabel lblRonda;
    private JPanel panelApuestas;
    private List<String> apuestasSeleccionadas;
    private int dado1Valor;
    private int dado2Valor;

    private Dice() {}

    public static Dice getInstance() {
        if (instance == null) instance = new Dice();
        return instance;
    }

    @Override
    public void iniciar() {
        puntosTotal = 0;
        ronda = 0;
        apuestasSeleccionadas = new ArrayList<>();

        frame = new JFrame("Juego de Dados Avanzado");
        frame.setLayout(new BorderLayout(15, 15));
        frame.getContentPane().setBackground(new Color(26, 188, 156));

        JPanel panelInfo = crearPanelInformacion();
        frame.add(panelInfo, BorderLayout.NORTH);

        JPanel panelDados = crearPanelDados();
        frame.add(panelDados, BorderLayout.CENTER);

        panelApuestas = crearPanelApuestas();
        frame.add(panelApuestas, BorderLayout.SOUTH);

        frame.setSize(600, 650);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        nuevaRonda();
    }

    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBackground(new Color(22, 160, 133));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        lblRonda = new JLabel("Ronda: 1/" + maxRondas, SwingConstants.CENTER);
        lblRonda.setFont(new Font("Arial", Font.BOLD, 20));
        lblRonda.setForeground(Color.WHITE);

        lblPuntos = new JLabel("Puntos totales: 0", SwingConstants.CENTER);
        lblPuntos.setFont(new Font("Arial", Font.BOLD, 18));
        lblPuntos.setForeground(new Color(241, 196, 15));

        panel.add(lblRonda);
        panel.add(lblPuntos);

        return panel;
    }

    private JPanel crearPanelDados() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(26, 188, 156));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        lblDado1 = crearEtiquetaDado();
        lblDado2 = crearEtiquetaDado();

        panel.add(lblDado1);
        panel.add(lblDado2);

        return panel;
    }

    private JLabel crearEtiquetaDado() {
        JLabel lbl = new JLabel("?", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 80));
        lbl.setForeground(new Color(44, 62, 80));
        lbl.setBackground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94), 3));
        lbl.setPreferredSize(new Dimension(120, 120));
        return lbl;
    }

    private JPanel crearPanelApuestas() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(236, 240, 241));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Selecciona tus apuestas:", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(44, 62, 80));

        JPanel panelBotones = new JPanel(new GridLayout(3, 2, 10, 10));
        panelBotones.setBackground(new Color(236, 240, 241));

        JToggleButton btnSumaPar = crearBotonApuesta("Suma Par (1pt)", new Color(52, 152, 219));
        JToggleButton btnSumaImpar = crearBotonApuesta("Suma Impar (1pt)", new Color(52, 152, 219));
        JToggleButton btnExactoUno = crearBotonApuesta("Num. Exacto (un dado) (6pts)", new Color(230, 126, 34));
        JToggleButton btnExactoDos = crearBotonApuesta("Num. Exacto (ambos dados) (12pts)", new Color(231, 76, 60));
        JToggleButton btnDobles = crearBotonApuesta("Dados Dobles (8pts)", new Color(155, 89, 182));
        JToggleButton btnSumaAlta = crearBotonApuesta("Suma mayor o igual a 8 (3pts)", new Color(46, 204, 113));

        panelBotones.add(btnSumaPar);
        panelBotones.add(btnSumaImpar);
        panelBotones.add(btnExactoUno);
        panelBotones.add(btnExactoDos);
        panelBotones.add(btnDobles);
        panelBotones.add(btnSumaAlta);

        JPanel panelControl = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelControl.setBackground(new Color(236, 240, 241));

        JButton btnLanzar = new JButton("LANZAR DADOS");
        btnLanzar.setFont(new Font("Arial", Font.BOLD, 16));
        btnLanzar.setBackground(new Color(39, 174, 96));
        btnLanzar.setForeground(Color.WHITE);
        btnLanzar.setFocusPainted(false);
        btnLanzar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLanzar.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));

        btnLanzar.addActionListener(e -> {
            apuestasSeleccionadas.clear();
            if (btnSumaPar.isSelected()) apuestasSeleccionadas.add("suma_par");
            if (btnSumaImpar.isSelected()) apuestasSeleccionadas.add("suma_impar");
            if (btnExactoUno.isSelected()) {
                String numero = JOptionPane.showInputDialog(frame, "Que numero (1-6)?", "1");
                if (numero != null) apuestasSeleccionadas.add("exacto_uno:" + numero);
            }
            if (btnExactoDos.isSelected()) {
                String numero = JOptionPane.showInputDialog(frame, "Que numero en ambos dados (1-6)?", "1");
                if (numero != null) apuestasSeleccionadas.add("exacto_dos:" + numero);
            }
            if (btnDobles.isSelected()) apuestasSeleccionadas.add("dobles");
            if (btnSumaAlta.isSelected()) apuestasSeleccionadas.add("suma_alta");

            if (apuestasSeleccionadas.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Debes seleccionar al menos una apuesta");
                return;
            }

            lanzarDados();
            btnSumaPar.setSelected(false);
            btnSumaImpar.setSelected(false);
            btnExactoUno.setSelected(false);
            btnExactoDos.setSelected(false);
            btnDobles.setSelected(false);
            btnSumaAlta.setSelected(false);
        });

        panelControl.add(btnLanzar);

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(panelBotones, BorderLayout.CENTER);
        panelPrincipal.add(panelControl, BorderLayout.SOUTH);

        return panelPrincipal;
    }

    private JToggleButton crearBotonApuesta(String texto, Color color) {
        JToggleButton btn = new JToggleButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(color.darker());
            } else {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }

    private void lanzarDados() {
        dado1Valor = 1 + (int)(Math.random() * 6);
        dado2Valor = 1 + (int)(Math.random() * 6);

        lblDado1.setText(String.valueOf(dado1Valor));
        lblDado2.setText(String.valueOf(dado2Valor));

        int puntosRonda = calcularPuntos();
        puntosTotal += puntosRonda;

        lblPuntos.setText("Puntos totales: " + puntosTotal);

        String mensaje = construirMensajeResultado(puntosRonda);
        JOptionPane.showMessageDialog(frame, mensaje, "Resultado", JOptionPane.INFORMATION_MESSAGE);

        ronda++;
        if (ronda < maxRondas) {
            nuevaRonda();
        } else {
            terminarJuego();
        }
    }

    private int calcularPuntos() {
        int suma = dado1Valor + dado2Valor;
        int puntos = 0;
        boolean todasGanadas = true;

        for (String apuesta : apuestasSeleccionadas) {
            boolean gano = false;

            if (apuesta.equals("suma_par") && suma % 2 == 0) {
                puntos += 1;
                gano = true;
            } else if (apuesta.equals("suma_impar") && suma % 2 != 0) {
                puntos += 1;
                gano = true;
            } else if (apuesta.startsWith("exacto_uno:")) {
                int num = Integer.parseInt(apuesta.split(":")[1]);
                if (dado1Valor == num || dado2Valor == num) {
                    puntos += 6;
                    gano = true;
                }
            } else if (apuesta.startsWith("exacto_dos:")) {
                int num = Integer.parseInt(apuesta.split(":")[1]);
                if (dado1Valor == num && dado2Valor == num) {
                    puntos += 12;
                    gano = true;
                }
            } else if (apuesta.equals("dobles") && dado1Valor == dado2Valor) {
                puntos += 8;
                gano = true;
            } else if (apuesta.equals("suma_alta") && suma >= 8) {
                puntos += 3;
                gano = true;
            }

            if (!gano) {
                todasGanadas = false;
            }
        }

        return todasGanadas ? puntos : 0;
    }

    private String construirMensajeResultado(int puntos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resultados:\n\n");
        sb.append("Dado 1: ").append(dado1Valor).append("\n");
        sb.append("Dado 2: ").append(dado2Valor).append("\n");
        sb.append("Suma: ").append(dado1Valor + dado2Valor).append("\n\n");
        
        if (puntos > 0) {
            sb.append("Ganaste ").append(puntos).append(" puntos!\n");
            sb.append("Todas tus apuestas fueron correctas");
        } else {
            sb.append("No ganaste puntos\n");
            sb.append("Al menos una apuesta fallo");
        }

        return sb.toString();
    }

    private void nuevaRonda() {
        lblRonda.setText("Ronda: " + (ronda + 1) + "/" + maxRondas);
        lblDado1.setText("?");
        lblDado2.setText("?");
    }

    private void terminarJuego() {
        String mensaje = String.format(
            "Juego terminado!\n\nPuntos finales: %d\nRondas jugadas: %d",
            puntosTotal, maxRondas
        );

        String[] opciones = {"Jugar de nuevo", "Salir"};
        int seleccion = JOptionPane.showOptionDialog(
            frame, mensaje, "Resultado Final",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, opciones, opciones[0]
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
        return new Stat("puntos", "Puntos totales", puntosTotal);
    }

    @Override
    public void setGameListener(IGameListener listener) {
        this.listener = listener;
    }
}
