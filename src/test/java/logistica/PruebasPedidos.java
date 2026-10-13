package logistica;

import java.time.LocalDate;
import java.util.Random;

import logistica.algoritmos.dyc.BusquedaBinaria;
import logistica.algoritmos.dyc.MergeSort;
import logistica.modelo.Pedido;
import logistica.modelo.Pedido.Criterio;
import logistica.resultado.ResultadoOrdenamiento;

/**
 * Casos de prueba de la funcionalidad 1 (Merge Sort y búsqueda binaria).
 */
public class PruebasPedidos {

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas: ordenamiento ===");
        pruebasOrdenamiento();
        System.out.println("=== Pruebas: búsqueda ===");
        pruebasBusqueda();
        System.out.println("=== Prueba de volumen ===");
        pruebaVolumen();

        System.out.println();
        System.out.println("Resultado: " + ok + "/" + (ok + fallas) + " OK");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    private static void pruebasOrdenamiento() {
        verificar("arreglo vacío", MergeSort.ordenar(new Pedido[0], Criterio.FECHA).getPedidos().length == 0);

        Pedido unico = p(1, 2026, 11, 10, 3);
        Pedido[] uno = MergeSort.ordenar(new Pedido[] { unico }, Criterio.PRIORIDAD).getPedidos();
        verificar("arreglo de un elemento", uno.length == 1 && uno[0] == unico);

        Pedido[] yaOrdenado = { p(1, 2026, 11, 1, 1), p(2, 2026, 11, 2, 1), p(3, 2026, 11, 3, 1), p(4, 2026, 11, 4, 1) };
        verificar("ya ordenado por fecha", ids(MergeSort.ordenar(yaOrdenado, Criterio.FECHA).getPedidos(), 1, 2, 3, 4));

        Pedido[] inverso = { p(4, 2026, 11, 4, 1), p(3, 2026, 11, 3, 1), p(2, 2026, 11, 2, 1), p(1, 2026, 11, 1, 1) };
        verificar("orden inverso por fecha", ids(MergeSort.ordenar(inverso, Criterio.FECHA).getPedidos(), 1, 2, 3, 4));

        // Fecha: empate de fecha -> id menor.
        Pedido[] mismaFecha = { p(9, 2026, 11, 5, 1), p(3, 2026, 11, 5, 1), p(5, 2026, 11, 1, 1) };
        verificar("fecha: empate se resuelve por id menor",
                ids(MergeSort.ordenar(mismaFecha, Criterio.FECHA).getPedidos(), 5, 3, 9));

        // Prioridad descendente.
        Pedido[] prioridades = { p(1, 2026, 11, 1, 1), p(2, 2026, 11, 1, 5), p(3, 2026, 11, 1, 3) };
        verificar("prioridad descendente",
                ids(MergeSort.ordenar(prioridades, Criterio.PRIORIDAD).getPedidos(), 2, 3, 1));

        // Prioridad: empate de prioridad -> fecha más cercana.
        Pedido[] empatePrioridad = { p(1, 2026, 11, 20, 4), p(2, 2026, 11, 10, 4), p(3, 2026, 11, 15, 4) };
        verificar("prioridad: empate se resuelve por fecha más cercana",
                ids(MergeSort.ordenar(empatePrioridad, Criterio.PRIORIDAD).getPedidos(), 2, 3, 1));

        // Prioridad: empate de prioridad y fecha -> id menor.
        Pedido[] empateTotal = { p(8, 2026, 11, 10, 4), p(2, 2026, 11, 10, 4), p(5, 2026, 11, 10, 4) };
        verificar("prioridad: empate de prioridad y fecha se resuelve por id menor",
                ids(MergeSort.ordenar(empateTotal, Criterio.PRIORIDAD).getPedidos(), 2, 5, 8));

        // El arreglo original no se modifica.
        Pedido[] original = { p(3, 2026, 11, 3, 1), p(1, 2026, 11, 1, 1), p(2, 2026, 11, 2, 1) };
        MergeSort.ordenar(original, Criterio.ID);
        verificar("no modifica el arreglo original", ids(original, 3, 1, 2));
    }

    private static void pruebasBusqueda() {
        Pedido[] desordenados = {
            p(40, 2026, 11, 1, 1), p(10, 2026, 11, 1, 1), p(50, 2026, 11, 1, 1),
            p(20, 2026, 11, 1, 1), p(30, 2026, 11, 1, 1),
        };
        Pedido[] porId = MergeSort.ordenar(desordenados, Criterio.ID).getPedidos();

        verificar("busca el primero", idEncontrado(porId, 10));
        verificar("busca el último", idEncontrado(porId, 50));
        verificar("busca uno del medio", idEncontrado(porId, 30));
        verificar("id inexistente menor al mínimo", !BusquedaBinaria.buscarPorId(porId, 5).isEncontrado());
        verificar("id inexistente mayor al máximo", !BusquedaBinaria.buscarPorId(porId, 99).isEncontrado());
        verificar("id inexistente entre dos existentes", !BusquedaBinaria.buscarPorId(porId, 25).isEncontrado());
        verificar("búsqueda en arreglo vacío", !BusquedaBinaria.buscarPorId(new Pedido[0], 1).isEncontrado());
    }

    private static void pruebaVolumen() {
        int n = 100_000;
        Random random = new Random(42);
        Pedido[] pedidos = new Pedido[n];
        for (int i = 0; i < n; i++) {
            LocalDate fecha = LocalDate.of(2026, 1, 1).plusDays(random.nextInt(365));
            pedidos[i] = new Pedido(i + 1, "Centro", fecha, random.nextInt(10), 1 + random.nextInt(500));
        }
        // Se mezclan los ids para que el orden por id no venga dado.
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Pedido t = pedidos[i]; pedidos[i] = pedidos[j]; pedidos[j] = t;
        }

        long inicio = System.nanoTime();
        ResultadoOrdenamiento r = MergeSort.ordenar(pedidos, Criterio.PRIORIDAD);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        verificar("100.000 pedidos quedan ordenados por prioridad", estaOrdenado(r.getPedidos(), Criterio.PRIORIDAD));
        long nLogN = Math.round(n * (Math.log(n) / Math.log(2)));
        System.out.println("    n = " + n + ", tiempo = " + ms + " ms, comparaciones = "
                + r.getComparaciones() + " (n*log2(n) = " + nLogN + ")");

        Pedido[] porId = MergeSort.ordenar(pedidos, Criterio.ID).getPedidos();
        verificar("100.000 pedidos: búsqueda en a lo sumo techo(log2(n+1)) = 17 pasos",
                BusquedaBinaria.buscarPorId(porId, 77_777).getPasos() <= 17);
    }

    // ---- Auxiliares ----

    private static Pedido p(int id, int anio, int mes, int dia, int prioridad) {
        return new Pedido(id, "Centro " + id, LocalDate.of(anio, mes, dia), prioridad, 100);
    }

    private static boolean ids(Pedido[] pedidos, int... esperados) {
        if (pedidos.length != esperados.length) return false;
        for (int i = 0; i < pedidos.length; i++) {
            if (pedidos[i].getId() != esperados[i]) return false;
        }
        return true;
    }

    private static boolean idEncontrado(Pedido[] porId, int id) {
        var r = BusquedaBinaria.buscarPorId(porId, id);
        return r.isEncontrado() && r.getPedido().getId() == id;
    }

    private static boolean estaOrdenado(Pedido[] pedidos, Criterio criterio) {
        for (int i = 1; i < pedidos.length; i++) {
            if (pedidos[i - 1].compararPor(criterio, pedidos[i]) > 0) return false;
        }
        return true;
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
