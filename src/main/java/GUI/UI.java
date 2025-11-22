package GUI;

import core.GameManager;
import model.IGameFunction;
import model.IGameListener;
import model.Stat;
import records.RecordsManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interfaz gráfica principal del gestor de juegos.
 * Carga dinámicamente todos los juegos disponibles (internos y externos).
 */
public class UI extends JFrame {
    private JDesktopPane desktopPane;
    private RecordsManager recordsManager;
    private GameManager gameManager;
    private Map<String, IGameFunction> juegosMap;

    public UI() {
        super("Gestor de Juegos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        recordsManager = new RecordsManager();
        gameManager = GameManager.getInstance();
        juegosMap = new HashMap<>();

        desktopPane = new JDesktopPane();
        setContentPane(desktopPane);

        // Crear barra de menú
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Menú Juegos - DINÁMICO
        JMenu menuJuegos = new JMenu("Juegos");
        menuBar.add(menuJuegos);

        // Cargar juegos dinámicamente desde GameManager
        cargarJuegosDinamicamente(menuJuegos);

        // Menú Records
        JMenu menuRecords = new JMenu("Records");
        menuBar.add(menuRecords);

        JMenuItem verRecords = new JMenuItem("Ver records");
        menuRecords.add(verRecords);
        verRecords.addActionListener(e -> mostrarRecords());

        // Opción para recargar plugins
        JMenu menuOpciones = new JMenu("Opciones");
        menuBar.add(menuOpciones);
        
        JMenuItem recargarPlugins = new JMenuItem("Recargar plugins externos");
        menuOpciones.add(recargarPlugins);
        recargarPlugins.addActionListener(e -> {
            gameManager.recargarPlugins();
            menuJuegos.removeAll();
            cargarJuegosDinamicamente(menuJuegos);
            JOptionPane.showMessageDialog(this, "Plugins recargados correctamente");
        });

        setVisible(true);
    }

    /**
     * Carga todos los juegos disponibles desde GameManager y los agrega al menú.
     */
    private void cargarJuegosDinamicamente(JMenu menuJuegos) {
        List<IGameFunction> juegosDisponibles = gameManager.getJuegosDisponibles();
        
        for (IGameFunction juego : juegosDisponibles) {
            String nombreJuego = obtenerNombreJuego(juego);
            juegosMap.put(nombreJuego, juego);
            
            JMenuItem itemJuego = new JMenuItem(nombreJuego);
            itemJuego.addActionListener(e -> lanzarJuego(nombreJuego, juego));
            menuJuegos.add(itemJuego);
        }
        
        System.out.println("Juegos cargados en el menú: " + juegosDisponibles.size());
    }

    /**
     * Obtiene el nombre del juego desde su clase.
     */
    private String obtenerNombreJuego(IGameFunction juego) {
        String nombreCompleto = juego.getClass().getSimpleName();
        
        // Mapeo de nombres conocidos
        switch (nombreCompleto) {
            case "ClickerGame": return "Clicker";
            case "Dice": return "Dado";
            case "Tictactoe": return "Tres en Raya";
            default: return nombreCompleto;
        }
    }

    /**
     * Lanza un juego en un JInternalFrame.
     */
    private void lanzarJuego(String nombre, IGameFunction juego) {
        JInternalFrame frame = new JInternalFrame(nombre, true, true, true, true);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        
        JLabel status = new JLabel("Jugando: " + nombre, SwingConstants.CENTER);
        status.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(status, BorderLayout.NORTH);

        // Integrar el listener para guardar el resultado
        juego.setGameListener(new IGameListener() {
            @Override
            public void onGameFinished(Stat stats) {
                recordsManager.registrarRecord(nombre, stats);
                status.setText("Partida finalizada. Resultado: " + stats.getValor());
                System.out.println("Record registrado para " + nombre + ": " + stats.getValor());
            }
        });

        // Iniciar el juego
        juego.iniciar();

        desktopPane.add(frame);
        frame.setVisible(true);
    }

    /**
     * Muestra los records en un JInternalFrame con JTable.
     */
    private void mostrarRecords() {
        JInternalFrame frame = new JInternalFrame("Records", true, true, true, true);
        frame.setSize(500, 350);

        String[] columnas = {"Juego", "Clave", "Nombre", "Valor"};
        
        // Calcular cuántas filas necesitamos (máximo 3 records por juego)
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
        table.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);
        
        desktopPane.add(frame);
        frame.setVisible(true);
    }
}
