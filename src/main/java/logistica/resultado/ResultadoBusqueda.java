package logistica.resultado;

import logistica.modelo.Pedido;

// Pedido encontrado
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
