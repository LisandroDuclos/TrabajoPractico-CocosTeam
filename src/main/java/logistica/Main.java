package logistica;

import java.time.LocalDate;

import logistica.algoritmos.dinamica.SeleccionCargas;
import logistica.algoritmos.dyc.BusquedaBinaria;
import logistica.algoritmos.dyc.MergeSort;
import logistica.algoritmos.greedy.Dijkstra;
import logistica.modelo.Lote;
import logistica.modelo.Pedido;
import logistica.modelo.Pedido.Criterio;
import logistica.modelo.RedVial;
import logistica.modelo.Vehiculo;
import logistica.resultado.ResultadoOrdenamiento;
import logistica.resultado.ResultadoRutasAlternativas;
import logistica.sistema.BitacoraViajes;
import logistica.sistema.SistemaLogistica;

public class Main {

    public static void main(String[] args) {
        demoPedidos();
        demoRutas();
        demoCargas();
        demoConsultasRutas();
        demoRutasAlternativas();
        demoRecorridoEntregas();
        demoIntegrada();
    }

    // ---- Funcionalidad 1: organización y consulta de pedidos (Divide y Conquista) ----

    private static void demoPedidos() {
        titulo("FUNCIONALIDAD 1 - Organización y consulta de pedidos");

        // Datos hardcoded para la demo:
        //  - 104 y 107: misma prioridad (5), distinta fecha  -> gana la fecha más cercana.
        //  - 102 y 109: misma prioridad (3) y misma fecha    -> gana el id menor.
        //  - 101 y 105: misma fecha                          -> en el orden por fecha gana el id menor.
        Pedido[] pedidos = {
                new Pedido(107, "Centro Norte",  LocalDate.of(2026, 11, 20), 5, 300),
                new Pedido(102, "Centro Sur",    LocalDate.of(2026, 11, 15), 3, 120),
                new Pedido(110, "Centro Oeste",  LocalDate.of(2026, 11, 30), 1,  80),
                new Pedido(104, "Centro Este",   LocalDate.of(2026, 11, 18), 5, 250),
                new Pedido(101, "Centro Centro", LocalDate.of(2026, 11, 12), 2, 400),
                new Pedido(109, "Centro Sur",    LocalDate.of(2026, 11, 15), 3,  60),
                new Pedido(103, "Centro Norte",  LocalDate.of(2026, 11, 25), 4, 150),
                new Pedido(105, "Centro Este",   LocalDate.of(2026, 11, 12), 1, 210),
                new Pedido(108, "Centro Oeste",  LocalDate.of(2026, 11, 22), 4,  90),
                new Pedido(106, "Centro Centro", LocalDate.of(2026, 11, 28), 2, 175),
        };

        System.out.println(MergeSort.ordenar(pedidos, Criterio.FECHA));
        System.out.println(MergeSort.ordenar(pedidos, Criterio.PRIORIDAD));

        // Ordenamiento
        Pedido[] porId = MergeSort.ordenar(pedidos, Criterio.ID).getPedidos();
        System.out.println(BusquedaBinaria.buscarPorId(porId, 108));
        System.out.println(BusquedaBinaria.buscarPorId(porId, 111));

        // Traza
        titulo("Traza de Merge Sort (por prioridad) y búsqueda binaria");
        Pedido[] chico = { pedidos[0], pedidos[1], pedidos[2], pedidos[3], pedidos[4], pedidos[5] };
        ResultadoOrdenamiento r = MergeSort.ordenar(chico, Criterio.PRIORIDAD, true);
        System.out.println(r);

        Pedido[] chicoPorId = MergeSort.ordenar(chico, Criterio.ID).getPedidos();
        System.out.println("Buscar id 109:");
        System.out.println(BusquedaBinaria.buscarPorId(chicoPorId, 109, true));
        System.out.println("Buscar id 105:");
        System.out.println(BusquedaBinaria.buscarPorId(chicoPorId, 105, true));
    }

    //  Funcionalidad 2

    private static void demoRutas() {
        titulo("FUNCIONALIDAD 2 - Cálculo de la ruta más rápida");

        RedVial red = crearRedDeEjemplo();
        System.out.println(red);

        // 1) Cálculo normal, mostrando las decisiones del algoritmo.
        subtitulo("1) Depósito Central -> Depósito Sur");
        System.out.println(Dijkstra.calcular(red, "Depósito Central", "Depósito Sur", true));

        // 2) El sentido de circulación importa: ida y vuelta pueden tener tiempos distintos.
        subtitulo("2) Sentido de circulación: ida y vuelta entre Central y Norte");
        System.out.println(Dijkstra.calcular(red, "Depósito Central", "Centro Norte"));
        System.out.println(Dijkstra.calcular(red, "Centro Norte", "Depósito Central"));

        // 3) Se deshabilita una conexión y se vuelve a calcular.
        subtitulo("3) Se deshabilita Centro Norte -> Centro Este y se recalcula");
        red.deshabilitarConexion("Centro Norte", "Centro Este");
        System.out.println(Dijkstra.calcular(red, "Depósito Central", "Depósito Sur", true));

        // 4) Se corta la única conexión que llega al Puerto: no hay ruta disponible.
        subtitulo("4) Depósito Central -> Centro Puerto, antes y después de cortar Sur -> Puerto");
        System.out.println(Dijkstra.calcular(red, "Depósito Central", "Centro Puerto"));
        red.deshabilitarConexion("Depósito Sur", "Centro Puerto");
        System.out.println(Dijkstra.calcular(red, "Depósito Central", "Centro Puerto", true));
    }

    // Red ejemplo
    private static RedVial crearRedDeEjemplo() {
        RedVial red = new RedVial();
        red.agregarUbicacion("Depósito Central");
        red.agregarUbicacion("Depósito Sur");
        red.agregarUbicacion("Centro Norte");
        red.agregarUbicacion("Centro Este");
        red.agregarUbicacion("Centro Oeste");
        red.agregarUbicacion("Centro Puerto");

        red.agregarConexion("Depósito Central", "Centro Norte", 10);
        red.agregarConexion("Depósito Central", "Centro Oeste", 15);
        red.agregarConexion("Depósito Central", "Centro Este", 25);
        red.agregarConexion("Centro Norte", "Centro Este", 8);
        red.agregarConexion("Centro Norte", "Depósito Sur", 30);
        red.agregarConexion("Centro Oeste", "Depósito Sur", 12);
        red.agregarConexion("Centro Este", "Depósito Sur", 5);
        red.agregarConexion("Depósito Sur", "Depósito Central", 20);
        red.agregarConexion("Depósito Sur", "Centro Puerto", 7);
        return red;
    }

    //  Funcionalidad 3

    private static void demoCargas() {
        titulo("FUNCIONALIDAD 3 - Selección de cargas prioritarias");

        // 1) Caso normal
        subtitulo("1) Vehículo de 30 kg con 6 lotes disponibles");
        Lote[] lotes = {
                new Lote(201,  8, 40),
                new Lote(202, 12, 55),
                new Lote(203,  5, 20),
                new Lote(204, 15, 70),
                new Lote(205, 10, 45),
                new Lote(206,  6, 30),
        };
        for (Lote lote : lotes) {
            System.out.println("  " + lote);
        }
        System.out.println("Reconstrucción:");
        System.out.println(SeleccionCargas.seleccionar(lotes, 30, true));

        // 2) Greedy por prioridad/peso
        subtitulo("2) Caso donde Greedy por prioridad/peso no da el óptimo (capacidad 50 kg)");
        Lote[] contraejemplo = {
                new Lote(301, 10, 60),
                new Lote(302, 20, 100),
                new Lote(303, 30, 120),
        };
        System.out.println("  Greedy por prioridad/peso: #301 + #302 -> 30 kg, prioridad 160");
        System.out.println(SeleccionCargas.seleccionar(contraejemplo, 50, true));

        // 3) Ningún lote entra en el vehículo
        subtitulo("3) Vehículo de 4 kg: ningún lote entra (el más liviano pesa 5 kg)");
        System.out.println(SeleccionCargas.seleccionar(lotes, 4, true));
    }

    //  Funcionalidad A1

    private static void demoConsultasRutas() {
        titulo("FUNCIONALIDAD A1 - Consultas de rutas entre todos los centros");

        SistemaLogistica sistema = new SistemaLogistica(crearRedDeEjemplo());

        // 1) Procesamiento inicial: se calcula una sola vez, mostrando las mejoras de cada etapa.
        subtitulo("1) Preparación con Floyd-Warshall");
        System.out.println(sistema.prepararConsultas(true));

        // 2) Las consultas leen la tabla: incluyen un par sin conexión y un origen igual al destino.
        subtitulo("2) Consultas sobre la tabla ya calculada");
        System.out.println(sistema.consultarRuta("Depósito Central", "Centro Puerto"));
        System.out.println(sistema.consultarRuta("Centro Norte", "Depósito Central"));
        System.out.println(sistema.consultarRuta("Depósito Central", "Depósito Central"));
        System.out.println(sistema.consultarRuta("Centro Puerto", "Depósito Central"));
        System.out.println("Preparaciones realizadas: " + sistema.getPreparaciones());

        // 3) Si la red cambia, la próxima consulta recalcula: nunca responde con datos viejos.
        subtitulo("3) Se corta Centro Norte -> Centro Este y se vuelve a consultar");
        sistema.deshabilitarConexion("Centro Norte", "Centro Este");
        System.out.println(sistema.consultarRuta("Depósito Central", "Depósito Sur"));
        System.out.println(sistema.consultarRuta("Centro Norte", "Centro Este"));
        System.out.println("Preparaciones realizadas: " + sistema.getPreparaciones());
    }

    //  Funcionalidad B3

    private static void demoRutasAlternativas() {
        titulo("FUNCIONALIDAD B3 - Rutas alternativas con restricciones (Backtracking)");

        SistemaLogistica sistema = new SistemaLogistica(crearRedParaAlternativas());
        String[] ninguna = {};

        // 1) Con tiempo holgado aparecen todas las rutas posibles, incluida la más rápida.
        subtitulo("1) Depósito Central a Centro Puerto, máximo 80 min, sin prohibidas (con traza)");
        System.out.println(sistema.rutasAlternativas("Depósito Central", "Centro Puerto", 80, ninguna, true));

        // 2) El tiempo máximo deja afuera las rutas largas.
        subtitulo("2) Mismo viaje con máximo 45 min");
        System.out.println(sistema.rutasAlternativas("Depósito Central", "Centro Puerto", 45, ninguna));

        // 3) Una ubicación prohibida descarta todas las rutas que pasan por ella.
        subtitulo("3) Máximo 80 min, sin pasar por Centro Este");
        System.out.println(sistema.rutasAlternativas("Depósito Central", "Centro Puerto", 80,
                new String[]{"Centro Este"}));

        // 4) Si las condiciones no dejan ninguna ruta, se informa.
        subtitulo("4) Máximo 20 min: no alcanza para ninguna ruta");
        System.out.println(sistema.rutasAlternativas("Depósito Central", "Centro Puerto", 20, ninguna));

        // 5) Si la red cambia (conexión deshabilitada), la búsqueda la respeta.
        subtitulo("5) Se corta Centro Este -> Depósito Sur y se vuelve a buscar (máximo 80 min)");
        sistema.deshabilitarConexion("Centro Este", "Depósito Sur");
        System.out.println(sistema.rutasAlternativas("Depósito Central", "Centro Puerto", 80, ninguna));
    }

    /** Red con conexiones en ambos sentidos (tiempos distintos): permite cadenas alternativas y ciclos. */
    private static RedVial crearRedParaAlternativas() {
        RedVial red = new RedVial();
        red.agregarUbicacion("Depósito Central");
        red.agregarUbicacion("Depósito Sur");
        red.agregarUbicacion("Centro Norte");
        red.agregarUbicacion("Centro Este");
        red.agregarUbicacion("Centro Oeste");
        red.agregarUbicacion("Centro Puerto");

        red.agregarConexion("Depósito Central", "Centro Norte", 10);
        red.agregarConexion("Centro Norte", "Depósito Central", 12);
        red.agregarConexion("Depósito Central", "Centro Oeste", 15);
        red.agregarConexion("Centro Oeste", "Depósito Central", 15);
        red.agregarConexion("Centro Norte", "Centro Este", 8);
        red.agregarConexion("Centro Este", "Centro Norte", 9);
        red.agregarConexion("Centro Norte", "Depósito Sur", 30);
        red.agregarConexion("Centro Oeste", "Depósito Sur", 12);
        red.agregarConexion("Depósito Sur", "Centro Oeste", 14);
        red.agregarConexion("Centro Este", "Depósito Sur", 5);
        red.agregarConexion("Depósito Sur", "Centro Este", 6);
        red.agregarConexion("Depósito Sur", "Centro Puerto", 7);
        red.agregarConexion("Centro Este", "Centro Puerto", 20);
        return red;
    }

    //  Funcionalidad B1

    private static void demoRecorridoEntregas() {
        titulo("FUNCIONALIDAD B1 - Recorrido con múltiples entregas (Backtracking)");

        SistemaLogistica sistema = new SistemaLogistica(crearRedParaAlternativas());

        // 1) Tres centros: se prueban los órdenes posibles y se podan los que ya no pueden mejorar.
        subtitulo("1) Desde Depósito Central por Centro Norte, Centro Este y Centro Oeste (con traza)");
        System.out.println(sistema.recorridoEntregas("Depósito Central",
                new String[]{"Centro Norte", "Centro Este", "Centro Oeste"}, true));

        // 2) En una red con sentidos únicos los tramos entre paradas atraviesan otras ubicaciones.
        subtitulo("2) Red de ejemplo (sentidos únicos): Centro Norte y Centro Oeste");
        SistemaLogistica sentidos = new SistemaLogistica(crearRedDeEjemplo());
        System.out.println(sentidos.recorridoEntregas("Depósito Central",
                new String[]{"Centro Norte", "Centro Oeste"}));

        // 3) Desde Centro Puerto no sale ninguna conexión: no se puede volver al depósito.
        subtitulo("3) Incluye Centro Puerto, que no tiene salida");
        System.out.println(sistema.recorridoEntregas("Depósito Central",
                new String[]{"Centro Norte", "Centro Puerto"}));

        // 4) Si la red cambia, la tabla de tiempos se recalcula y el recorrido la respeta.
        subtitulo("4) Se corta Centro Norte -> Centro Este y se repite el caso 1");
        sistema.deshabilitarConexion("Centro Norte", "Centro Este");
        System.out.println(sistema.recorridoEntregas("Depósito Central",
                new String[]{"Centro Norte", "Centro Este", "Centro Oeste"}));
        System.out.println("Preparaciones de la tabla de tiempos (A1): " + sistema.getPreparaciones());

        // 5) El máximo de la consigna: sin podas habría que completar 8! = 40.320 recorridos.
        subtitulo("5) Red de 8 centros: se visitan todos");
        SistemaLogistica grande = new SistemaLogistica(crearRedParaEntregas());
        String[] ocho = {"Centro Norte", "Centro Sur", "Centro Este", "Centro Oeste",
                "Centro Puerto", "Centro Río", "Centro Plaza", "Centro Estación"};
        System.out.println(grande.recorridoEntregas("Depósito Central", ocho));
        System.out.println("Sin podas se completarían 40320 recorridos (8!).");

        // 6) Integrado: un reparto atiende varios pedidos en un mismo viaje (F1 -> F3 -> B1).
        subtitulo("6) Reparto integrado con varios pedidos");
        SistemaLogistica reparto = new SistemaLogistica(crearRedParaAlternativas());
        reparto.agregarPedido(new Pedido(104, "Centro Este",  LocalDate.of(2026, 11, 18), 5, 250));
        reparto.agregarPedido(new Pedido(107, "Centro Norte", LocalDate.of(2026, 11, 20), 5, 300));
        reparto.agregarPedido(new Pedido(102, "Centro Este",  LocalDate.of(2026, 11, 15), 4, 120));
        reparto.agregarPedido(new Pedido(108, "Centro Oeste", LocalDate.of(2026, 11, 22), 4,  90));
        reparto.agregarPedido(new Pedido(110, "Centro Puerto", LocalDate.of(2026, 11, 30), 1, 80));
        reparto.agregarLote(new Lote(201,  8, 40));
        reparto.agregarLote(new Lote(202, 12, 55));
        reparto.agregarLote(new Lote(203,  5, 20));
        reparto.agregarLote(new Lote(204, 15, 70));
        reparto.agregarLote(new Lote(205, 10, 45));
        reparto.agregarLote(new Lote(206,  6, 30));
        reparto.agregarVehiculo(new Vehiculo(1, 30, "Depósito Central"));

        BitacoraViajes bitacora = new BitacoraViajes(reparto);
        bitacora.presentar(1);
        // Los 4 más urgentes van a 3 centros: Centro Este recibe dos pedidos en una sola parada.
        bitacora.reparto(1, 4, true);
        // Queda #110 en Centro Puerto, que no tiene salida: no se despacha y no cambia nada.
        bitacora.reparto(1, 4);
        bitacora.resumen();
    }

    /** 8 centros con conexiones en ambos sentidos: el máximo de centros que admite B1. */
    private static RedVial crearRedParaEntregas() {
        RedVial red = new RedVial();
        red.agregarUbicacion("Depósito Central");
        red.agregarUbicacion("Centro Norte");
        red.agregarUbicacion("Centro Sur");
        red.agregarUbicacion("Centro Este");
        red.agregarUbicacion("Centro Oeste");
        red.agregarUbicacion("Centro Puerto");
        red.agregarUbicacion("Centro Río");
        red.agregarUbicacion("Centro Plaza");
        red.agregarUbicacion("Centro Estación");

        dobleMano(red, "Depósito Central", "Centro Norte", 10);
        dobleMano(red, "Depósito Central", "Centro Oeste", 15);
        dobleMano(red, "Depósito Central", "Centro Plaza", 7);
        dobleMano(red, "Depósito Central", "Centro Estación", 12);
        dobleMano(red, "Centro Norte", "Centro Este", 8);
        dobleMano(red, "Centro Norte", "Centro Plaza", 9);
        dobleMano(red, "Centro Norte", "Centro Río", 18);
        dobleMano(red, "Centro Este", "Centro Puerto", 11);
        dobleMano(red, "Centro Este", "Centro Río", 6);
        dobleMano(red, "Centro Este", "Centro Sur", 14);
        dobleMano(red, "Centro Sur", "Centro Puerto", 9);
        dobleMano(red, "Centro Sur", "Centro Oeste", 16);
        dobleMano(red, "Centro Sur", "Centro Estación", 10);
        dobleMano(red, "Centro Oeste", "Centro Estación", 8);
        dobleMano(red, "Centro Oeste", "Centro Plaza", 11);
        dobleMano(red, "Centro Río", "Centro Puerto", 7);
        return red;
    }

    private static void dobleMano(RedVial red, String a, String b, int tiempo) {
        red.agregarConexion(a, b, tiempo);
        red.agregarConexion(b, a, tiempo);
    }

    //  Integración

    private static void demoIntegrada() {

        SistemaLogistica sistema = new SistemaLogistica(crearRedDeEjemplo());
        sistema.agregarPedido(new Pedido(107, "Centro Norte",  LocalDate.of(2026, 11, 20), 5, 300));
        sistema.agregarPedido(new Pedido(104, "Centro Este",   LocalDate.of(2026, 11, 18), 5, 250));
        sistema.agregarPedido(new Pedido(102, "Centro Este",   LocalDate.of(2026, 11, 15), 4, 120));
        sistema.agregarPedido(new Pedido(108, "Centro Oeste",  LocalDate.of(2026, 11, 22), 4,  90));
        sistema.agregarPedido(new Pedido(110, "Centro Puerto", LocalDate.of(2026, 11, 30), 1,  80));
        sistema.agregarLote(new Lote(201,  8, 40));
        sistema.agregarLote(new Lote(202, 12, 55));
        sistema.agregarLote(new Lote(203,  5, 20));
        sistema.agregarLote(new Lote(204, 15, 70));
        sistema.agregarLote(new Lote(205, 10, 45));
        sistema.agregarLote(new Lote(206,  6, 30));
        sistema.agregarVehiculo(new Vehiculo(1, 30, "Depósito Central"));

        BitacoraViajes bitacora = new BitacoraViajes(sistema);
        bitacora.presentar(1);

        // 1) 104 y 107 empatan en prioridad 5: gana 104 por fecha más cercana.
        bitacora.viaje(1, true);

        // 2) Sigue con 107 y el stock que quedó: los tres lotes entran (26 kg).
        bitacora.viaje(1);

        // 3) Llega mercadería, se corta Centro Norte -> Centro Este y F2 recalcula.
        bitacora.evento("llegan al depósito los lotes #207, #208 y #209");
        sistema.agregarLote(new Lote(207, 10, 50));
        sistema.agregarLote(new Lote(208, 12, 35));
        sistema.agregarLote(new Lote(209,  9, 40));
        bitacora.evento("se corta la conexión Centro Norte -> Centro Este");
        sistema.deshabilitarConexion("Centro Norte", "Centro Este");
        bitacora.viaje(1, true);

        bitacora.resumen();

        // A1 sobre el mismo sistema: los tiempos ya contemplan la conexión cortada.
        subtitulo("Tiempos desde Depósito Central hasta los destinos pendientes (A1)");
        for (Pedido pedido : sistema.pedidosPorPrioridad().getPedidos()) {
            System.out.println("Pedido #" + pedido.getId() + ": "
                    + sistema.consultarRuta("Depósito Central", pedido.getCentroDestino()));
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(texto);
        System.out.println("=".repeat(70));
    }

    private static void subtitulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }
}