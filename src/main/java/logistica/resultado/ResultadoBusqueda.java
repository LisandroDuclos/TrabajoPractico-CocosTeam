package logistica.resultado;

import logistica.modelo.Pedido;

/**
 * Lo que devuelve la búsqueda: el pedido encontrado (o null)
 * y la cantidad de pasos (elementos del medio examinados).
 */
public class ResultadoBusqueda {

    private final int idBuscado;
    private final Pedido pedido;
    private final int pasos;

    public ResultadoBusqueda(int idBuscado, Pedido pedido, int pasos) {
        this.idBuscado = idBuscado;
        this.pedido = pedido;
        this.pasos = pasos;
    }

    public boolean isEncontrado() { return pedido != null; }
    public Pedido getPedido() { return pedido; }
    public int getPasos() { return pasos; }

    @Override
    public String toString() {
        if (isEncontrado()) {
            return "Pedido " + idBuscado + " encontrado en " + pasos + " pasos: " + pedido;
        }
        return "Pedido " + idBuscado + " no existe (" + pasos + " pasos)";
    }
}
