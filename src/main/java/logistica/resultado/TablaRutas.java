package logistica.resultado;

import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;

// Resultado de Floyd-Warshall: tiempos mínimos entre todos los pares, listos para consultar
public class TablaRutas {

    public static final int SIN_CONEXION = Integer.MAX_VALUE;

    private final RedVial red;
    private final int versionRed;     // versión de la red con la que se calculó
    private final int[][] tiempos;    // tiempos[i][j]: tiempo mínimo de i a j
    private final int[][] siguiente;  // siguiente[i][j]: primer salto de la ruta de i a j
    private final int mejoras;

    public TablaRutas(RedVial red, int versionRed, int[][] tiempos, int[][] siguiente, int mejoras) {
        this.red = red;
        this.versionRed = versionRed;
        this.tiempos = tiempos;
        this.siguiente = siguiente;
        this.mejoras = mejoras;
    }

    /** False si la red cambió después del cálculo: la tabla ya no representa a la red. */
    public boolean isVigente() { return red.getVersion() == versionRed; }
    public int getMejoras() { return mejoras; }

    public ResultadoConsultaRuta consultar(String origen, String destino) {
        return consultar(red.getId(origen), red.getId(destino));
    }

    /** No repite el cálculo: lee el tiempo y sigue los saltos guardados, O(largo de la ruta). */
    public ResultadoConsultaRuta consultar(int origen, int destino) {
        if (!isVigente()) {
            throw new IllegalStateException("La red cambió: hay que volver a calcular la tabla de rutas");
        }
        Ubicacion uOrigen = red.getUbicacion(origen);
        Ubicacion uDestino = red.getUbicacion(destino);
        if (tiempos[origen][destino] == SIN_CONEXION) {
            return new ResultadoConsultaRuta(uOrigen, uDestino, null, 0);
        }

        int largo = 1;
        for (int u = origen; u != destino; u = siguiente[u][destino]) {
            largo++;
        }
        Ubicacion[] camino = new Ubicacion[largo];
        int u = origen;
        for (int i = 0; i < largo; i++) {
            camino[i] = red.getUbicacion(u);
            u = siguiente[u][destino];
        }
        return new ResultadoConsultaRuta(uOrigen, uDestino, camino, tiempos[origen][destino]);
    }

    @Override
    public String toString() {
        return formatear(red, tiempos);
    }

    /** Matriz de tiempos en minutos; "-" indica que no hay conexión. */
    public static String formatear(RedVial red, int[][] tiempos) {
        int n = tiempos.length;
        StringBuilder sb = new StringBuilder(String.format("  %-20s", ""));
        for (int j = 0; j < n; j++) {
            sb.append(String.format("%5s", "[" + j + "]"));
        }
        sb.append("\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format("  %-20s", "[" + i + "] " + red.getUbicacion(i)));
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%5s", tiempos[i][j] == SIN_CONEXION ? "-" : tiempos[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
