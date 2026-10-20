package logistica.resultado;

import logistica.modelo.Ubicacion;

public class ResultadoRuta {

    private final Ubicacion origen;
    private final Ubicacion destino;
    private final Ubicacion[] camino; // null si no existe ruta
    private final int tiempoTotal;    // sólo tiene sentido si existe ruta
    private final int nodosFijados;
    private final int relajaciones;

    public ResultadoRuta(Ubicacion origen, Ubicacion destino, Ubicacion[] camino,
                         int tiempoTotal, int nodosFijados, int relajaciones) {
        this.origen = origen;
        this.destino = destino;
        this.camino = camino;
        this.tiempoTotal = tiempoTotal;
        this.nodosFijados = nodosFijados;
        this.relajaciones = relajaciones;
    }

    public boolean isExiste() { return camino != null; }
    public Ubicacion[] getCamino() { return camino; }
    public int getTiempoTotal() { return tiempoTotal; }
    public int getNodosFijados() { return nodosFijados; }
    public int getRelajaciones() { return relajaciones; }

    @Override
    public String toString() {
        if (!isExiste()) {
            return "No existe una ruta disponible de " + origen + " a " + destino
                    + " (" + nodosFijados + " nodos fijados, " + relajaciones + " relajaciones)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Ruta más rápida de ").append(origen).append(" a ").append(destino)
                .append(": ").append(tiempoTotal).append(" min\n");
        sb.append("  Recorrido: ");
        for (int i = 0; i < camino.length; i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(camino[i]);
        }
        sb.append("\n  (").append(nodosFijados).append(" nodos fijados, ")
                .append(relajaciones).append(" relajaciones)");
        return sb.toString();
    }
}