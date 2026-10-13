package logistica.algoritmos.dyc;

import logistica.modelo.Pedido;
import logistica.resultado.ResultadoBusqueda;

/**
 * Búsqueda de un pedido por identificador mediante búsqueda binaria (Divide y Conquista).
 *
 *   DIVIDIR:     se toma el elemento del medio del intervalo [ini, fin].
 *   CONQUISTAR:  si no es el buscado, se resuelve el subproblema en UNA sola mitad;
 *                la otra se descarta porque, al estar ordenado, no puede contener el id.
 *   COMBINAR:    trivial, la respuesta de la mitad es la respuesta del problema.
 *
 * Precondición (organización de los datos): el arreglo debe estar ordenado por id
 * (MergeSort con Criterio.ID). Ese orden se prepara una vez en O(n log n) y luego
 * se reutiliza en todas las búsquedas.
 *
 * Complejidad temporal: T(n) = T(n/2) + O(1)  =>  O(log n) por búsqueda.
 */
public class BusquedaBinaria {

    private static int pasos;
    private static boolean traza;

    public static ResultadoBusqueda buscarPorId(Pedido[] ordenadosPorId, int id) {
        return buscarPorId(ordenadosPorId, id, false);
    }

    public static ResultadoBusqueda buscarPorId(Pedido[] ordenadosPorId, int id, boolean conTraza) {
        pasos = 0;
        traza = conTraza;
        Pedido encontrado = buscar(ordenadosPorId, id, 0, ordenadosPorId.length - 1);
        return new ResultadoBusqueda(id, encontrado, pasos);
    }

    private static Pedido buscar(Pedido[] arr, int id, int ini, int fin) {
        // CASO BASE: intervalo vacío, el id no está.
        if (ini > fin) {
            if (traza) System.out.println("  Intervalo vacío: el pedido " + id + " no existe");
            return null;
        }

        // DIVIDIR
        int medio = ini + (fin - ini) / 2;
        int idMedio = arr[medio].getId();
        pasos++;
        if (traza) {
            System.out.println("  Intervalo [" + ini + ", " + fin + "], medio = " + medio
                    + " (id " + idMedio + ")");
        }

        if (idMedio == id) {
            return arr[medio];
        }
        // CONQUISTAR: se sigue sólo en la mitad que puede contener el id.
        if (id < idMedio) {
            return buscar(arr, id, ini, medio - 1);
        } else {
            return buscar(arr, id, medio + 1, fin);
        }
    }
}
