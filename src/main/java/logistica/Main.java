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
import logistica.sistema.BitacoraViajes;
import logistica.sistema.SistemaLogistica;

public class Main {

    public static void main(String[] args) {
        demoPedidos();
        demoRutas();
        demoCargas();
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