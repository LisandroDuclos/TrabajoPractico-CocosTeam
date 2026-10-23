package logistica.resultado;

import logistica.modelo.Pedido;
import logistica.modelo.Vehiculo;

// Viaje planificado
public class ResultadoViaje {

    private final Vehiculo vehiculo;
    private final Pedido pedido;          // null si no había pedidos pendientes
    private final ResultadoCarga carga;   // null si no se llegó a armar
    private final ResultadoRuta ruta;     // null si no se llegó a calcular
    private final String motivo;          // null si el viaje se despachó

    private ResultadoViaje(Vehiculo vehiculo, Pedido pedido, ResultadoCarga carga,
                           ResultadoRuta ruta, String motivo) {
        this.vehiculo = vehiculo;
        this.pedido = pedido;
        this.carga = carga;
        this.ruta = ruta;
        this.motivo = motivo;
    }

    public static ResultadoViaje despachado(Vehiculo vehiculo, Pedido pedido,
                                            ResultadoCarga carga, ResultadoRuta ruta) {
        return new ResultadoViaje(vehiculo, pedido, carga, ruta, null);
    }

    public static ResultadoViaje noDespachado(Vehiculo vehiculo, Pedido pedido, ResultadoCarga carga,
                                              ResultadoRuta ruta, String motivo) {
        return new ResultadoViaje(vehiculo, pedido, carga, ruta, motivo);
    }

    public boolean isDespachado() { return motivo == null; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public Pedido getPedido() { return pedido; }
    public ResultadoCarga getCarga() { return carga; }
    public ResultadoRuta getRuta() { return ruta; }
    public String getMotivo() { return motivo; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Viaje de V").append(vehiculo.getId())
                .append(isDespachado() ? ": DESPACHADO" : ": NO SE DESPACHA").append("\n");
        if (pedido != null) {
            sb.append("Pedido atendido (F1): ").append(pedido).append("\n");
        }
        if (carga != null) {
            sb.append("Carga (F3): ").append(carga).append("\n");
        }
        if (ruta != null) {
            sb.append("Ruta (F2): ").append(ruta).append("\n");
        }
        if (!isDespachado()) {
            sb.append("Motivo: ").append(motivo).append("\n");
        }
        sb.setLength(sb.length() - 1); // sin el último salto de línea, como los otros resultados
        return sb.toString();
    }
}
