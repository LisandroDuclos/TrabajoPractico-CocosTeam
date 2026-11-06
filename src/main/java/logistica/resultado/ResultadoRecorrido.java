package logistica.resultado;

import logistica.modelo.Ubicacion;

// Resultado de B1
public class ResultadoRecorrido {

    private final Ubicacion deposito;
    private final Ubicacion[] centros;           // centros seleccionados, en el orden en que se pidieron
    private final Ubicacion[] paradas;           // depósito, centros en orden de visita, depósito; null si no existe
    private final ResultadoConsultaRuta[] tramos; // trayecto entre cada par de paradas seguidas; null si no existe
    private final int tiempoTotal;               // sólo tiene sentido si existe el recorrido
    private final int nodosExplorados;
    private final int recorridosCompletos;
    private final int podasPorCota;
    private final int podasSinConexion;

    public ResultadoRecorrido(Ubicacion deposito, Ubicacion[] centros, Ubicacion[] paradas,
                              ResultadoConsultaRuta[] tramos, int tiempoTotal, int nodosExplorados,
                              int recorridosCompletos, int podasPorCota, int podasSinConexion) {
        this.deposito = deposito;
        this.centros = centros;
        this.paradas = paradas;
        this.tramos = tramos;
        this.tiempoTotal = tiempoTotal;
        this.nodosExplorados = nodosExplorados;
        this.recorridosCompletos = recorridosCompletos;
        this.podasPorCota = podasPorCota;
        this.podasSinConexion = podasSinConexion;
    }

    public boolean isExiste() { return paradas != null; }
    public Ubicacion getDeposito() { return deposito; }
    public Ubicacion[] getCentros() { return centros; }
    public Ubicacion[] getParadas() { return paradas; }
    public ResultadoConsultaRuta[] getTramos() { return tramos; }
    public int getTiempoTotal() { return tiempoTotal; }
    public int getNodosExplorados() { return nodosExplorados; }
    public int getRecorridosCompletos() { return recorridosCompletos; }
    public int getPodasPorCota() { return podasPorCota; }
    public int getPodasSinConexion() { return podasSinConexion; }

    //Todas las ubicaciones que atraviesa el vehículo, incluidas las intermedias de cada tramo.
    public Ubicacion[] getCamino() {
        if (!isExiste()) {
            return null;
        }
        int largo = 1;
        for (ResultadoConsultaRuta tramo : tramos) {
            largo += tramo.getCamino().length - 1; // el inicio de un tramo es el final del anterior
        }
        Ubicacion[] camino = new Ubicacion[largo];
        camino[0] = deposito;
        int pos = 1;
        for (ResultadoConsultaRuta tramo : tramos) {
            Ubicacion[] c = tramo.getCamino();
            for (int i = 1; i < c.length; i++) {
                camino[pos++] = c[i];
            }
        }
        return camino;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Recorrido desde ").append(deposito).append(" por ").append(centros.length).append(" centros: ");
        for (int i = 0; i < centros.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(centros[i]);
        }
        sb.append("\n");
        if (!isExiste()) {
            sb.append("  No existe un recorrido que visite todos los centros y vuelva al depósito\n");
        } else {
            sb.append("  Orden de visita: ");
            for (int i = 0; i < paradas.length; i++) {
                if (i > 0) sb.append(" -> ");
                sb.append(paradas[i]);
            }
            sb.append(" (").append(tiempoTotal).append(" min)\n");
            sb.append("  Tramos:\n");
            for (int i = 0; i < tramos.length; i++) {
                sb.append("    ").append(i + 1).append(") ").append(tramos[i]).append("\n");
            }
        }
        sb.append("  (").append(nodosExplorados).append(" nodos explorados, ")
                .append(recorridosCompletos).append(" recorridos completos, podas: ")
                .append(podasPorCota).append(" por cota, ")
                .append(podasSinConexion).append(" sin conexión)");
        return sb.toString();
    }
}
