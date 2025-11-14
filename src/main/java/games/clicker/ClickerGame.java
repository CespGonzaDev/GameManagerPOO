/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package games.clicker;
import model.*;
/**
 *
 * @author danie
 */


import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class ClickerGame implements IGameFunction {
    private static ClickerGame instance;
    private IGameListener listener;
    private int clickCount = 0;
    private int duration = 10; // segundos

    private ClickerGame() {}

    public static ClickerGame getInstance() {
        if (instance == null) instance = new ClickerGame();
        return instance;
    }

    @Override
    public void iniciar() {
        JFrame frame = new JFrame("Clicker Game");
        JButton button = new JButton("¡Clic aquí!");
        JLabel label = new JLabel("Tiempo restante: " + duration);
        frame.setLayout(new java.awt.FlowLayout());
        frame.add(label);
        frame.add(button);

        button.addActionListener(e -> clickCount++);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int timeLeft = duration;
            @Override
            public void run() {
                timeLeft--;
                label.setText("Tiempo restante: " + timeLeft);

                if (timeLeft <= 0) {
                    timer.cancel();
                    frame.dispose();
                    if (listener != null) {
                        Stat stats = new Stat("clicks", "Clicks totales", clickCount);
                        listener.onGameFinished(stats);
                    }
                }
            }
        }, 1000, 1000);

        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
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
