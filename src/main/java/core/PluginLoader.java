package core;

import model.IGameFunction;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Enumeration;

/**
 * Gestor de carga dinámica de juegos desde archivos JAR externos.
 * Compatible con JARs de diferentes estructuras de paquetes.
 */
public class PluginLoader {
    private File pluginsDir;
    private static Map<IGameFunction, String> nombresOriginales = new HashMap<>();
    private static Map<IGameFunction, ClassLoader> classLoaders = new HashMap<>();

    public PluginLoader(File pluginsDir) {
        this.pluginsDir = pluginsDir;
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
    }

    /**
     * Obtiene el nombre original de un juego cargado dinámicamente.
     */
    public static String getNombreOriginal(IGameFunction juego) {
        return nombresOriginales.get(juego);
    }

    public List<IGameFunction> cargarPlugins() {
        List<IGameFunction> plugins = new ArrayList<>();
        nombresOriginales.clear();
        classLoaders.clear();

        File[] archivosJar = pluginsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        if (archivosJar == null || archivosJar.length == 0) {
            System.out.println("No se encontraron archivos JAR en el directorio de plugins.");
            return plugins;
        }

        for (File archivoJar : archivosJar) {
            System.out.println("\n=== Procesando JAR: " + archivoJar.getName() + " ===");
            try {
                List<IGameFunction> juegosDelJar = cargarDesdeJar(archivoJar);
                
                for (IGameFunction juego : juegosDelJar) {
                    if (juego != null) {
                        plugins.add(juego);
                    }
                }
                
                if (juegosDelJar.isEmpty()) {
                    System.out.println("WARNING: No se encontraron juegos compatibles en " + archivoJar.getName());
                } else {
                    System.out.println("SUCCESS: Cargados " + juegosDelJar.size() + " juego(s) desde " + archivoJar.getName());
                }
            } catch (Exception e) {
                System.err.println("ERROR al procesar " + archivoJar.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== RESUMEN: Total de plugins cargados exitosamente: " + plugins.size() + " ===");
        return plugins;
    }

    @SuppressWarnings("resource")
    private List<IGameFunction> cargarDesdeJar(File archivoJar) {
        List<IGameFunction> juegos = new ArrayList<>();

        try {
            URL jarUrl = archivoJar.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());

            try (JarFile jarFile = new JarFile(archivoJar)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                List<String> clasesEncontradas = new ArrayList<>();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String nombreEntrada = entry.getName();

                    if (nombreEntrada.endsWith(".class") && !nombreEntrada.contains("$")) {
                        String nombreClase = nombreEntrada.replace('/', '.').replace(".class", "");
                        clasesEncontradas.add(nombreClase);
                    }
                }

                System.out.println("Clases encontradas: " + clasesEncontradas.size());

                for (String nombreClase : clasesEncontradas) {
                    if (nombreClase.toLowerCase().contains("listener") || 
                        nombreClase.toLowerCase().contains("stat") ||
                        nombreClase.toLowerCase().contains("function") ||
                        nombreClase.toLowerCase().contains("interface")) {
                        continue;
                    }

                    try {
                        Class<?> clase = classLoader.loadClass(nombreClase);
                        
                        if (tieneMetodosDeJuego(clase)) {
                            System.out.println("  -> Candidato: " + nombreClase);
                            
                            Object instancia = instanciarJuego(clase);
                            if (instancia != null) {
                                String nombreSimple = clase.getSimpleName();
                                
                                IGameFunction juegoAdaptado = crearAdaptador(instancia, clase, nombreSimple, classLoader);
                                if (juegoAdaptado != null) {
                                    try {
                                        model.Stat testStat = juegoAdaptado.getStats();
                                        if (testStat != null) {
                                            juegos.add(juegoAdaptado);
                                            nombresOriginales.put(juegoAdaptado, nombreSimple);
                                            classLoaders.put(juegoAdaptado, classLoader);
                                            System.out.println("  -> SUCCESS: Plugin cargado: " + nombreClase);
                                        }
                                    } catch (Exception e) {
                                        System.out.println("  -> FAIL: Error al validar " + nombreClase);
                                    }
                                }
                            }
                        }
                    } catch (NoClassDefFoundError e) {
                        System.out.println("  -> SKIP: " + nombreClase + " (dependencias faltantes)");
                    } catch (Exception e) {
                        // Silenciar
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR crítico al procesar JAR: " + e.getMessage());
        }

        return juegos;
    }

    private boolean tieneMetodosDeJuego(Class<?> clase) {
        if (clase.isInterface() || clase.isEnum() || clase.isAnnotation()) {
            return false;
        }

        try {
            boolean tieneIniciar = false;
            boolean tieneGetStats = false;
            boolean tieneSetListener = false;

            for (Method metodo : clase.getMethods()) {
                if (metodo.getName().equals("iniciar") && metodo.getParameterCount() == 0) {
                    tieneIniciar = true;
                }
                if (metodo.getName().equals("getStats") && metodo.getParameterCount() == 0) {
                    tieneGetStats = true;
                }
                if (metodo.getName().equals("setGameListener") && metodo.getParameterCount() == 1) {
                    tieneSetListener = true;
                }
            }

            return tieneIniciar && tieneGetStats && tieneSetListener;
        } catch (Exception e) {
            return false;
        }
    }

    private IGameFunction crearAdaptador(Object juegoExterno, Class<?> claseJuego, String nombreOriginal, ClassLoader jarClassLoader) {
        return new IGameFunction() {
            private Object instanciaActual = juegoExterno;
            private ClassLoader gameClassLoader = jarClassLoader;
            
            @Override
            public void iniciar() {
                // Guardar el ClassLoader actual del hilo
                Thread currentThread = Thread.currentThread();
                ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                
                try {
                    // Establecer el ClassLoader del JAR como contexto
                    currentThread.setContextClassLoader(gameClassLoader);
                    
                    System.out.println("DEBUG: Iniciando juego externo: " + nombreOriginal);
                    System.out.println("DEBUG: ClassLoader configurado: " + gameClassLoader.getClass().getName());
                    
                    // Intentar crear una nueva instancia para cada partida
                    try {
                        Method getInstance = claseJuego.getMethod("getInstance");
                        instanciaActual = getInstance.invoke(null);
                        System.out.println("DEBUG: Nueva instancia obtenida via getInstance()");
                    } catch (NoSuchMethodException e) {
                        try {
                            instanciaActual = claseJuego.getDeclaredConstructor().newInstance();
                            System.out.println("DEBUG: Nueva instancia creada via constructor");
                        } catch (Exception ex) {
                            System.out.println("DEBUG: Usando instancia existente");
                        }
                    }
                    
                    Method metodo = claseJuego.getMethod("iniciar");
                    metodo.invoke(instanciaActual);
                    System.out.println("DEBUG: Juego iniciado correctamente: " + nombreOriginal);
                    
                } catch (Exception e) {
                    System.err.println("ERROR al iniciar juego " + nombreOriginal + ": " + e.getMessage());
                    e.printStackTrace();
                    
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            null,
                            "Error al iniciar el juego " + nombreOriginal + ":\n" + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                } finally {
                    // Restaurar el ClassLoader original
                    currentThread.setContextClassLoader(originalClassLoader);
                }
            }

            @Override
            public model.Stat getStats() {
                Thread currentThread = Thread.currentThread();
                ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                
                try {
                    currentThread.setContextClassLoader(gameClassLoader);
                    
                    Method metodo = claseJuego.getMethod("getStats");
                    Object statExterno = metodo.invoke(instanciaActual);
                    
                    if (statExterno == null) {
                        return new model.Stat("puntos", "Puntos", 0);
                    }
                    
                    Class<?> statClass = statExterno.getClass();
                    Method getClave = statClass.getMethod("getClave");
                    Method getNombre = statClass.getMethod("getNombre");
                    Method getValor = statClass.getMethod("getValor");
                    
                    String clave = (String) getClave.invoke(statExterno);
                    String nombre = (String) getNombre.invoke(statExterno);
                    
                    Object valorObj = getValor.invoke(statExterno);
                    int valor = 0;
                    if (valorObj instanceof Integer) {
                        valor = (Integer) valorObj;
                    } else if (valorObj instanceof String) {
                        try {
                            valor = Integer.parseInt((String) valorObj);
                        } catch (NumberFormatException e) {
                            valor = 0;
                        }
                    }
                    
                    return new model.Stat(clave, nombre, valor);
                } catch (Exception e) {
                    System.err.println("Error al obtener stats de " + nombreOriginal + ": " + e.getMessage());
                    return new model.Stat("error", "Error", 0);
                } finally {
                    currentThread.setContextClassLoader(originalClassLoader);
                }
            }

            @Override
            public void setGameListener(model.IGameListener listener) {
                Thread currentThread = Thread.currentThread();
                ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                
                try {
                    currentThread.setContextClassLoader(gameClassLoader);
                    
                    System.out.println("DEBUG: Configurando listener para " + nombreOriginal);
                    
                    Method setListenerMethod = null;
                    Class<?> listenerParamType = null;
                    
                    for (Method m : claseJuego.getMethods()) {
                        if (m.getName().equals("setGameListener") && m.getParameterCount() == 1) {
                            setListenerMethod = m;
                            listenerParamType = m.getParameterTypes()[0];
                            break;
                        }
                    }
                    
                    if (setListenerMethod == null) {
                        System.err.println("WARN: No se encontró método setGameListener en " + nombreOriginal);
                        return;
                    }
                    
                    System.out.println("DEBUG: Creando proxy listener para " + listenerParamType.getName());
                    
                    final Method finalSetListenerMethod = setListenerMethod;
                    
                    Object listenerProxy = java.lang.reflect.Proxy.newProxyInstance(
                        gameClassLoader,
                        new Class<?>[]{listenerParamType},
                        (proxy, method, args) -> {
                            System.out.println("DEBUG: Método invocado en listener: " + method.getName());
                            
                            if (method.getName().equals("onGameFinished") && args != null && args.length > 0) {
                                try {
                                    Object statExterno = args[0];
                                    Class<?> statClass = statExterno.getClass();
                                    
                                    Method getClave = statClass.getMethod("getClave");
                                    Method getNombre = statClass.getMethod("getNombre");
                                    Method getValor = statClass.getMethod("getValor");
                                    
                                    String clave = (String) getClave.invoke(statExterno);
                                    String nombre = (String) getNombre.invoke(statExterno);
                                    
                                    Object valorObj = getValor.invoke(statExterno);
                                    int valor = 0;
                                    if (valorObj instanceof Integer) {
                                        valor = (Integer) valorObj;
                                    } else if (valorObj instanceof String) {
                                        valor = Integer.parseInt((String) valorObj);
                                    }
                                    
                                    model.Stat statInterno = new model.Stat(clave, nombre, valor);
                                    System.out.println("DEBUG: Notificando fin de juego con stats: " + valor);
                                    listener.onGameFinished(statInterno);
                                } catch (Exception e) {
                                    System.err.println("ERROR en listener proxy: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    );
                    
                    finalSetListenerMethod.invoke(instanciaActual, listenerProxy);
                    System.out.println("DEBUG: Listener configurado correctamente para " + nombreOriginal);
                    
                } catch (Exception e) {
                    System.err.println("ERROR al configurar listener para " + nombreOriginal + ": " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    currentThread.setContextClassLoader(originalClassLoader);
                }
            }
            
            @Override
            public String toString() {
                return nombreOriginal;
            }
        };
    }

    private Object instanciarJuego(Class<?> clase) {
        try {
            try {
                Method getInstance = clase.getMethod("getInstance");
                return getInstance.invoke(null);
            } catch (NoSuchMethodException e) {
                return clase.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public File getPluginsDir() {
        return pluginsDir;
    }

    public void setPluginsDir(File pluginsDir) {
        this.pluginsDir = pluginsDir;
    }
}
