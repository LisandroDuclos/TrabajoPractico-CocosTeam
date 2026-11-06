package logistica;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Random;

import logistica.algoritmos.backtracking.RecorridoEntregas;
import logistica.algoritmos.dinamica.FloydWarshall;
import logistica.algoritmos.greedy.Dijkstra;
import logistica.modelo.Lote;
import logistica.modelo.Pedido;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.modelo.Vehiculo;
import logistica.resultado.ResultadoConsultaRuta;
import logistica.resultado.ResultadoRecorrido;
import logistica.resultado.ResultadoReparto;
import logistica.resultado.ResultadoRuta;
import logistica.resultado.TablaRutas;
import logistica.sistema.SistemaLogistica;

/**
 * Casos de prueba de la funcionalidad B1 (recorrido con múltiples entregas, Backtracking).
 */
public class PruebasRecorridoEntregas {

    private static int ok = 0;
    private static int fallas = 0;
    private static final int SIN_CAMINO = Integer.MAX_VALUE;

    public static void main(String[] args) {
        System.out.println("=== Pruebas: orden de visita y tiempo total ===");
        pruebasBasicas();
        System.out.println("=== Pruebas: tramos que atraviesan otras ubicaciones ===");
        pruebasTramos();
        System.out.println("=== Pruebas: empates ===");
        pruebasEmpates();
        System.out.println("=== Pruebas: sin recorrido posible ===");
        pruebasSinRecorrido();
        System.out.println("=== Pruebas: podas y contadores ===");
        pruebasPodas();
        System.out.println("=== Pruebas: validaciones ===");
        pruebasValidaciones();
        System.out.println("=== Pruebas: B1 desde el sistema ===");
        pruebasSistema();
        System.out.println("=== Pruebas: reparto integrado F1 -> F3 -> B1 ===");
        pruebasReparto();
        System.out.println("=== Pruebas: repartos que no se despachan ===");
        pruebasRepartoNoDespachado();
        System.out.println("=== Pruebas: contra fuerza bruta con tiempos de Dijkstra ===");
        pruebasContraFuerzaBruta();
        System.out.println("=== Prueba de volumen ===");
        pruebaVolumen();

        System.out.println();
        System.out.println("Resultado: " + ok + "/" + (ok + fallas) + " OK");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    /**
     * Anillo de un solo sentido: D -> A 3, A -> X 2, X -> B 2, B -> C 2, C -> D 6.
     * X no es un centro: sólo se atraviesa. El único orden razonable es D-A-B-C-D (15 min).
     */
    private static RedVial redAnillo() {
        RedVial red = new RedVial();
        red.agregarUbicacion("D");
        red.agregarUbicacion("A");
        red.agregarUbicacion("X");
        red.agregarUbicacion("B");
        red.agregarUbicacion("C");
        red.agregarConexion("D", "A", 3);
        red.agregarConexion("A", "X", 2);
        red.agregarConexion("X", "B", 2);
        red.agregarConexion("B", "C", 2);
        red.agregarConexion("C", "D", 6);
        return red;
    }

    /**
     * Conexiones en ambos sentidos con el mismo tiempo: D-A 2, D-B 5, D-C 9, A-B 3, A-C 4, B-C 1.
     * Tiempos mínimos: D-C 6 (por A y B). Hay cuatro órdenes empatados en 12 min.
     */
    private static RedVial redSimetrica() {
        RedVial red = new RedVial();
        red.agregarUbicacion("D");
        red.agregarUbicacion("A");
        red.agregarUbicacion("B");
        red.agregarUbicacion("C");
        dobleMano(red, "D", "A", 2);
        dobleMano(red, "D", "B", 5);
        dobleMano(red, "D", "C", 9);
        dobleMano(red, "A", "B", 3);
        dobleMano(red, "A", "C", 4);
        dobleMano(red, "B", "C", 1);
        return red;
    }

    //  Orden de visita y tiempo total

    private static void pruebasBasicas() {
        RedVial red = redAnillo();
        ResultadoRecorrido r = RecorridoEntregas.calcular(red, "D", new String[]{"C", "A", "B"});

        verificar("existe un recorrido", r.isExiste());
        verificar("el orden óptimo es D-A-B-C-D", texto(r.getParadas()).equals("D-A-B-C-D"));
        verificar("tarda 15 min", r.getTiempoTotal() == 15);
        verificar("empieza y termina en el depósito", empiezaYTerminaEnDeposito(r));
        verificar("cada centro aparece exactamente una vez como parada", cadaCentroUnaVez(r));
        verificar("hay un tramo por cada par de paradas seguidas", r.getTramos().length == 4);
        verificar("el tiempo total es la suma de los tramos", tiempoConsistente(r));
        verificar("el tiempo de cada tramo es el mínimo (igual a Dijkstra)", tramosMinimos(red, r));
        verificar("el camino completo es D-A-X-B-C-D", texto(r.getCamino()).equals("D-A-X-B-C-D"));
        verificar("los centros se informan en el orden en que se pidieron",
                texto(r.getCentros()).equals("C-A-B"));

        ResultadoRecorrido uno = RecorridoEntregas.calcular(red, "D", new String[]{"C"});
        verificar("un solo centro: ida y vuelta D-C-D en 15 min (9 + 6)",
                texto(uno.getParadas()).equals("D-C-D") && uno.getTiempoTotal() == 15);
        verificar("un solo centro: 2 tramos", uno.getTramos().length == 2);
    }

    //  Tramos que atraviesan otras ubicaciones

    private static void pruebasTramos() {
        RedVial red = redAnillo();
        ResultadoRecorrido r = RecorridoEntregas.calcular(red, "D", new String[]{"A", "C"});
        verificar("centros A y C: orden D-A-C-D en 15 min",
                texto(r.getParadas()).equals("D-A-C-D") && r.getTiempoTotal() == 15);
        verificar("el tramo A -> C atraviesa X y B, que no son paradas",
                texto(r.getTramos()[1].getCamino()).equals("A-X-B-C"));

        // Estrella: D-A 1 y A-B 1 en ambos sentidos, D-B 10. Ir a B conviene pasando por A.
        RedVial estrella = new RedVial();
        estrella.agregarUbicacion("D");
        estrella.agregarUbicacion("A");
        estrella.agregarUbicacion("B");
        dobleMano(estrella, "D", "A", 1);
        dobleMano(estrella, "A", "B", 1);
        dobleMano(estrella, "D", "B", 10);
        ResultadoRecorrido e = RecorridoEntregas.calcular(estrella, "D", new String[]{"B", "A"});
        verificar("estrella: 4 min, los dos órdenes empatan y queda el primero (D-B-A-D)",
                e.getTiempoTotal() == 4 && texto(e.getParadas()).equals("D-B-A-D"));
        verificar("el tramo D -> B atraviesa A, otro centro seleccionado",
                texto(e.getTramos()[0].getCamino()).equals("D-A-B"));
        verificar("aun así A aparece una sola vez como parada", cadaCentroUnaVez(e));
        verificar("el camino completo pasa dos veces por A (D-A-B-A-D)",
                texto(e.getCamino()).equals("D-A-B-A-D"));
    }

    //  Empates

    private static void pruebasEmpates() {
        RedVial red = redSimetrica();
        ResultadoRecorrido abc = RecorridoEntregas.calcular(red, "D", new String[]{"A", "B", "C"});
        verificar("red simétrica: el mejor recorrido dura 12 min", abc.getTiempoTotal() == 12);
        verificar("ante un empate se conserva el primero encontrado (D-A-B-C-D)",
                texto(abc.getParadas()).equals("D-A-B-C-D"));
        ResultadoRecorrido cba = RecorridoEntregas.calcular(red, "D", new String[]{"C", "B", "A"});
        verificar("con los centros en otro orden, el primero encontrado es D-C-B-A-D (12 min)",
                texto(cba.getParadas()).equals("D-C-B-A-D") && cba.getTiempoTotal() == 12);
        ResultadoRecorrido otra = RecorridoEntregas.calcular(red, "D", new String[]{"A", "B", "C"});
        verificar("misma entrada, mismo resultado", texto(otra.getParadas()).equals(texto(abc.getParadas()))
                && otra.getNodosExplorados() == abc.getNodosExplorados());
    }

    //  Sin recorrido posible

    private static void pruebasSinRecorrido() {
        RedVial red = redAnillo();
        red.agregarUbicacion("Z"); // sin conexiones
        ResultadoRecorrido aislado = RecorridoEntregas.calcular(red, "D", new String[]{"A", "Z"});
        verificar("un centro aislado: no existe recorrido", !aislado.isExiste());
        verificar("sin recorrido: paradas, tramos y camino son null",
                aislado.getParadas() == null && aislado.getTramos() == null && aislado.getCamino() == null);
        verificar("sin recorrido: se cuentan las podas sin conexión", aislado.getPodasSinConexion() > 0);

        RedVial sumidero = redAnillo();
        sumidero.agregarUbicacion("S");
        sumidero.agregarConexion("C", "S", 1); // se llega a S pero no se sale
        verificar("un centro al que se llega pero del que no se vuelve: no existe recorrido",
                !RecorridoEntregas.calcular(sumidero, "D", new String[]{"S"}).isExiste());

        RedVial cortada = redAnillo();
        cortada.deshabilitarConexion("C", "D");
        verificar("se corta la única vuelta al depósito: no existe recorrido",
                !RecorridoEntregas.calcular(cortada, "D", new String[]{"A"}).isExiste());
        cortada.habilitarConexion("C", "D");
        verificar("al habilitarla otra vez vuelve a existir",
                RecorridoEntregas.calcular(cortada, "D", new String[]{"A"}).isExiste());
    }

    //  Podas y contadores

    private static void pruebasPodas() {
        ResultadoRecorrido r = RecorridoEntregas.calcular(redSimetrica(), "D", new String[]{"A", "B", "C"});
        verificar("3 centros: como mucho 3! = 6 recorridos completos", r.getRecorridosCompletos() <= 6);
        verificar("3 centros: como mucho 1 + 3 + 6 + 6 = 16 nodos", r.getNodosExplorados() <= 16);
        verificar("la cota descarta ramas que no pueden mejorar", r.getPodasPorCota() > 0);

        RedVial completa = grafoCompleto(9, new Random(7));
        ResultadoRecorrido grande = RecorridoEntregas.calcular(completa, "U0", centrosU(1, 8));
        verificar("8 centros: completa muchos menos que los 8! = 40320 recorridos ("
                + grande.getRecorridosCompletos() + ")", grande.getRecorridosCompletos() < 40320);
        verificar("8 centros: hay podas por cota (" + grande.getPodasPorCota() + ")", grande.getPodasPorCota() > 0);

        ResultadoRecorrido sinTraza = RecorridoEntregas.calcular(completa, "U0", centrosU(1, 5));
        ResultadoRecorrido conTraza = calcularEnSilencio(completa, "U0", centrosU(1, 5));
        verificar("con y sin traza dan el mismo recorrido y los mismos contadores",
                texto(conTraza.getParadas()).equals(texto(sinTraza.getParadas()))
                        && conTraza.getTiempoTotal() == sinTraza.getTiempoTotal()
                        && conTraza.getNodosExplorados() == sinTraza.getNodosExplorados()
                        && conTraza.getPodasPorCota() == sinTraza.getPodasPorCota());
    }

    //  Validaciones

    private static void pruebasValidaciones() {
        RedVial red = grafoCompleto(10, new Random(1));
        verificar("red null lanza excepción",
                lanza(() -> RecorridoEntregas.calcular(null, "U0", new String[]{"U1"})));
        verificar("centros null lanza excepción", lanza(() -> RecorridoEntregas.calcular(red, "U0", null)));
        verificar("sin centros lanza excepción", lanza(() -> RecorridoEntregas.calcular(red, "U0", new String[0])));
        verificar("9 centros (más del máximo) lanza excepción",
                lanza(() -> RecorridoEntregas.calcular(red, "U0", centrosU(1, 9))));
        verificar("8 centros (el máximo) es válido",
                !lanza(() -> RecorridoEntregas.calcular(red, "U0", centrosU(1, 8))));
        verificar("un centro repetido lanza excepción",
                lanza(() -> RecorridoEntregas.calcular(red, "U0", new String[]{"U1", "U2", "U1"})));
        verificar("el depósito como centro lanza excepción",
                lanza(() -> RecorridoEntregas.calcular(red, "U0", new String[]{"U1", "U0"})));
        verificar("un centro inexistente lanza excepción",
                lanza(() -> RecorridoEntregas.calcular(red, "U0", new String[]{"Nada"})));
        verificar("un depósito inexistente lanza excepción",
                lanza(() -> RecorridoEntregas.calcular(red, "Nada", new String[]{"U1"})));
        verificar("tabla null lanza excepción",
                lanza(() -> RecorridoEntregas.calcular(red, null, "U0", new String[]{"U1"}, false)));

        RedVial cambia = redAnillo();
        TablaRutas vieja = FloydWarshall.calcular(cambia);
        cambia.deshabilitarConexion("C", "D");
        verificar("una tabla vieja no se usa: lanza IllegalStateException",
                lanzaEstado(() -> RecorridoEntregas.calcular(cambia, vieja, "D", new String[]{"A"}, false)));
    }

    //  Desde el sistema

    private static void pruebasSistema() {
        SistemaLogistica sistema = new SistemaLogistica(redSimetrica());
        String[] centros = {"A", "B", "C"};
        ResultadoRecorrido desdeSistema = sistema.recorridoEntregas("D", centros);
        ResultadoRecorrido directo = RecorridoEntregas.calcular(redSimetrica(), "D", centros);
        verificar("el sistema da el mismo recorrido que el algoritmo",
                texto(desdeSistema.getParadas()).equals(texto(directo.getParadas()))
                        && desdeSistema.getTiempoTotal() == directo.getTiempoTotal());

        int version = sistema.getRed().getVersion();
        sistema.recorridoEntregas("D", new String[]{"B", "C"});
        sistema.consultarRuta("D", "C");
        verificar("los recorridos y las consultas de A1 comparten una sola preparación",
                sistema.getPreparaciones() == 1);
        verificar("calcular un recorrido no modifica la red", sistema.getRed().getVersion() == version);

        sistema.deshabilitarConexion("A", "B");
        sistema.deshabilitarConexion("B", "A");
        ResultadoRecorrido cortado = sistema.recorridoEntregas("D", centros);
        verificar("si la red cambia, la tabla se recalcula", sistema.getPreparaciones() == 2);
        verificar("sin A <-> B pasa a D-A-C-B-D (12 min), que no usa la conexión cortada",
                texto(cortado.getParadas()).equals("D-A-C-B-D") && cortado.getTiempoTotal() == 12
                        && !contieneTramo(cortado, "A", "B") && !contieneTramo(cortado, "B", "A"));
    }

    //  Reparto integrado

    /** Red simétrica, un vehículo de 20 kg en D, cinco pedidos y cuatro lotes. */
    private static SistemaLogistica sistemaReparto() {
        SistemaLogistica sistema = new SistemaLogistica(redSimetrica());
        sistema.agregarPedido(new Pedido(1, "A", LocalDate.of(2026, 11, 10), 5, 50));
        sistema.agregarPedido(new Pedido(2, "B", LocalDate.of(2026, 11, 11), 4, 50));
        sistema.agregarPedido(new Pedido(3, "A", LocalDate.of(2026, 11, 12), 3, 50));
        sistema.agregarPedido(new Pedido(4, "C", LocalDate.of(2026, 11, 13), 2, 50));
        sistema.agregarPedido(new Pedido(5, "C", LocalDate.of(2026, 11, 14), 1, 50));
        sistema.agregarLote(new Lote(10, 10, 30));
        sistema.agregarLote(new Lote(11, 10, 20));
        sistema.agregarLote(new Lote(12, 5, 10));
        sistema.agregarLote(new Lote(13, 5, 5));
        sistema.agregarVehiculo(new Vehiculo(1, 20, "D"));
        return sistema;
    }

    private static void pruebasReparto() {
        SistemaLogistica sistema = sistemaReparto();
        ResultadoReparto r1 = sistema.planificarReparto(1, 3);
        verificar("reparto 1: se despacha", r1.isDespachado());
        verificar("reparto 1: atiende los 3 pedidos más urgentes (#1, #2, #3)", ids(r1.getPedidos()).equals("1,2,3"));
        verificar("reparto 1: #1 y #3 van a A y comparten la parada (2 centros: A y B)",
                texto(r1.getRecorrido().getCentros()).equals("A-B"));
        verificar("reparto 1: recorrido D-A-B-D de 10 min",
                texto(r1.getRecorrido().getParadas()).equals("D-A-B-D") && r1.getRecorrido().getTiempoTotal() == 10);
        verificar("reparto 1: la carga es la de mayor prioridad que entra (#10 + #11, prioridad 50)",
                r1.getCarga().getPrioridadTotal() == 50 && r1.getCarga().getPesoTotal() == 20);
        verificar("reparto 1: los pedidos atendidos dejan de estar pendientes",
                ids(sistema.getPedidosPendientes()).equals("4,5"));
        verificar("reparto 1: los lotes cargados salen del stock", idsLotes(sistema.getStock()).equals("12,13"));

        ResultadoReparto r2 = sistema.planificarReparto(1, 8);
        verificar("reparto 2: atiende los 2 que quedan, los dos a C (una sola parada)",
                r2.isDespachado() && ids(r2.getPedidos()).equals("4,5")
                        && texto(r2.getRecorrido().getParadas()).equals("D-C-D"));
        verificar("reparto 2: ida y vuelta a C en 12 min", r2.getRecorrido().getTiempoTotal() == 12);
        verificar("reparto 2: no quedan pedidos ni stock",
                sistema.getPedidosPendientes().length == 0 && sistema.getStock().length == 0);

        // Un pedido para el propio depósito no es una parada: se saltea y queda pendiente.
        SistemaLogistica conDeposito = sistemaReparto();
        conDeposito.agregarPedido(new Pedido(6, "D", LocalDate.of(2026, 11, 1), 5, 10));
        ResultadoReparto r3 = conDeposito.planificarReparto(1, 2);
        verificar("un pedido con destino en el depósito se saltea (atiende #1 y #2)",
                r3.isDespachado() && ids(r3.getPedidos()).equals("1,2"));
        verificar("el pedido al depósito sigue pendiente", ids(conDeposito.getPedidosPendientes()).equals("3,4,5,6"));

        // Con más destinos que el máximo, se llenan las 8 paradas y los pedidos a esas paradas siguen entrando.
        RedVial red = grafoCompleto(11, new Random(3));
        SistemaLogistica muchos = new SistemaLogistica(red);
        for (int i = 1; i <= 10; i++) {
            muchos.agregarPedido(new Pedido(i, "U" + i, LocalDate.of(2026, 11, i), 5, 10));
        }
        muchos.agregarPedido(new Pedido(11, "U1", LocalDate.of(2026, 11, 30), 5, 10));
        muchos.agregarLote(new Lote(1, 1, 1));
        muchos.agregarVehiculo(new Vehiculo(1, 10, "U0"));
        ResultadoReparto r4 = muchos.planificarReparto(1, 11);
        verificar("10 destinos distintos: el recorrido usa el máximo de 8 centros",
                r4.isDespachado() && r4.getRecorrido().getCentros().length == RecorridoEntregas.MAXIMO_CENTROS);
        verificar("entran los pedidos #1 a #8 y #11 (va a U1, que ya es parada)",
                ids(r4.getPedidos()).equals("1,2,3,4,5,6,7,8,11"));
        verificar("#9 y #10 quedan pendientes", ids(muchos.getPedidosPendientes()).equals("9,10"));
    }

    private static void pruebasRepartoNoDespachado() {
        SistemaLogistica vacio = new SistemaLogistica(redSimetrica());
        vacio.agregarVehiculo(new Vehiculo(1, 20, "D"));
        vacio.agregarLote(new Lote(10, 10, 30));
        ResultadoReparto sinPedidos = vacio.planificarReparto(1, 3);
        verificar("sin pedidos: no se despacha y no llega a armar la carga",
                !sinPedidos.isDespachado() && sinPedidos.getCarga() == null && sinPedidos.getRecorrido() == null);
        verificar("sin pedidos: el stock no cambia", vacio.getStock().length == 1);

        SistemaLogistica pesado = new SistemaLogistica(redSimetrica());
        pesado.agregarVehiculo(new Vehiculo(1, 5, "D"));
        pesado.agregarPedido(new Pedido(1, "A", LocalDate.of(2026, 11, 10), 5, 50));
        pesado.agregarLote(new Lote(10, 10, 30));
        ResultadoReparto sinCarga = pesado.planificarReparto(1, 3);
        verificar("ningún lote entra: no se despacha y no calcula el recorrido",
                !sinCarga.isDespachado() && sinCarga.getCarga().isVacia() && sinCarga.getRecorrido() == null);
        verificar("ningún lote entra: el pedido sigue pendiente y el stock no cambia",
                pesado.getPedidosPendientes().length == 1 && pesado.getStock().length == 1);

        RedVial red = redSimetrica();
        red.agregarUbicacion("S");
        red.agregarConexion("C", "S", 1); // se llega a S pero no se vuelve
        SistemaLogistica sinVuelta = new SistemaLogistica(red);
        sinVuelta.agregarVehiculo(new Vehiculo(1, 20, "D"));
        sinVuelta.agregarPedido(new Pedido(1, "A", LocalDate.of(2026, 11, 10), 5, 50));
        sinVuelta.agregarPedido(new Pedido(2, "S", LocalDate.of(2026, 11, 11), 4, 50));
        sinVuelta.agregarLote(new Lote(10, 10, 30));
        ResultadoReparto sinRecorrido = sinVuelta.planificarReparto(1, 2);
        verificar("sin recorrido posible: no se despacha e informa el recorrido",
                !sinRecorrido.isDespachado() && sinRecorrido.getRecorrido() != null
                        && !sinRecorrido.getRecorrido().isExiste());
        verificar("sin recorrido posible: no cambian los pedidos ni el stock",
                sinVuelta.getPedidosPendientes().length == 2 && sinVuelta.getStock().length == 1);

        SistemaLogistica sistema = sistemaReparto();
        verificar("cantidad de pedidos 0 lanza excepción", lanza(() -> sistema.planificarReparto(1, 0)));
        verificar("vehículo inexistente lanza excepción", lanza(() -> sistema.planificarReparto(9, 3)));
    }

    //  Contra fuerza bruta

    private static void pruebasContraFuerzaBruta() {
        Random azar = new Random(2026);
        int redes = 150;
        int coinciden = 0;
        int consistentes = 0;
        int conRecorrido = 0;
        for (int caso = 0; caso < redes; caso++) {
            int n = 3 + azar.nextInt(7); // 3 a 9 ubicaciones
            RedVial red = new RedVial();
            for (int i = 0; i < n; i++) red.agregarUbicacion("U" + i);
            int densidad = 30 + azar.nextInt(50);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j && azar.nextInt(100) < densidad) {
                        red.agregarConexion(i, j, 1 + azar.nextInt(20));
                    }
                }
            }
            int deposito = azar.nextInt(n);
            int k = 1 + azar.nextInt(Math.min(n - 1, RecorridoEntregas.MAXIMO_CENTROS));
            int[] paradas = elegirParadas(azar, n, deposito, k);
            String[] centros = new String[k];
            for (int i = 0; i < k; i++) centros[i] = "U" + paradas[i + 1];

            ResultadoRecorrido r = RecorridoEntregas.calcular(red, "U" + deposito, centros);
            int referencia = mejorPorFuerzaBruta(tiemposPorDijkstra(red, paradas));
            boolean igual = (referencia == SIN_CAMINO) ? !r.isExiste()
                    : (r.isExiste() && r.getTiempoTotal() == referencia);
            if (igual) coinciden++;
            if (!r.isExiste() || (empiezaYTerminaEnDeposito(r) && cadaCentroUnaVez(r)
                    && tiempoConsistente(r) && tramosMinimos(red, r))) {
                consistentes++;
            }
            if (r.isExiste()) conRecorrido++;
        }
        verificar(redes + " redes al azar: el mismo tiempo que probar todos los órdenes ("
                + conRecorrido + " con recorrido)", coinciden == redes);
        verificar(redes + " redes al azar: cada centro una vez, tramos mínimos y tiempo total consistente",
                consistentes == redes);
    }

    //  Volumen

    private static void pruebaVolumen() {
        RedVial red = grafoCompleto(9, new Random(99));
        int[] paradas = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        long inicio = System.currentTimeMillis();
        ResultadoRecorrido r = RecorridoEntregas.calcular(red, "U0", centrosU(1, 8));
        long ms = System.currentTimeMillis() - inicio;
        verificar("grafo completo con 8 centros: óptimo igual a fuerza bruta ("
                + r.getTiempoTotal() + " min, " + r.getNodosExplorados() + " nodos, " + ms + " ms)",
                r.isExiste() && r.getTiempoTotal() == mejorPorFuerzaBruta(tiemposPorDijkstra(red, paradas)));
        verificar("grafo completo con 8 centros: termina en menos de 2 segundos", ms < 2000);
    }

    //  Referencia independiente: tiempos con Dijkstra (F2) y todos los órdenes posibles, sin podas

    private static int[][] tiemposPorDijkstra(RedVial red, int[] paradas) {
        int n = paradas.length;
        int[][] t = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                ResultadoRuta ruta = Dijkstra.calcular(red, paradas[i], paradas[j], false);
                t[i][j] = ruta.isExiste() ? ruta.getTiempoTotal() : SIN_CAMINO;
            }
        }
        return t;
    }

    /** Menor tiempo de 0 -> permutación de 1..k -> 0 probando todas las permutaciones. */
    private static int mejorPorFuerzaBruta(int[][] t) {
        int[] perm = new int[t.length - 1];
        for (int i = 0; i < perm.length; i++) perm[i] = i + 1;
        return permutar(t, perm, 0);
    }

    private static int permutar(int[][] t, int[] perm, int pos) {
        if (pos == perm.length) {
            int total = 0;
            int anterior = 0;
            for (int p : perm) {
                if (t[anterior][p] == SIN_CAMINO) return SIN_CAMINO;
                total += t[anterior][p];
                anterior = p;
            }
            if (t[anterior][0] == SIN_CAMINO) return SIN_CAMINO;
            return total + t[anterior][0];
        }
        int mejor = SIN_CAMINO;
        for (int i = pos; i < perm.length; i++) {
            intercambiar(perm, pos, i);
            int candidato = permutar(t, perm, pos + 1);
            if (candidato < mejor) mejor = candidato;
            intercambiar(perm, pos, i);
        }
        return mejor;
    }

    private static void intercambiar(int[] a, int i, int j) {
        int aux = a[i];
        a[i] = a[j];
        a[j] = aux;
    }

    /** Devuelve el depósito en la posición 0 y k centros distintos al azar en las siguientes. */
    private static int[] elegirParadas(Random azar, int n, int deposito, int k) {
        int[] paradas = new int[k + 1];
        boolean[] usada = new boolean[n];
        paradas[0] = deposito;
        usada[deposito] = true;
        for (int i = 1; i <= k; i++) {
            int u;
            do {
                u = azar.nextInt(n);
            } while (usada[u]);
            usada[u] = true;
            paradas[i] = u;
        }
        return paradas;
    }

    //  Auxiliares

    private static void dobleMano(RedVial red, String a, String b, int tiempo) {
        red.agregarConexion(a, b, tiempo);
        red.agregarConexion(b, a, tiempo);
    }

    /** n ubicaciones U0..U(n-1) con conexiones en los dos sentidos entre todo par, tiempos de 1 a 30. */
    private static RedVial grafoCompleto(int n, Random azar) {
        RedVial red = new RedVial();
        for (int i = 0; i < n; i++) red.agregarUbicacion("U" + i);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) red.agregarConexion(i, j, 1 + azar.nextInt(30));
            }
        }
        return red;
    }

    private static String[] centrosU(int desde, int cantidad) {
        String[] centros = new String[cantidad];
        for (int i = 0; i < cantidad; i++) centros[i] = "U" + (desde + i);
        return centros;
    }

    /** Corre con traza sin ensuciar la salida de las pruebas. */
    private static ResultadoRecorrido calcularEnSilencio(RedVial red, String deposito, String[] centros) {
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            return RecorridoEntregas.calcular(red, deposito, centros, true);
        } finally {
            System.setOut(original);
        }
    }

    private static boolean empiezaYTerminaEnDeposito(ResultadoRecorrido r) {
        Ubicacion[] p = r.getParadas();
        return p[0].getId() == r.getDeposito().getId() && p[p.length - 1].getId() == r.getDeposito().getId();
    }

    /** Las paradas intermedias son exactamente los centros pedidos, cada uno una sola vez. */
    private static boolean cadaCentroUnaVez(ResultadoRecorrido r) {
        Ubicacion[] p = r.getParadas();
        Ubicacion[] centros = r.getCentros();
        if (p.length != centros.length + 2) return false;
        for (Ubicacion centro : centros) {
            int veces = 0;
            for (int i = 1; i < p.length - 1; i++) {
                if (p[i].getId() == centro.getId()) veces++;
            }
            if (veces != 1) return false;
        }
        return true;
    }

    private static boolean tiempoConsistente(ResultadoRecorrido r) {
        int total = 0;
        ResultadoConsultaRuta[] tramos = r.getTramos();
        Ubicacion[] p = r.getParadas();
        for (int i = 0; i < tramos.length; i++) {
            Ubicacion[] c = tramos[i].getCamino();
            if (c[0].getId() != p[i].getId() || c[c.length - 1].getId() != p[i + 1].getId()) return false;
            total += tramos[i].getTiempoTotal();
        }
        return total == r.getTiempoTotal();
    }

    private static boolean tramosMinimos(RedVial red, ResultadoRecorrido r) {
        Ubicacion[] p = r.getParadas();
        for (int i = 0; i + 1 < p.length; i++) {
            ResultadoRuta dijkstra = Dijkstra.calcular(red, p[i].getId(), p[i + 1].getId(), false);
            if (!dijkstra.isExiste() || dijkstra.getTiempoTotal() != r.getTramos()[i].getTiempoTotal()) return false;
        }
        return true;
    }

    private static boolean contieneTramo(ResultadoRecorrido r, String a, String b) {
        Ubicacion[] c = r.getCamino();
        for (int i = 0; i + 1 < c.length; i++) {
            if (c[i].getNombre().equals(a) && c[i + 1].getNombre().equals(b)) return true;
        }
        return false;
    }

    /** Nombres unidos con "-" (en las pruebas los nombres son de una letra o U + número). */
    private static String texto(Ubicacion[] ubicaciones) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ubicaciones.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(ubicaciones[i]);
        }
        return sb.toString();
    }

    private static String ids(Pedido[] pedidos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pedidos.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(pedidos[i].getId());
        }
        return sb.toString();
    }

    private static String idsLotes(Lote[] lotes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lotes.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(lotes[i].getId());
        }
        return sb.toString();
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean lanzaEstado(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalStateException e) {
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
