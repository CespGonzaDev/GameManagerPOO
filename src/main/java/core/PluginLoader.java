package core;

import model.IGameFunction;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Enumeration;

/**
 * Gestor de carga dinámica de juegos desde archivos JAR externos.
 * Busca archivos JAR en un directorio específico, carga las clases que implementan
 * IGameFunction y las retorna para integración con el sistema.
 */
public class PluginLoader {
    private File pluginsDir;

    /**
     * Constructor.
     *
     * @param pluginsDir Directorio donde se encuentran los archivos JAR de plugins.
     */
    public PluginLoader(File pluginsDir) {
        this.pluginsDir = pluginsDir;
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
    }

    /**
     * Carga todos los plugins (juegos) desde archivos JAR en el directorio configurado.
     * Manejo de excepciones: captura errores de carga de clases y archivos JAR inválidos.
     *
     * @return Lista de instancias de juegos que implementan IGameFunction.
     */
    public List<IGameFunction> cargarPlugins() {
        List<IGameFunction> plugins = new ArrayList<>();

        // Buscar archivos JAR en el directorio
        File[] archivosJar = pluginsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        if (archivosJar == null || archivosJar.length == 0) {
            System.out.println("No se encontraron archivos JAR en el directorio de plugins.");
            return plugins;
        }

        // Procesar cada archivo JAR
        for (File archivoJar : archivosJar) {
            try {
                plugins.addAll(cargarDesdeJar(archivoJar));
            } catch (Exception e) {
                System.err.println("Error al cargar plugin desde " + archivoJar.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Total de plugins cargados: " + plugins.size());
        return plugins;
    }

    /**
     * Carga juegos desde un archivo JAR específico.
     * Explora todas las clases del JAR y retorna aquellas que implementan IGameFunction.
     *
     * @param archivoJar Archivo JAR a procesar.
     * @return Lista de instancias de juegos encontrados en el JAR.
     * @throws Exception Si ocurre un error al procesar el JAR o cargar las clases.
     */
    private List<IGameFunction> cargarDesdeJar(File archivoJar) throws Exception {
        List<IGameFunction> juegos = new ArrayList<>();

        // Crear ClassLoader para el JAR
        URL jarUrl = archivoJar.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());

        try {
            // Abrir el archivo JAR y explorar sus entradas
            try (JarFile jarFile = new JarFile(archivoJar)) {
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String nombreEntrada = entry.getName();

                    // Filtrar solo archivos .class
                    if (nombreEntrada.endsWith(".class")) {
                        // Convertir ruta a nombre de clase
                        String nombreClase = nombreEntrada.replace('/', '.').replace(".class", "");

                        try {
                            // Cargar la clase
                            Class<?> clase = classLoader.loadClass(nombreClase);

                            // Verificar si implementa IGameFunction
                            if (IGameFunction.class.isAssignableFrom(clase) && !clase.isInterface()) {
                                // Instanciar el juego
                                IGameFunction instancia = instanciarJuego(clase);
                                if (instancia != null) {
                                    juegos.add(instancia);
                                    System.out.println("Plugin cargado exitosamente: " + nombreClase);
                                }
                            }
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            // Ignorar clases que no se pueden cargar
                        }
                    }
                }
            }
        } finally {
            // Cerrar el classLoader para evitar resource leak
            classLoader.close();
        }

        return juegos;
    }

    /**
     * Intenta instanciar un juego usando el patrón Singleton (método getInstance).
     * Si no existe, usa el constructor por defecto.
     *
     * @param clase Clase del juego a instanciar.
     * @return Instancia del juego, o null si no se pudo crear.
     */
    private IGameFunction instanciarJuego(Class<?> clase) {
        try {
            // Intentar obtener instancia mediante método getInstance (Singleton)
            try {
                return (IGameFunction) clase.getMethod("getInstance").invoke(null);
            } catch (NoSuchMethodException e) {
                // Si no tiene getInstance, usar constructor por defecto
                return (IGameFunction) clase.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            System.err.println("No se pudo instanciar la clase " + clase.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el directorio de plugins configurado.
     *
     * @return Directorio de plugins.
     */
    public File getPluginsDir() {
        return pluginsDir;
    }

    /**
     * Cambia el directorio de plugins.
     *
     * @param pluginsDir Nuevo directorio de plugins.
     */
    public void setPluginsDir(File pluginsDir) {
        this.pluginsDir = pluginsDir;
    }
}
