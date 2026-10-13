package logistica;

import java.time.LocalDate;

import logistica.algoritmos.dyc.BusquedaBinaria;
import logistica.algoritmos.dyc.MergeSort;
import logistica.modelo.Pedido;
import logistica.modelo.Pedido.Criterio;
import logistica.resultado.ResultadoOrdenamiento;

public class Main {

    public static void main(String[] args) {
        demoPedidos();
    }

    // ---- Funcionalidad 1: organización y consulta de pedidos (Divide y Conquista) ----

    private static void demoPedidos() {
        titulo("FUNCIONALIDAD 1 - Organización y consulta de pedidos");

        // Datos con empates a propósito:
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

        // La búsqueda binaria necesita el arreglo ordenado por id: se prepara una vez.
        Pedido[] porId = MergeSort.ordenar(pedidos, Criterio.ID).getPedidos();
        System.out.println(BusquedaBinaria.buscarPorId(porId, 108));
        System.out.println(BusquedaBinaria.buscarPorId(porId, 111));

        // Traza paso a paso con un conjunto chico, para ver cómo trabaja D&C.
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

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(texto);
        System.out.println("=".repeat(70));
    }
}
