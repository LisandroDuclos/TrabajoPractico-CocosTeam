package logistica.resultado;

import logistica.modelo.Pedido;
import logistica.modelo.Vehiculo;

// Reparto planificado: un viaje que atiende varios pedidos y visita sus centros en el mejor orden (B1)
public class ResultadoReparto {

    private final Vehiculo vehiculo;
    private final Pedido[] pedidos;            // vacío si no había pedidos para repartir
    private final ResultadoCarga carga;        // null si no se llegó a armar
    private final ResultadoRecorrido recorrido; // null si no se llegó a calcular
    private final String motivo;               // null si el reparto se despachó

    private ResultadoReparto(Vehiculo vehiculo, Pedido[] pedidos, ResultadoCarga carga,
                             ResultadoRecorrido recorrido, String motivo) {
        this.vehiculo = vehiculo;
        this.pedidos = pedidos;
        this.carga = carga;
        this.recorrido = recorrido;
        this.motivo = motivo;
    }

    public static ResultadoReparto despachado(Vehiculo vehiculo, Pedido[] pedidos,
                                              ResultadoCarga carga, ResultadoRecorrido recorrido) {
        return new ResultadoReparto(vehiculo, pedidos, carga, recorrido, null);
    }

    public static ResultadoReparto noDespachado(Vehiculo vehiculo, Pedido[] pedidos, ResultadoCarga carga,
                                                ResultadoRecorrido recorrido, String motivo) {
        return new ResultadoReparto(vehiculo, pedidos, carga, recorrido, motivo);
    }

    public boolean isDespachado() { return motivo == null; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public Pedido[] getPedidos() { return pedidos; }
    public ResultadoCarga getCarga() { return carga; }
    public ResultadoRecorrido getRecorrido() { return recorrido; }
    public String getMotivo() { return motivo; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reparto de V").append(vehiculo.getId())
                .append(isDespachado() ? ": DESPACHADO" : ": NO SE DESPACHA").append("\n");
        for (Pedido p : pedidos) {
            sb.append("Pedido atendido (F1): ").append(p).append("\n");
        }
        if (carga != null) {
            sb.append("Carga (F3): ").append(carga).append("\n");
        }
        if (recorrido != null) {
            sb.append("Recorrido (B1): ").append(recorrido).append("\n");
        }
        if (!isDespachado()) {
            sb.append("Motivo: ").append(motivo).append("\n");
        }
        sb.setLength(sb.length() - 1); // sin el último salto de línea, como los otros resultados
        return sb.toString();
    }
}
