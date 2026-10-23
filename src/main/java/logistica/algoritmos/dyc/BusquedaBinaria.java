package logistica.algoritmos.dyc;

import logistica.modelo.Pedido;
import logistica.resultado.ResultadoBusqueda;

// Busqueda binaria
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
