/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package records;

import model.Stat;
import java.io.*;
import java.util.*;

/**
 *
 * @author danie
 */

/**
 * Gestor de records del sistema de juegos.
 * Almacena y recupera los mejores puntajes de cada juego en archivos.
 * Mantiene solo los tres mejores resultados por juego.
 */
public class RecordsManager {
    private static final String RECORDS_DIR = "records/";
    private static final int MAX_RECORDS = 3;
    private Map<String, List<Stat>> recordsPorJuego;

    /**
     * Constructor. Inicializa el gestor y carga los records existentes.
     */
    public RecordsManager() {
        recordsPorJuego = new HashMap<>();
        crearDirectorioSiNoExiste();
        cargarRecords();
    }

    /**
     * Crea el directorio de records si no existe.
     */
    private void crearDirectorioSiNoExiste() {
        File dir = new File(RECORDS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Registra un nuevo record para un juego específico.
     * Si el puntaje merece estar en el top 3, actualiza la lista y guarda en archivo.
     *
     * @param nombreJuego Nombre del juego.
     * @param stat        Estadística del resultado obtenido.
     */
    public void registrarRecord(String nombreJuego, Stat stat) {
        System.out.println("DEBUG: Registrando record para juego: '" + nombreJuego + "'");
        // Obtener lista actual de records para este juego
        List<Stat> records = recordsPorJuego.getOrDefault(nombreJuego, new ArrayList<>());

        // Agregar el nuevo resultado
        records.add(stat);

        // Ordenar de mayor a menor por valor
        records.sort((s1, s2) -> Integer.compare(s2.getValor(), s1.getValor()));

        // Mantener solo los 3 mejores
        if (records.size() > MAX_RECORDS) {
            records = records.subList(0, MAX_RECORDS);
        }

        // Actualizar la lista en memoria
        recordsPorJuego.put(nombreJuego, records);

        // Guardar en archivo
        guardarRecords(nombreJuego);
    }

    /**
     * Obtiene la lista de los mejores records de un juego específico.
     *
     * @param nombreJuego Nombre del juego.
     * @return Lista con los mejores records (hasta 3).
     */
    public List<Stat> getMejoresRecords(String nombreJuego) {
        return recordsPorJuego.getOrDefault(nombreJuego, new ArrayList<>());
    }

    /**
     * Guarda los records de un juego específico en un archivo.
     * Manejo de excepciones: captura errores de escritura de archivos.
     *
     * @param nombreJuego Nombre del juego.
     */
    private void guardarRecords(String nombreJuego) {
        String nombreArchivo = RECORDS_DIR + nombreJuego + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            List<Stat> records = recordsPorJuego.get(nombreJuego);
            for (Stat stat : records) {
                // Formato: clave|nombre|valor
                writer.write(stat.getClave() + "|" + stat.getNombre() + "|" + stat.getValor());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar records del juego " + nombreJuego + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga todos los records desde los archivos existentes.
     * Manejo de excepciones: captura errores de lectura de archivos y datos inválidos.
     */
    private void cargarRecords() {
        File dir = new File(RECORDS_DIR);
        File[] archivos = dir.listFiles((d, name) -> name.endsWith(".txt"));

        if (archivos == null) return;

        for (File archivo : archivos) {
            String nombreJuego = archivo.getName().replace(".txt", "");
            List<Stat> records = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    try {
                        // Parsear formato: clave|nombre|valor
                        String[] partes = linea.split("\\|");
                        if (partes.length == 3) {
                            String clave = partes[0];
                            String nombre = partes[1];
                            int valor = Integer.parseInt(partes[2]);
                            records.add(new Stat(clave, nombre, valor));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Dato inválido en archivo " + archivo.getName() + ": " + linea);
                    }
                }
                recordsPorJuego.put(nombreJuego, records);
            } catch (IOException e) {
                System.err.println("Error al cargar records del archivo " + archivo.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Limpia todos los records de un juego específico.
     *
     * @param nombreJuego Nombre del juego.
     */
    public void limpiarRecords(String nombreJuego) {
        recordsPorJuego.remove(nombreJuego);
        File archivo = new File(RECORDS_DIR + nombreJuego + ".txt");
        if (archivo.exists()) {
            archivo.delete();
        }
    }

    /**
     * Limpia todos los records del sistema.
     */
    public void limpiarTodosLosRecords() {
        recordsPorJuego.clear();
        File dir = new File(RECORDS_DIR);
        File[] archivos = dir.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                archivo.delete();
            }
        }
    }
}
