package logistica.resultado;

import logistica.modelo.Ubicacion;

// Respuesta a una consulta de la tabla de rutas (A1)
public class ResultadoConsultaRuta {

    private final Ubicacion origen;
    private final Ubicacion destino;
    private final Ubicacion[] camino; // null si no existe conexión
    private final int tiempoTotal;    // sólo tiene sentido si existe conexión

    public ResultadoConsultaRuta(Ubicacion origen, Ubicacion destino, Ubicacion[] camino, int tiempoTotal) {
        this.origen = origen;
        this.destino = destino;
        this.camino = camino;
        this.tiempoTotal = tiempoTotal;
    }

    public boolean isExiste() { return camino != null; }
    public Ubicacion[] getCamino() { return camino; }
    public int getTiempoTotal() { return tiempoTotal; }

    @Override
    public String toString() {
        if (!isExiste()) {
            return "No existe conexión de " + origen + " a " + destino;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(origen).append(" a ").append(destino).append(": ").append(tiempoTotal).append(" min (");
        for (int i = 0; i < camino.length; i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(camino[i]);
        }
        return sb.append(")").toString();
    }
}
