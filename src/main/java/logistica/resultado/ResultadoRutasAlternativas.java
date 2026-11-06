package logistica.resultado;

import logistica.modelo.Ubicacion;

// Resultado de B3: todas las rutas válidas entre un origen y un destino, con los datos de la búsqueda
public class ResultadoRutasAlternativas {

    /** Una ruta válida: las ubicaciones que atraviesa (sin repetir) y su tiempo total. */
    public static class Ruta {
        private final Ubicacion[] camino;
        private final int tiempoTotal;

        public Ruta(Ubicacion[] camino, int tiempoTotal) {
            this.camino = camino;
            this.tiempoTotal = tiempoTotal;
        }

        public Ubicacion[] getCamino() { return camino; }
        public int getTiempoTotal() { return tiempoTotal; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < camino.length; i++) {
                if (i > 0) sb.append(" -> ");
                sb.append(camino[i]);
            }
            return sb.append(": ").append(tiempoTotal).append(" min").toString();
        }
    }

    private final Ubicacion origen;
    private final Ubicacion destino;
    private final int tiempoMaximo;
    private final Ubicacion[] prohibidas;
    private final Ruta[] rutas;          // en el orden en que las encontró la búsqueda
    private final int indiceMejor;       // posición de la ruta de menor tiempo; -1 si no hay rutas
    private final int nodosExplorados;
    private final int podasPorTiempo;
    private final int podasPorProhibida;
    private final int podasPorRepetida;

    public ResultadoRutasAlternativas(Ubicacion origen, Ubicacion destino, int tiempoMaximo,
                                      Ubicacion[] prohibidas, Ruta[] rutas, int indiceMejor,
                                      int nodosExplorados, int podasPorTiempo,
                                      int podasPorProhibida, int podasPorRepetida) {
        this.origen = origen;
        this.destino = destino;
        this.tiempoMaximo = tiempoMaximo;
        this.prohibidas = prohibidas;
        this.rutas = rutas;
        this.indiceMejor = indiceMejor;
        this.nodosExplorados = nodosExplorados;
        this.podasPorTiempo = podasPorTiempo;
        this.podasPorProhibida = podasPorProhibida;
        this.podasPorRepetida = podasPorRepetida;
    }

    public boolean isExiste() { return rutas.length > 0; }
    public int getCantidadRutas() { return rutas.length; }
    public Ruta[] getRutas() { return rutas; }
    public Ruta getMejor() { return indiceMejor < 0 ? null : rutas[indiceMejor]; }
    public int getNodosExplorados() { return nodosExplorados; }
    public int getPodasPorTiempo() { return podasPorTiempo; }
    public int getPodasPorProhibida() { return podasPorProhibida; }
    public int getPodasPorRepetida() { return podasPorRepetida; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rutas de ").append(origen).append(" a ").append(destino)
                .append(" con tiempo máximo ").append(tiempoMaximo).append(" min");
        if (prohibidas.length > 0) {
            sb.append(", sin pasar por ");
            for (int i = 0; i < prohibidas.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(prohibidas[i]);
            }
        }
        sb.append("\n");
        if (!isExiste()) {
            sb.append("  No existe ninguna ruta que cumpla las condiciones\n");
        } else {
            for (int i = 0; i < rutas.length; i++) {
                sb.append("  ").append(i + 1).append(") ").append(rutas[i]);
                if (i == indiceMejor) sb.append("   <- la de menor tiempo");
                sb.append("\n");
            }
        }
        sb.append("  (").append(rutas.length).append(" rutas, ").append(nodosExplorados)
                .append(" nodos explorados, podas: ").append(podasPorTiempo).append(" por tiempo, ")
                .append(podasPorProhibida).append(" por ubicación prohibida, ")
                .append(podasPorRepetida).append(" por ubicación repetida)");
        return sb.toString();
    }
}
