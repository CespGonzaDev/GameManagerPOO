/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package games.dice;
import model.*;

/**
 *
 * @author danie
 */

import javax.swing.*;
import java.awt.event.*;

public class Dice implements IGameFunction {
    private static Dice instance;
    private IGameListener listener;
    private int aciertos;
    private int total;

    private Dice() {}

    public static Dice getInstance() {
        if (instance == null) instance = new Dice();
        return instance;
    }

    @Override
    public void iniciar() {
        JFrame frame = new JFrame("Juego del Dado");
        JLabel lblResultado = new JLabel("Apuesta por par o impar:");
        JButton btnPar = new JButton("Par");
        JButton btnImpar = new JButton("Impar");
        frame.setLayout(new java.awt.FlowLayout());
        frame.add(lblResultado);
        frame.add(btnPar);
        frame.add(btnImpar);

        ActionListener al = e -> {
            int apuesta = e.getSource() == btnPar ? 0 : 1;
            int dado = 1 + (int)(Math.random() * 6);
            boolean esPar = dado % 2 == 0;
            boolean acierto = (apuesta == 0 && esPar) || (apuesta == 1 && !esPar);
            total++;
            if (acierto) aciertos++;
            lblResultado.setText("Sacaste " + dado + ". " + (acierto ? "¡Acierto!" : "Fallaste.") +
                " Total: " + total + " | Aciertos: " + aciertos);

            // Juego termina (por ejemplo) tras 5 lanzamientos
            if (total >= 5) {
                frame.dispose();
                if (listener != null)
                    listener.onGameFinished(getStats());
            }
        };
        btnPar.addActionListener(al);
        btnImpar.addActionListener(al);

        frame.setSize(300,160);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    public Stat getStats() {
        return new Stat("aciertos", "Aciertos en 5 tiradas", aciertos);
    }

    @Override
    public void setGameListener(IGameListener listener) {
        this.listener = listener;
    }
}
