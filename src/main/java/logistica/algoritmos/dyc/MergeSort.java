package logistica.algoritmos.dyc;

import logistica.modelo.Pedido;
import logistica.modelo.Pedido.Criterio;
import logistica.resultado.ResultadoOrdenamiento;

/**
 * Ordenamiento de pedidos por Merge Sort (Divide y Conquista).
 *
 *   DIVIDIR:     se parte el arreglo en dos mitades por el medio.
 *   CONQUISTAR:  se ordena recursivamente cada mitad.
 *   COMBINAR:    se mezclan las dos mitades ya ordenadas en un solo tramo ordenado.
 *
 * Complejidad temporal: T(n) = 2T(n/2) + O(n)  =>  O(n log n) en el mejor,
 * promedio y peor caso (la división siempre es por la mitad, sin importar los datos).
 * Espacio extra: O(n) por el arreglo auxiliar usado al mezclar.
 *
 * Es estable: ante dos pedidos equivalentes se conserva el orden original.
 */
public class MergeSort {

    private static long comparaciones;
    private static boolean traza;

    public static ResultadoOrdenamiento ordenar(Pedido[] pedidos, Criterio criterio) {
        return ordenar(pedidos, criterio, false);
    }

    public static ResultadoOrdenamiento ordenar(Pedido[] pedidos, Criterio criterio, boolean conTraza) {
        comparaciones = 0;
        traza = conTraza;

        // Se trabaja sobre una copia para no modificar el arreglo original.
        Pedido[] copia = new Pedido[pedidos.length];
        for (int i = 0; i < pedidos.length; i++) {
            copia[i] = pedidos[i];
        }
        // Un único arreglo auxiliar, reservado una sola vez para todas las mezclas.
        Pedido[] aux = new Pedido[pedidos.length];

        mergeSort(copia, aux, 0, copia.length - 1, criterio, 0);
        return new ResultadoOrdenamiento(copia, criterio, comparaciones);
    }

    /** Ordena el tramo arr[ini..fin] (ambos inclusive). */
    private static void mergeSort(Pedido[] arr, Pedido[] aux, int ini, int fin, Criterio criterio, int nivel) {
        // CASO BASE: un tramo de 0 o 1 elemento ya está ordenado.
        if (ini >= fin) {
            return;
        }

        // DIVIDIR
        int medio = ini + (fin - ini) / 2;
        imprimirTraza(nivel, "Dividir " + ids(arr, ini, fin) + " -> "
                + ids(arr, ini, medio) + " | " + ids(arr, medio + 1, fin));

        // CONQUISTAR
        mergeSort(arr, aux, ini, medio, criterio, nivel + 1);
        mergeSort(arr, aux, medio + 1, fin, criterio, nivel + 1);

        // COMBINAR
        mezclar(arr, aux, ini, medio, fin, criterio);
        imprimirTraza(nivel, "Mezclar -> " + ids(arr, ini, fin));
    }

    /**
     * Mezcla los tramos ordenados arr[ini..medio] y arr[medio+1..fin].
     * Recorre ambos con un índice cada uno y en cada paso copia el menor: O(n).
     */
    private static void mezclar(Pedido[] arr, Pedido[] aux, int ini, int medio, int fin, Criterio criterio) {
        for (int k = ini; k <= fin; k++) {
            aux[k] = arr[k];
        }

        int i = ini;        // recorre la mitad izquierda
        int j = medio + 1;  // recorre la mitad derecha
        int k = ini;        // posición a escribir en arr

        while (i <= medio && j <= fin) {
            comparaciones++;
            // "<= 0" toma primero el de la izquierda en caso de empate: eso da la estabilidad.
            if (aux[i].compararPor(criterio, aux[j]) <= 0) {
                arr[k++] = aux[i++];
            } else {
                arr[k++] = aux[j++];
            }
        }
        // Lo que quede de una de las mitades ya está ordenado: se copia tal cual.
        while (i <= medio) {
            arr[k++] = aux[i++];
        }
        while (j <= fin) {
            arr[k++] = aux[j++];
        }
    }

    // ---- Traza para visualizar la ejecución ----

    private static void imprimirTraza(int nivel, String mensaje) {
        if (traza) {
            System.out.println("  ".repeat(nivel + 1) + mensaje);
        }
    }

    private static String ids(Pedido[] arr, int ini, int fin) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = ini; i <= fin; i++) {
            if (i > ini) sb.append(", ");
            sb.append(arr[i].getId());
        }
        return sb.append("]").toString();
    }
}
