package logistica;

import java.util.Random;

import logistica.algoritmos.greedy.Dijkstra;
import logistica.algoritmos.greedy.MinHeap;
import logistica.modelo.Conexion;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.resultado.ResultadoRuta;

public class PruebasRutas {

    private static int ok = 0;
    private static int fallas = 0;

    static void main() {
        System.out.println("=== Pruebas: min-heap ===");
        pruebasHeap();
        System.out.println("=== Pruebas: ruta más rápida ===");
        pruebasRutas();
        System.out.println("=== Pruebas: deshabilitar conexiones ===");
        pruebasDeshabilitar();
        System.out.println("=== Pruebas: validaciones ===");
        pruebasValidaciones();
        System.out.println("=== Prueba de volumen ===");
        pruebaVolumen();

        System.out.println();
        System.out.println("Resultado: " + ok + "/" + (ok + fallas) + " OK");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    //  Min-heap

    private static void pruebasHeap() {
        MinHeap heap = new MinHeap(2); // capacidad chica a propósito: fuerza el crecimiento
        verificar("heap nuevo está vacío", heap.estaVacia());
        verificar("extraer de un heap vacío lanza excepción", lanza(() -> new MinHeap(4).extraerMin()));

        Random random = new Random(7);
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            heap.insertar(i, random.nextInt(500)); // muchas claves repetidas
        }
        verificar("tamaño tras insertar 10.000", heap.tamanio() == n);

        boolean ordenado = true;
        MinHeap.Entrada anterior = heap.extraerMin();
        for (int i = 1; i < n; i++) {
            MinHeap.Entrada actual = heap.extraerMin();
            boolean bien = anterior.clave < actual.clave
                    || (anterior.clave == actual.clave && anterior.nodo < actual.nodo);
            if (!bien) ordenado = false;
            anterior = actual;
        }
        verificar("extrae en orden (clave, nodo) creciente", ordenado);
        verificar("queda vacío al extraer todo", heap.estaVacia());
    }

    //  Rutas sobre la red de ejemplo

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

    private static void pruebasRutas() {
        RedVial red = redEjemplo();

        ResultadoRuta r = Dijkstra.calcular(red, "C", "S");
        verificar("ruta normal C -> S: tiempo 23", r.isExiste() && r.getTiempoTotal() == 23);
        verificar("ruta normal C -> S: recorrido C-N-E-S", camino(r).equals("C-N-E-S"));

        ResultadoRuta mismo = Dijkstra.calcular(red, "C", "C");
        verificar("origen = destino: tiempo 0", mismo.isExiste() && mismo.getTiempoTotal() == 0);
        verificar("origen = destino: recorrido de un solo nodo", camino(mismo).equals("C"));

        ResultadoRuta ida = Dijkstra.calcular(red, "C", "N");
        ResultadoRuta vuelta = Dijkstra.calcular(red, "N", "C");
        verificar("sentido: C -> N tarda 10", ida.getTiempoTotal() == 10);
        verificar("sentido: N -> C debe dar la vuelta (33 min por N-E-S-C)",
                vuelta.getTiempoTotal() == 33 && camino(vuelta).equals("N-E-S-C"));

        ResultadoRuta sinSalida = Dijkstra.calcular(red, "P", "C");
        verificar("P no tiene conexiones salientes: no hay ruta a C", !sinSalida.isExiste());

        ResultadoRuta largo = Dijkstra.calcular(red, "C", "P");
        verificar("ruta C -> P: tiempo 30 por C-N-E-S-P", largo.getTiempoTotal() == 30
                && camino(largo).equals("C-N-E-S-P"));

        verificar("el cálculo es repetible (misma red, mismo resultado)",
                Dijkstra.calcular(red, "C", "S").getTiempoTotal() == 23);
        verificar("la ruta con traza da lo mismo que sin traza",
                camino(Dijkstra.calcular(red, "C", "S", true)).equals("C-N-E-S"));
    }

    private static void pruebasDeshabilitar() {
        RedVial red = redEjemplo();

        verificar("deshabilitar una conexión existente devuelve true", red.deshabilitarConexion("N", "E"));
        ResultadoRuta desvio = Dijkstra.calcular(red, "C", "S");
        verificar("tras deshabilitar N -> E: desvío por O, 27 min",
                desvio.getTiempoTotal() == 27 && camino(desvio).equals("C-O-S"));

        verificar("deshabilitar una conexión inexistente devuelve false", !red.deshabilitarConexion("C", "P"));

        red.habilitarConexion("N", "E");
        verificar("al volver a habilitar N -> E se recupera la ruta de 23 min",
                Dijkstra.calcular(red, "C", "S").getTiempoTotal() == 23);

        red.deshabilitarConexion("S", "P");
        ResultadoRuta sinRuta = Dijkstra.calcular(red, "C", "P");
        verificar("cortada la única entrada a P: no existe ruta", !sinRuta.isExiste());
        verificar("sin ruta: no hay recorrido", sinRuta.getCamino() == null);
        verificar("sin ruta: el mensaje lo informa",
                sinRuta.toString().startsWith("No existe una ruta disponible"));

        // Se cortan todas las entradas a S: queda aislado.
        RedVial red2 = redEjemplo();
        red2.deshabilitarConexion("N", "S");
        red2.deshabilitarConexion("O", "S");
        red2.deshabilitarConexion("E", "S");
        verificar("S aislado: no existe ruta C -> S", !Dijkstra.calcular(red2, "C", "S").isExiste());
    }

    private static void pruebasValidaciones() {
        RedVial red = redEjemplo();
        verificar("ubicación origen inexistente lanza excepción", lanza(() -> Dijkstra.calcular(red, "X", "C")));
        verificar("ubicación destino inexistente lanza excepción", lanza(() -> Dijkstra.calcular(red, "C", "X")));
        verificar("conexión con tiempo 0 lanza excepción", lanza(() -> red.agregarConexion("C", "P", 0)));
        verificar("conexión con tiempo negativo lanza excepción", lanza(() -> red.agregarConexion("C", "P", -5)));
        verificar("conexión duplicada lanza excepción", lanza(() -> red.agregarConexion("C", "N", 3)));
        verificar("ubicación duplicada lanza excepción", lanza(() -> red.agregarUbicacion("C")));
    }

    // Volumen

    private static void pruebaVolumen() {
        int n = 100_000;
        Random random = new Random(42);
        RedVial red = new RedVial();
        for (int i = 0; i < n; i++) red.agregarUbicacion("N" + i);
        for (int i = 0; i < n; i++) {
            if (i + 1 < n) red.agregarConexion(i, i + 1, 1 + random.nextInt(20)); // asegura conectividad
            for (int k = 0; k < 3; k++) {
                int destino = random.nextInt(n);
                if (destino == i) continue;
                try {
                    red.agregarConexion(i, destino, 1 + random.nextInt(200));
                } catch (IllegalArgumentException duplicada) {
                    // conexión repetida: se ignora
                }
            }
        }

        long inicio = System.nanoTime();
        ResultadoRuta r = Dijkstra.calcular(red, 0, n - 1, false);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        verificar("100.000 ubicaciones: existe ruta de 0 a n-1", r.isExiste());
        verificar("100.000 ubicaciones: el recorrido es transitable y suma el tiempo informado",
                r.isExiste() && sumaCamino(red, r.getCamino()) == r.getTiempoTotal());
        System.out.println("    V = " + n + ", E = " + red.cantidadConexiones() + ", tiempo = " + ms + " ms, "
                + "nodos fijados = " + r.getNodosFijados() + ", relajaciones = " + r.getRelajaciones());
    }

    // Auxiliares

    private static String camino(ResultadoRuta r) {
        if (!r.isExiste()) return "(sin ruta)";
        StringBuilder sb = new StringBuilder();
        Ubicacion[] c = r.getCamino();
        for (int i = 0; i < c.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(c[i].getNombre());
        }
        return sb.toString();
    }

    private static long sumaCamino(RedVial red, Ubicacion[] camino) {   // Suma los tiempos del recorrido; devuelve -1 si algún tramo no existe o está deshabilitado
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