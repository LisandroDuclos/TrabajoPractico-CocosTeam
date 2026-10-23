package logistica.resultado;

import logistica.modelo.Pedido;

// Pedidos ordenados y cantidad de comparaciones
public class ResultadoOrdenamiento {

    private final Pedido[] pedidos;
    private final Pedido.Criterio criterio;
    private final long comparaciones;

    public ResultadoOrdenamiento(Pedido[] pedidos, Pedido.Criterio criterio, long comparaciones) {
        this.pedidos = pedidos;
        this.criterio = criterio;
        this.comparaciones = comparaciones;
    }

    public Pedido[] getPedidos() { return pedidos; }
    public Pedido.Criterio getCriterio() { return criterio; }
    public long getComparaciones() { return comparaciones; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pedidos ordenados por ").append(criterio)
          .append(" (").append(comparaciones).append(" comparaciones)\n");
        for (int i = 0; i < pedidos.length; i++) {
            sb.append(String.format("  %2d. %s%n", i + 1, pedidos[i]));
        }
        return sb.toString();
    }
}
