package GUI;

import core.GameManager;
import model.IGameFunction;
import model.IGameListener;
import model.Stat;
import records.RecordsManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UI extends JFrame {
    private JDesktopPane desktopPane;
    private RecordsManager recordsManager;
    private GameManager gameManager;
    private Map<String, IGameFunction> juegosMap;
    private JPanel panelJuegos;

    public UI() {
        super("Gestor de Juegos - Plataforma Profesional");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        recordsManager = new RecordsManager();
        gameManager = GameManager.getInstance();
        juegosMap = new HashMap<>();

        setLayout(new BorderLayout());

        JPanel panelLateral = crearPanelLateral();
        add(panelLateral, BorderLayout.WEST);

        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(245, 245, 250));
        add(desktopPane, BorderLayout.CENTER);

        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);

        setVisible(true);
        // MÉTODO TEMPORAL DE PRUEBA - Eliminar después de las pruebas
        probarJuegoExterno();
    }
    // MÉTODO TEMPORAL DE PRUEBA - Agregar después del constructor
    private void probarJuegoExterno() {
        System.out.println("\n=== PRUEBA DE JUEGO EXTERNO ===");
        
        for (Map.Entry<String, IGameFunction> entrada : juegosMap.entrySet()) {
            String nombre = entrada.getKey();
            IGameFunction juego = entrada.getValue();
            
            // Verificar si es externo
            if (core.PluginLoader.getNombreOriginal(juego) != null) {
                System.out.println("\nProbando juego externo: " + nombre);
                System.out.println("  - Clase: " + juego.getClass().getName());
                
                try {
                    // Probar getStats
                    Stat stats = juego.getStats();
                    System.out.println("  - getStats() funciona: " + stats.getClave());
                    
                    // Probar setGameListener
                    juego.setGameListener(stat -> {
                        System.out.println("  - Listener funciona correctamente");
                    });
                    System.out.println("  - setGameListener() funciona");
                    
                } catch (Exception e) {
                    System.err.println("  - ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("\n=== FIN DE PRUEBA ===\n");
    }


    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("PLATAFORMA DE JUEGOS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        JButton btnRecords = crearBotonHerramienta("Ver Records", new Color(46, 204, 113));
        JButton btnRecargar = crearBotonHerramienta("Recargar Plugins", new Color(241, 196, 15));

        btnRecords.addActionListener(e -> mostrarRecords());
        btnRecargar.addActionListener(e -> {
            gameManager.recargarPlugins();
            actualizarListaJuegos();
            JOptionPane.showMessageDialog(this, "Plugins recargados correctamente", 
                "Exito", JOptionPane.INFORMATION_MESSAGE);
        });

        panelBotones.add(btnRecords);
        panelBotones.add(btnRecargar);

        panel.add(lblTitulo, BorderLayout.WEST);
        panel.add(panelBotones, BorderLayout.EAST);

        return panel;
    }

    private JButton crearBotonHerramienta(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }

    private JPanel crearPanelLateral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblTitulo = new JLabel("JUEGOS DISPONIBLES");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(236, 240, 241));
        lblTitulo.setBorder(new EmptyBorder(0, 0, 15, 0));

        panelJuegos = new JPanel();
        panelJuegos.setLayout(new BoxLayout(panelJuegos, BoxLayout.Y_AXIS));
        panelJuegos.setBackground(new Color(52, 73, 94));

        actualizarListaJuegos();

        JScrollPane scrollPane = new JScrollPane(panelJuegos);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(52, 73, 94));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarListaJuegos() {
        panelJuegos.removeAll();
        juegosMap.clear();

        List<IGameFunction> juegosDisponibles = gameManager.getJuegosDisponibles();

        for (IGameFunction juego : juegosDisponibles) {
            if (juego == null) {
                System.err.println("Juego nulo detectado, omitiendo...");
                continue;
            }

            String nombreJuego = obtenerNombreJuego(juego);
            
            if (nombreJuego == null || nombreJuego.trim().isEmpty()) {
                System.err.println("Juego sin nombre detectado: " + juego.getClass().getName());
                continue;
            }

            juegosMap.put(nombreJuego, juego);

            JButton btnJuego = crearBotonJuego(nombreJuego);
            btnJuego.addActionListener(e -> lanzarJuego(nombreJuego, juego));
            
            panelJuegos.add(btnJuego);
            panelJuegos.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        panelJuegos.revalidate();
        panelJuegos.repaint();
        
        System.out.println("Juegos cargados en UI: " + juegosMap.size());
    }

    private JButton crearBotonJuego(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(new Color(41, 128, 185));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(41, 128, 185));
            }
        });

        return btn;
    }

    private String obtenerNombreJuego(IGameFunction juego) {
        // Primero intentar obtener el nombre del PluginLoader (juegos externos)
        String nombreOriginal = core.PluginLoader.getNombreOriginal(juego);
        if (nombreOriginal != null && !nombreOriginal.isEmpty()) {
            return nombreOriginal;
        }
        
        // Si es juego interno, usar switch normal
        String nombreCompleto = juego.getClass().getSimpleName();
        switch (nombreCompleto) {
            case "ClickerGame": return "Clicker";
            case "Dice": return "Dados";
            case "Tictactoe": return "Tres en Raya";
            default: return nombreCompleto;
        }
    }

    /**
     * Lanza un juego en su propia ventana independiente.
     * Funciona tanto para juegos internos como externos.
     */

    private void lanzarJuego(String nombre, IGameFunction juego) {
        System.out.println("=== Lanzando juego: " + nombre + " ===");
        
        // Verificar si es un juego externo
        boolean esJuegoExterno = core.PluginLoader.getNombreOriginal(juego) != null;
        
        if (esJuegoExterno) {
            // Para juegos externos: mostrar mensaje informativo
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "Los juegos externos del JAR tienen limitaciones de compatibilidad.\n\n" +
                "El juego '" + nombre + "' puede no funcionar correctamente debido a\n" +
                "restricciones de diseño (Singleton, ventanas ya creadas, etc.).\n\n" +
                "¿Deseas intentar ejecutarlo de todos modos?",
                "Advertencia - Juego Externo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        try {
            juego.setGameListener(new IGameListener() {
                @Override
                public void onGameFinished(Stat stats) {
                    System.out.println("Juego finalizado: " + nombre + " - Puntos: " + stats.getValor());
                    recordsManager.registrarRecord(nombre, stats);
                    
                    SwingUtilities.invokeLater(() -> {
                        String mensaje = String.format(
                            "Partida finalizada\n\nJuego: %s\nPuntos: %d\n\nRecord guardado correctamente",
                            nombre, stats.getValor()
                        );
                        JOptionPane.showMessageDialog(
                            UI.this,
                            mensaje,
                            "Resultado guardado",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    });
                }
            });
            
            System.out.println("Listener configurado, iniciando juego...");
            juego.iniciar();
            System.out.println("Comando iniciar() ejecutado");
            
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al lanzar juego " + nombre);
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(
                this,
                "Error al iniciar el juego " + nombre + ":\n\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }



    /**
     * Muestra los records en un JInternalFrame con JTable.
     */
    private void mostrarRecords() {
        JInternalFrame frame = new JInternalFrame("Records y Estadisticas", true, true, true, true);
        frame.setSize(650, 400);
        frame.setLayout(new BorderLayout());

        String[] columnas = {"Juego", "Categoria", "Descripcion", "Puntos"};
        int totalJuegos = juegosMap.size();
        Object[][] datos = new Object[totalJuegos * 3][4];
        int index = 0;

        for (String nombreJuego : juegosMap.keySet()) {
            List<Stat> records = recordsManager.getMejoresRecords(nombreJuego);
            for (Stat stat : records) {
                datos[index][0] = nombreJuego;
                datos[index][1] = stat.getClave();
                datos[index][2] = stat.getNombre();
                datos[index][3] = stat.getValor();
                index++;
            }
        }

        JTable table = new JTable(datos, columnas);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(174, 214, 241));
        table.setGridColor(new Color(189, 195, 199));

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setLocation(50, 50);
        desktopPane.add(frame);
        frame.setVisible(true);
        
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
    }
}
