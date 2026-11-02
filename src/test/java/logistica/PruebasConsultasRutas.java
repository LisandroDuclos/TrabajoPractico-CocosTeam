package logistica;

import java.util.Random;

import logistica.algoritmos.dinamica.FloydWarshall;
import logistica.algoritmos.greedy.Dijkstra;
import logistica.modelo.Conexion;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.resultado.ResultadoConsultaRuta;
import logistica.resultado.ResultadoRuta;
import logistica.resultado.TablaRutas;
import logistica.sistema.SistemaLogistica;

/**
 * Casos de prueba de la funcionalidad A1 (consultas de rutas con Floyd-Warshall).
 */
public class PruebasConsultasRutas {

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas: tiempos y recorridos ===");
        pruebasConsultas();
        System.out.println("=== Pruebas: ejemplo de la clase ===");
        pruebasEjemploClase();
        System.out.println("=== Pruebas: casos borde y validaciones ===");
        pruebasBorde();
        System.out.println("=== Pruebas: cambios en la red ===");
        pruebasCambiosEnLaRed();
        System.out.println("=== Pruebas: integración con el sistema ===");
        pruebasSistema();
        System.out.println("=== Pruebas: contra Dijkstra ===");
        pruebasContraDijkstra();
        System.out.println("=== Prueba de volumen ===");
        pruebaVolumen();

        System.out.println();
        System.out.println("Resultado: " + ok + "/" + (ok + fallas) + " OK");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    /**
     * C -> N 10, C -> O 15, C -> E 25, N -> E 8, N -> S 30,
     * O -> S 12, E -> S 5, S -> C 20, S -> P 7.   (P no tiene salidas)
     */
    private static RedVial redEjemplo() {
        RedVial red = new RedVial();
        red.agregarUbicacion("C");
        red.agregarUbicacion("N");
        red.agregarUbicacion("E");
        red.agregarUbicacion("O");
        red.agregarUbicacion("S");
        red.agregarUbicacion("P");
        red.agregarConexion("C", "N", 10);
        red.agregarConexion("C", "O", 15);
        red.agregarConexion("C", "E", 25);
        red.agregarConexion("N", "E", 8);
        red.agregarConexion("N", "S", 30);
        red.agregarConexion("O", "S", 12);
        red.agregarConexion("E", "S", 5);
        red.agregarConexion("S", "C", 20);
        red.agregarConexion("S", "P", 7);
        return red;
    }

    //  Tiempos y recorridos

    private static void pruebasConsultas() {
        TablaRutas tabla = FloydWarshall.calcular(redEjemplo());

        verificar("C -> P: 30 min por C-N-E-S-P", ruta(tabla, "C", "P", 30, "C-N-E-S-P"));
        verificar("N -> C: 33 min por N-E-S-C", ruta(tabla, "N", "C", 33, "N-E-S-C"));
        verificar("el intermedio mejora a la conexión directa: C -> E 18 min", ruta(tabla, "C", "E", 18, "C-N-E"));
        verificar("conexión directa como mejor ruta: C -> N 10 min", ruta(tabla, "C", "N", 10, "C-N"));
        verificar("varios intermedios: N -> O 48 min", ruta(tabla, "N", "O", 48, "N-E-S-C-O"));
        verificar("el sentido importa: E -> N 35 min y N -> E 8 min",
                ruta(tabla, "E", "N", 35, "E-S-C-N") && ruta(tabla, "N", "E", 8, "N-E"));
        verificar("origen = destino: 0 min", ruta(tabla, "C", "C", 0, "C"));

        ResultadoConsultaRuta sinConexion = tabla.consultar("P", "C");
        verificar("P no tiene salidas: no existe conexión a C",
                !sinConexion.isExiste() && sinConexion.getCamino() == null);
        verificar("sin conexión: el mensaje lo informa",
                sinConexion.toString().equals("No existe conexión de P a C"));
    }

    //  Grafo de la clase del 9 de octubre

    private static void pruebasEjemploClase() {
        RedVial red = new RedVial();
        for (int i = 1; i <= 5; i++) {
            red.agregarUbicacion(String.valueOf(i));
        }
        red.agregarConexion("1", "2", 2);
        red.agregarConexion("1", "4", 15);
        red.agregarConexion("1", "5", 4);
        red.agregarConexion("2", "3", 3);
        red.agregarConexion("2", "5", 2);
        red.agregarConexion("3", "1", 2);
        red.agregarConexion("3", "4", 6);
        red.agregarConexion("4", "5", 3);
        red.agregarConexion("5", "4", 1);
        TablaRutas tabla = FloydWarshall.calcular(red);

        verificar("3 -> 2 pasando por 1: 4", ruta(tabla, "3", "2", 4, "3-1-2"));
        verificar("3 -> 5: 6", tabla.consultar("3", "5").getTiempoTotal() == 6);
        verificar("3 -> 4 sigue siendo directa: 6", ruta(tabla, "3", "4", 6, "3-4"));
        // 1-2-5-4 también tarda 5: ante un empate se conserva la ruta que ya estaba (1-5).
        verificar("1 -> 4 baja de 15 a 5", ruta(tabla, "1", "4", 5, "1-5-4"));
        verificar("de 4 y 5 no se llega a 1",
                !tabla.consultar("4", "1").isExiste() && !tabla.consultar("5", "1").isExiste());
    }

    //  Casos borde y validaciones

    private static void pruebasBorde() {
        TablaRutas vacia = FloydWarshall.calcular(new RedVial());
        verificar("red vacía: la tabla se calcula sin mejoras", vacia.getMejoras() == 0);

        RedVial una = new RedVial();
        una.agregarUbicacion("A");
        verificar("una sola ubicación: A -> A 0 min", ruta(FloydWarshall.calcular(una), "A", "A", 0, "A"));

        RedVial lazo = new RedVial();
        lazo.agregarUbicacion("A");
        lazo.agregarConexion("A", "A", 5);
        verificar("una conexión de A a A no cambia los 0 min", ruta(FloydWarshall.calcular(lazo), "A", "A", 0, "A"));

        RedVial aislados = new RedVial();
        aislados.agregarUbicacion("A");
        aislados.agregarUbicacion("B");
        verificar("sin conexiones: no existe conexión", !FloydWarshall.calcular(aislados).consultar("A", "B").isExiste());

        // Tiempos enormes: sumar contra "infinito" desbordaría y crearía una ruta falsa.
        RedVial grande = new RedVial();
        grande.agregarUbicacion("A");
        grande.agregarUbicacion("B");
        grande.agregarUbicacion("C");
        grande.agregarConexion("A", "B", 2_000_000_000);
        TablaRutas tablaGrande = FloydWarshall.calcular(grande);
        verificar("tiempo enorme: A -> C sigue sin conexión", !tablaGrande.consultar("A", "C").isExiste());
        verificar("tiempo enorme: A -> B se conserva", tablaGrande.consultar("A", "B").getTiempoTotal() == 2_000_000_000);

        TablaRutas tabla = FloydWarshall.calcular(redEjemplo());
        verificar("origen inexistente lanza excepción", lanza(() -> tabla.consultar("X", "C")));
        verificar("destino inexistente lanza excepción", lanza(() -> tabla.consultar("C", "X")));
    }

    //  Cambios en la red

    private static void pruebasCambiosEnLaRed() {
        RedVial red = redEjemplo();
        red.deshabilitarConexion("N", "E");
        TablaRutas tabla = FloydWarshall.calcular(red);
        verificar("conexión deshabilitada antes de calcular: C -> S por C-O-S, 27 min",
                ruta(tabla, "C", "S", 27, "C-O-S"));
        verificar("tabla recién calculada: vigente", tabla.isVigente());

        red.habilitarConexion("N", "E");
        verificar("al cambiar la red la tabla deja de estar vigente", !tabla.isVigente());
        verificar("una tabla vieja no responde: lanza excepción", lanza(() -> tabla.consultar("C", "S")));

        TablaRutas nueva = FloydWarshall.calcular(red);
        red.deshabilitarConexion("C", "P"); // no existe esa conexión
        red.habilitarConexion("N", "E");    // ya estaba habilitada
        verificar("una operación que no cambia nada mantiene la tabla vigente", nueva.isVigente());

        red.agregarUbicacion("Z");
        verificar("agregar una ubicación deja la tabla vieja", !nueva.isVigente());
        TablaRutas conZ = FloydWarshall.calcular(red);
        red.agregarConexion("P", "Z", 1);
        verificar("agregar una conexión deja la tabla vieja", !conZ.isVigente());
    }

    //  Integración con el sistema

    private static void pruebasSistema() {
        SistemaLogistica sistema = new SistemaLogistica(redEjemplo());
        verificar("sin consultas no se prepara nada", sistema.getPreparaciones() == 0);

        for (int i = 0; i < 1000; i++) {
            sistema.consultarRuta("C", "P");
        }
        verificar("1.000 consultas hacen una sola preparación", sistema.getPreparaciones() == 1);

        sistema.deshabilitarConexion("N", "E");
        ResultadoConsultaRuta despues = sistema.consultarRuta("C", "S");
        verificar("cortar una conexión: la consulta ve la red nueva (27 min)", despues.getTiempoTotal() == 27);
        verificar("cortar una conexión: una preparación más", sistema.getPreparaciones() == 2);

        sistema.consultarRuta("C", "E");
        verificar("sin más cambios no se vuelve a preparar", sistema.getPreparaciones() == 2);

        sistema.habilitarConexion("N", "E");
        verificar("rehabilitar: se recuperan los 23 min", sistema.consultarRuta("C", "S").getTiempoTotal() == 23);

        sistema.getRed().deshabilitarConexion("S", "P");
        verificar("un cambio hecho directo sobre la red también se detecta",
                !sistema.consultarRuta("C", "P").isExiste());

        verificar("la consulta coincide con la ruta de Dijkstra del sistema",
                sistema.consultarRuta("C", "E").getTiempoTotal() == sistema.rutaMasRapida("C", "E").getTiempoTotal());
    }

    //  Contra Dijkstra

    private static void pruebasContraDijkstra() {
        Random random = new Random(42);
        int casos = 200;
        boolean mismoTiempo = true;
        boolean mismaConexion = true;
        boolean recorridoValido = true;
        for (int c = 0; c < casos; c++) {
            RedVial red = redAleatoria(random, 1 + random.nextInt(30), 3);
            TablaRutas tabla = FloydWarshall.calcular(red);
            int n = red.cantidadUbicaciones();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    ResultadoRuta dijkstra = Dijkstra.calcular(red, i, j, false);
                    ResultadoConsultaRuta floyd = tabla.consultar(i, j);
                    if (dijkstra.isExiste() != floyd.isExiste()) {
                        mismaConexion = false;
                    } else if (floyd.isExiste()) {
                        if (dijkstra.getTiempoTotal() != floyd.getTiempoTotal()) mismoTiempo = false;
                        if (sumaCamino(red, floyd.getCamino()) != floyd.getTiempoTotal()) recorridoValido = false;
                        if (floyd.getCamino()[0].getId() != i
                                || floyd.getCamino()[floyd.getCamino().length - 1].getId() != j) recorridoValido = false;
                    }
                }
            }
        }
        verificar(casos + " redes aleatorias: mismos pares conectados que Dijkstra", mismaConexion);
        verificar(casos + " redes aleatorias: mismos tiempos que Dijkstra", mismoTiempo);
        verificar(casos + " redes aleatorias: el recorrido es transitable y suma el tiempo", recorridoValido);
    }

    //  Volumen

    private static void pruebaVolumen() {
        int n = 500;
        int consultas = 100_000;
        Random random = new Random(42);
        RedVial red = redAleatoria(random, n, 4);

        long inicio = System.nanoTime();
        TablaRutas tabla = FloydWarshall.calcular(red);
        long msPreparacion = (System.nanoTime() - inicio) / 1_000_000;

        inicio = System.nanoTime();
        for (int i = 0; i < consultas; i++) {
            tabla.consultar(random.nextInt(n), random.nextInt(n));
        }
        long msConsultas = (System.nanoTime() - inicio) / 1_000_000;

        boolean coincide = true;
        for (int i = 0; i < 200; i++) {
            int origen = random.nextInt(n);
            int destino = random.nextInt(n);
            ResultadoRuta dijkstra = Dijkstra.calcular(red, origen, destino, false);
            ResultadoConsultaRuta floyd = tabla.consultar(origen, destino);
            if (dijkstra.isExiste() != floyd.isExiste()
                    || (floyd.isExiste() && dijkstra.getTiempoTotal() != floyd.getTiempoTotal())) {
                coincide = false;
            }
        }
        verificar(n + " ubicaciones: 200 pares al azar coinciden con Dijkstra", coincide);
        System.out.println("    n = " + n + ", E = " + red.cantidadConexiones() + ", preparación = " + msPreparacion
                + " ms, " + consultas + " consultas = " + msConsultas + " ms, mejoras = " + tabla.getMejoras());
    }

    // Auxiliares

    private static RedVial redAleatoria(Random random, int n, int salidasPorNodo) {
        RedVial red = new RedVial();
        for (int i = 0; i < n; i++) {
            red.agregarUbicacion("N" + i);
        }
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < salidasPorNodo; k++) {
                int destino = random.nextInt(n);
                if (destino == i || existeConexion(red, i, destino)) continue;
                red.agregarConexion(i, destino, 1 + random.nextInt(100));
                if (random.nextInt(5) == 0) {
                    red.deshabilitarConexion("N" + i, "N" + destino);
                }
            }
        }
        return red;
    }

    private static boolean existeConexion(RedVial red, int origen, int destino) {
        for (Conexion c : red.getConexionesSalientes(origen)) {
            if (c.getDestino() == destino) return true;
        }
        return false;
    }

    private static boolean ruta(TablaRutas tabla, String origen, String destino, int tiempo, String recorrido) {
        ResultadoConsultaRuta r = tabla.consultar(origen, destino);
        return r.isExiste() && r.getTiempoTotal() == tiempo && camino(r).equals(recorrido);
    }

    private static String camino(ResultadoConsultaRuta r) {
        StringBuilder sb = new StringBuilder();
        for (Ubicacion u : r.getCamino()) {
            if (sb.length() > 0) sb.append("-");
            sb.append(u.getNombre());
        }
        return sb.toString();
    }

    // Suma los tiempos del recorrido; devuelve -1 si algún tramo no existe o está deshabilitado
    private static long sumaCamino(RedVial red, Ubicacion[] camino) {
        long suma = 0;
        for (int i = 0; i + 1 < camino.length; i++) {
            Conexion encontrada = null;
            for (Conexion c : red.getConexionesSalientes(camino[i].getId())) {
                if (c.getDestino() == camino[i + 1].getId()) encontrada = c;
            }
            if (encontrada == null || !encontrada.isHabilitada()) return -1;
            suma += encontrada.getTiempo();
        }
        return suma;
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    private static void verificar(String nombre, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("  [OK]    " + nombre);
        } else {
            fallas++;
            System.out.println("  [FALLA] " + nombre);
        }
    }
}
