package logistica;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logistica.algoritmos.backtracking.RutasAlternativas;
import logistica.algoritmos.greedy.Dijkstra;
import logistica.modelo.Conexion;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.resultado.ResultadoRuta;
import logistica.resultado.ResultadoRutasAlternativas;
import logistica.resultado.ResultadoRutasAlternativas.Ruta;
import logistica.sistema.SistemaLogistica;

/**
 * Casos de prueba de la funcionalidad B3 (rutas alternativas con restricciones, Backtracking).
 */
public class PruebasRutasAlternativas {

    private static int ok = 0;
    private static int fallas = 0;
    private static final String[] NINGUNA = {};

    public static void main(String[] args) {
        System.out.println("=== Pruebas: rutas, tiempos y mejor ruta ===");
        pruebasBasicas();
        System.out.println("=== Pruebas: tiempo máximo ===");
        pruebasTiempoMaximo();
        System.out.println("=== Pruebas: ubicaciones prohibidas ===");
        pruebasProhibidas();
        System.out.println("=== Pruebas: sin repetir ubicaciones (ciclos) ===");
        pruebasCiclos();
        System.out.println("=== Pruebas: contadores de la búsqueda ===");
        pruebasContadores();
        System.out.println("=== Pruebas: casos borde y validaciones ===");
        pruebasBorde();
        System.out.println("=== Pruebas: conexiones deshabilitadas e integración ===");
        pruebasSistema();
        System.out.println("=== Pruebas: contra una enumeración independiente (BFS) y contra Dijkstra ===");
        pruebasContraReferencias();
        System.out.println("=== Prueba de volumen ===");
        pruebaVolumen();

        System.out.println();
        System.out.println("Resultado: " + ok + "/" + (ok + fallas) + " OK");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    /** A -> B 2, A -> C 5, B -> C 1, B -> D 7, C -> D 2. De A a D: A-B-D 9, A-B-C-D 5, A-C-D 7. */
    private static RedVial redBase() {
        RedVial red = new RedVial();
        red.agregarUbicacion("A");
        red.agregarUbicacion("B");
        red.agregarUbicacion("C");
        red.agregarUbicacion("D");
        red.agregarConexion("A", "B", 2);
        red.agregarConexion("A", "C", 5);
        red.agregarConexion("B", "C", 1);
        red.agregarConexion("B", "D", 7);
        red.agregarConexion("C", "D", 2);
        return red;
    }

    /** La red base más C -> B 1 y D -> A 1: aparecen ciclos posibles. */
    private static RedVial redConCiclos() {
        RedVial red = redBase();
        red.agregarConexion("C", "B", 1);
        red.agregarConexion("D", "A", 1);
        return red;
    }

    //  Rutas, tiempos y mejor ruta

    private static void pruebasBasicas() {
        ResultadoRutasAlternativas r = RutasAlternativas.buscar(redBase(), "A", "D", 100, NINGUNA);

        verificar("encuentra las 3 rutas posibles de A a D", r.getCantidadRutas() == 3);
        verificar("A-B-D dura 9 min", contiene(r, "A-B-D", 9));
        verificar("A-B-C-D dura 5 min", contiene(r, "A-B-C-D", 5));
        verificar("A-C-D dura 7 min", contiene(r, "A-C-D", 7));
        verificar("la de menor tiempo es A-B-C-D con 5 min",
                r.getMejor() != null && r.getMejor().getTiempoTotal() == 5 && texto(r.getMejor()).equals("A-B-C-D"));
        verificar("toda ruta empieza en el origen y termina en el destino", todasDeAaD(r));
        verificar("el tiempo informado es la suma de los tramos", tiemposConsistentes(redBase(), r));
    }

    //  Tiempo máximo

    private static void pruebasTiempoMaximo() {
        RedVial red = redBase();
        verificar("máximo 8: quedan A-B-C-D (5) y A-C-D (7)",
                cantidad(red, "A", "D", 8, NINGUNA) == 2);
        verificar("máximo 6: sólo queda A-B-C-D (5)",
                cantidad(red, "A", "D", 6, NINGUNA) == 1);
        verificar("máximo 5: el tiempo máximo se puede alcanzar exactamente (A-B-C-D = 5)",
                cantidad(red, "A", "D", 5, NINGUNA) == 1);
        verificar("máximo 4: ninguna ruta alcanza", cantidad(red, "A", "D", 4, NINGUNA) == 0);
        verificar("máximo 9: entra A-B-D justo en 9", cantidad(red, "A", "D", 9, NINGUNA) == 3);
        ResultadoRutasAlternativas sin = RutasAlternativas.buscar(red, "A", "D", 4, NINGUNA);
        verificar("sin rutas: no existe y no hay mejor", !sin.isExiste() && sin.getMejor() == null);
    }

    //  Ubicaciones prohibidas

    private static void pruebasProhibidas() {
        RedVial red = redBase();
        ResultadoRutasAlternativas sinC = RutasAlternativas.buscar(red, "A", "D", 100, new String[]{"C"});
        verificar("prohibida C: sólo queda A-B-D (9)", sinC.getCantidadRutas() == 1 && contiene(sinC, "A-B-D", 9));
        ResultadoRutasAlternativas sinB = RutasAlternativas.buscar(red, "A", "D", 100, new String[]{"B"});
        verificar("prohibida B: sólo queda A-C-D (7)", sinB.getCantidadRutas() == 1 && contiene(sinB, "A-C-D", 7));
        verificar("prohibidas B y C: no hay rutas", cantidad(red, "A", "D", 100, new String[]{"B", "C"}) == 0);
        verificar("prohibida C y máximo 8: tampoco hay rutas (A-B-D dura 9)",
                cantidad(red, "A", "D", 8, new String[]{"C"}) == 0);
        verificar("prohibida D en la red con ciclos: A -> C pierde sólo las rutas que pasaban por D",
                cantidad(redConCiclos(), "A", "C", 100, new String[]{"D"})
                        == cantidad(redConCiclos(), "A", "C", 100, NINGUNA) - cantidadConD(redConCiclos()));
        verificar("origen prohibido: no hay rutas", cantidad(red, "A", "D", 100, new String[]{"A"}) == 0);
        verificar("destino prohibido: no hay rutas", cantidad(red, "A", "D", 100, new String[]{"D"}) == 0);
        verificar("ninguna ruta pasa por una ubicación prohibida", ningunaPasaPor(redConCiclos(), "B"));
    }

    /** Rutas A -> C que pasan por D en la red con ciclos (para comparar contra la prohibición de D). */
    private static int cantidadConD(RedVial red) {
        ResultadoRutasAlternativas r = RutasAlternativas.buscar(red, "A", "C", 100, NINGUNA);
        int n = 0;
        for (Ruta ruta : r.getRutas()) {
            if (texto(ruta).contains("D")) n++;
        }
        return n;
    }

    private static boolean ningunaPasaPor(RedVial red, String prohibida) {
        ResultadoRutasAlternativas r = RutasAlternativas.buscar(red, "A", "D", 100, new String[]{prohibida});
        for (Ruta ruta : r.getRutas()) {
            if (texto(ruta).contains(prohibida)) return false;
        }
        return r.getCantidadRutas() > 0;
    }

    //  Sin repetir ubicaciones

    private static void pruebasCiclos() {
        RedVial red = redConCiclos();
        ResultadoRutasAlternativas r = RutasAlternativas.buscar(red, "A", "D", 100, NINGUNA);
        verificar("con ciclos posibles hay 4 rutas: A-B-D, A-B-C-D, A-C-D y A-C-B-D", r.getCantidadRutas() == 4);
        verificar("A-C-B-D dura 5 + 1 + 7 = 13 min", contiene(r, "A-C-B-D", 13));
        verificar("ninguna ruta repite una ubicación", sinRepetidas(r));
        verificar("no hay rutas duplicadas", sinDuplicadas(r));
        verificar("D -> A no se usa para volver al origen", cantidad(red, "A", "D", 100, NINGUNA) == 4);
        ResultadoRutasAlternativas b = RutasAlternativas.buscar(red, "B", "B", 100, NINGUNA);
        verificar("origen igual al destino: una ruta de una sola ubicación y 0 min",
                b.getCantidadRutas() == 1 && b.getMejor().getTiempoTotal() == 0 && b.getMejor().getCamino().length == 1);
    }

    //  Contadores de la búsqueda

    private static void pruebasContadores() {
        ResultadoRutasAlternativas sinLimite = RutasAlternativas.buscar(redBase(), "A", "D", 100, NINGUNA);
        verificar("sin límite se exploran 7 nodos (A, B, C, D, D, C, D)", sinLimite.getNodosExplorados() == 7);
        verificar("sin límite no se poda por tiempo", sinLimite.getPodasPorTiempo() == 0);

        ResultadoRutasAlternativas limite6 = RutasAlternativas.buscar(redBase(), "A", "D", 6, NINGUNA);
        verificar("con máximo 6 se podan 2 ramas por tiempo (B -> D y C -> D desde A-C)",
                limite6.getPodasPorTiempo() == 2);
        verificar("con máximo 6 se exploran 5 nodos: la poda evita recorrer ramas inútiles",
                limite6.getNodosExplorados() == 5);
        verificar("podar nunca explora más que no podar", limite6.getNodosExplorados() <= sinLimite.getNodosExplorados());

        ResultadoRutasAlternativas prohib = RutasAlternativas.buscar(redBase(), "A", "D", 100, new String[]{"C"});
        verificar("con C prohibida se podan 2 ramas por ubicación prohibida (A -> C y B -> C)",
                prohib.getPodasPorProhibida() == 2);

        ResultadoRutasAlternativas ciclos = RutasAlternativas.buscar(redConCiclos(), "A", "D", 100, NINGUNA);
        verificar("en la red con ciclos se podan ramas por ubicación repetida", ciclos.getPodasPorRepetida() > 0);
    }

    //  Casos borde y validaciones

    private static void pruebasBorde() {
        RedVial red = redBase();
        verificar("destino sin camino desde el origen (A no se alcanza desde D): sin rutas",
                cantidad(red, "D", "A", 100, NINGUNA) == 0);
        verificar("tiempo máximo 0 y origen distinto del destino: sin rutas", cantidad(red, "A", "D", 0, NINGUNA) == 0);
        verificar("tiempo máximo 0 y origen igual al destino: una ruta de 0 min", cantidad(red, "A", "A", 0, NINGUNA) == 1);
        verificar("ruta de una sola conexión: A -> B", cantidad(red, "A", "B", 2, NINGUNA) == 1);

        verificar("origen inexistente lanza IllegalArgumentException",
                lanza(() -> RutasAlternativas.buscar(redBase(), "Z", "D", 10, NINGUNA)));
        verificar("destino inexistente lanza IllegalArgumentException",
                lanza(() -> RutasAlternativas.buscar(redBase(), "A", "Z", 10, NINGUNA)));
        verificar("ubicación prohibida inexistente lanza IllegalArgumentException",
                lanza(() -> RutasAlternativas.buscar(redBase(), "A", "D", 10, new String[]{"Z"})));
        verificar("tiempo máximo negativo lanza IllegalArgumentException",
                lanza(() -> RutasAlternativas.buscar(redBase(), "A", "D", -1, NINGUNA)));
        verificar("conjunto de prohibidas null lanza IllegalArgumentException",
                lanza(() -> RutasAlternativas.buscar(redBase(), "A", "D", 10, null)));
        verificar("red null lanza IllegalArgumentException",
                lanza(() -> RutasAlternativas.buscar(null, "A", "D", 10, NINGUNA)));
        verificar("una ubicación prohibida repetida no es un error",
                cantidad(red, "A", "D", 100, new String[]{"C", "C"}) == 1);
    }

    //  Conexiones deshabilitadas e integración

    private static void pruebasSistema() {
        RedVial red = redBase();
        SistemaLogistica sistema = new SistemaLogistica(red);
        verificar("antes de cortar: 3 rutas", sistema.rutasAlternativas("A", "D", 100, NINGUNA).getCantidadRutas() == 3);

        sistema.deshabilitarConexion("B", "C");
        ResultadoRutasAlternativas r = sistema.rutasAlternativas("A", "D", 100, NINGUNA);
        verificar("con B -> C deshabilitada quedan A-B-D (9) y A-C-D (7)",
                r.getCantidadRutas() == 2 && contiene(r, "A-B-D", 9) && contiene(r, "A-C-D", 7));
        verificar("la de menor tiempo pasa a ser A-C-D con 7 min", r.getMejor().getTiempoTotal() == 7);

        sistema.habilitarConexion("B", "C");
        verificar("al habilitarla de nuevo vuelven las 3 rutas",
                sistema.rutasAlternativas("A", "D", 100, NINGUNA).getCantidadRutas() == 3);

        int version = red.getVersion();
        sistema.rutasAlternativas("A", "D", 100, new String[]{"C"});
        verificar("la búsqueda sólo consulta: no modifica la red", red.getVersion() == version);
    }

    //  Contra referencias independientes

    private static void pruebasContraReferencias() {
        Random azar = new Random(2026);
        int redes = 60;
        int coinciden = 0;
        int minimoCoincide = 0;
        for (int caso = 0; caso < redes; caso++) {
            int n = 5 + azar.nextInt(3); // 5 a 7 ubicaciones
            RedVial red = new RedVial();
            for (int i = 0; i < n; i++) red.agregarUbicacion("U" + i);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j && azar.nextInt(100) < 40) {
                        red.agregarConexion(i, j, 1 + azar.nextInt(15));
                    }
                }
            }
            int origen = azar.nextInt(n);
            int destino = azar.nextInt(n);
            int maximo = 5 + azar.nextInt(40);
            int prohibida = azar.nextInt(n);
            String[] prohibidas = (prohibida != origen && prohibida != destino && azar.nextBoolean())
                    ? new String[]{"U" + prohibida} : NINGUNA;

            ResultadoRutasAlternativas r = RutasAlternativas.buscar(red, "U" + origen, "U" + destino, maximo, prohibidas);
            List<String> referencia = enumerarPorBFS(red, origen, destino, maximo, prohibidas);
            if (coincideConReferencia(r, referencia)) coinciden++;

            // Sin restricciones: la menor de todas las rutas es la que calcula Dijkstra (F2).
            ResultadoRutasAlternativas libre = RutasAlternativas.buscar(red, "U" + origen, "U" + destino, 1000, NINGUNA);
            ResultadoRuta dijkstra = Dijkstra.calcular(red, "U" + origen, "U" + destino);
            boolean igual = dijkstra.isExiste()
                    ? (libre.isExiste() && libre.getMejor().getTiempoTotal() == dijkstra.getTiempoTotal())
                    : !libre.isExiste();
            if (igual) minimoCoincide++;
        }
        verificar(redes + " redes al azar: mismas rutas y tiempos que una enumeración independiente por BFS",
                coinciden == redes);
        verificar(redes + " redes al azar: la ruta más rápida sin restricciones coincide con Dijkstra",
                minimoCoincide == redes);
    }

    /** Referencia: enumera las rutas con una cola de rutas parciales (BFS), sin recursión ni backtracking. */
    private static List<String> enumerarPorBFS(RedVial red, int origen, int destino, int maximo, String[] prohibidas) {
        List<String> resultado = new ArrayList<>();
        for (String p : prohibidas) {
            if (red.getId(p) == origen || red.getId(p) == destino) return resultado;
        }
        List<int[]> cola = new ArrayList<>();
        cola.add(new int[]{origen});
        int cabeza = 0;
        while (cabeza < cola.size()) {
            int[] camino = cola.get(cabeza++);
            int ultimo = camino[camino.length - 1];
            if (ultimo == destino) {
                resultado.add(textoDe(red, camino) + ":" + tiempoDe(red, camino));
                continue;
            }
            for (Conexion c : red.getConexionesSalientes(ultimo)) {
                int v = c.getDestino();
                if (!c.isHabilitada() || contieneId(camino, v) || esProhibida(red, v, prohibidas)) continue;
                int[] nuevo = new int[camino.length + 1];
                System.arraycopy(camino, 0, nuevo, 0, camino.length);
                nuevo[camino.length] = v;
                if (tiempoDe(red, nuevo) <= maximo) cola.add(nuevo);
            }
        }
        return resultado;
    }

    private static boolean coincideConReferencia(ResultadoRutasAlternativas r, List<String> referencia) {
        if (r.getCantidadRutas() != referencia.size()) return false;
        for (Ruta ruta : r.getRutas()) {
            if (!referencia.contains(texto(ruta) + ":" + ruta.getTiempoTotal())) return false;
        }
        return sinDuplicadas(r);
    }

    //  Volumen

    private static void pruebaVolumen() {
        // Grafo completo de 9 ubicaciones: de la 0 a la 8 hay 13.700 rutas sin repetir ubicaciones
        // (la suma de las variaciones de 7 intermedias tomadas de a k, con k de 0 a 7).
        int n = 9;
        RedVial red = new RedVial();
        for (int i = 0; i < n; i++) red.agregarUbicacion("U" + i);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) red.agregarConexion(i, j, 1 + (i * 7 + j * 3) % 9);
            }
        }
        long inicio = System.nanoTime();
        ResultadoRutasAlternativas r = RutasAlternativas.buscar(red, "U0", "U8", 1000, NINGUNA);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        verificar("grafo completo de 9 ubicaciones: 13.700 rutas (" + ms + " ms)", r.getCantidadRutas() == 13700);
        verificar("la poda por tiempo reduce la búsqueda: máximo 12 min explora menos nodos",
                RutasAlternativas.buscar(red, "U0", "U8", 12, NINGUNA).getNodosExplorados() < r.getNodosExplorados());
    }

    //  Auxiliares

    private static int cantidad(RedVial red, String origen, String destino, int maximo, String[] prohibidas) {
        return RutasAlternativas.buscar(red, origen, destino, maximo, prohibidas).getCantidadRutas();
    }

    private static boolean contiene(ResultadoRutasAlternativas r, String camino, int tiempo) {
        for (Ruta ruta : r.getRutas()) {
            if (texto(ruta).equals(camino) && ruta.getTiempoTotal() == tiempo) return true;
        }
        return false;
    }

    /** Nombres de las ubicaciones de la ruta, unidos con "-" (en los tests los nombres son de una letra o U + número). */
    private static String texto(Ruta ruta) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ruta.getCamino().length; i++) {
            if (i > 0) sb.append("-");
            sb.append(ruta.getCamino()[i]);
        }
        return sb.toString();
    }

    private static String textoDe(RedVial red, int[] camino) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camino.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(red.getUbicacion(camino[i]));
        }
        return sb.toString();
    }

    private static int tiempoDe(RedVial red, int[] camino) {
        int total = 0;
        for (int i = 0; i + 1 < camino.length; i++) {
            for (Conexion c : red.getConexionesSalientes(camino[i])) {
                if (c.getDestino() == camino[i + 1]) total += c.getTiempo();
            }
        }
        return total;
    }

    private static boolean contieneId(int[] camino, int id) {
        for (int x : camino) {
            if (x == id) return true;
        }
        return false;
    }

    private static boolean esProhibida(RedVial red, int id, String[] prohibidas) {
        for (String p : prohibidas) {
            if (red.getId(p) == id) return true;
        }
        return false;
    }

    private static boolean todasDeAaD(ResultadoRutasAlternativas r) {
        for (Ruta ruta : r.getRutas()) {
            Ubicacion[] c = ruta.getCamino();
            if (!c[0].getNombre().equals("A") || !c[c.length - 1].getNombre().equals("D")) return false;
        }
        return true;
    }

    private static boolean tiemposConsistentes(RedVial red, ResultadoRutasAlternativas r) {
        for (Ruta ruta : r.getRutas()) {
            int[] ids = new int[ruta.getCamino().length];
            for (int i = 0; i < ids.length; i++) ids[i] = ruta.getCamino()[i].getId();
            if (tiempoDe(red, ids) != ruta.getTiempoTotal()) return false;
        }
        return true;
    }

    private static boolean sinRepetidas(ResultadoRutasAlternativas r) {
        for (Ruta ruta : r.getRutas()) {
            Ubicacion[] c = ruta.getCamino();
            for (int i = 0; i < c.length; i++) {
                for (int j = i + 1; j < c.length; j++) {
                    if (c[i].getId() == c[j].getId()) return false;
                }
            }
        }
        return true;
    }

    private static boolean sinDuplicadas(ResultadoRutasAlternativas r) {
        Ruta[] rutas = r.getRutas();
        for (int i = 0; i < rutas.length; i++) {
            for (int j = i + 1; j < rutas.length; j++) {
                if (texto(rutas[i]).equals(texto(rutas[j]))) return false;
            }
        }
        return true;
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalArgumentException e) {
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
