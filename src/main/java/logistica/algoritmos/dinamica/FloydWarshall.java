package logistica.algoritmos.dinamica;

import logistica.modelo.Conexion;
import logistica.modelo.RedVial;
import logistica.resultado.TablaRutas;

public class FloydWarshall {

    private static final int INFINITO = TablaRutas.SIN_CONEXION;

    public static TablaRutas calcular(RedVial red) {
        return calcular(red, false);
    }

    public static TablaRutas calcular(RedVial red, boolean conTraza) {
        int n = red.cantidadUbicaciones();
        // M[i][j] = menor tiempo de i a j usando como intermedios sólo los nodos ya considerados.
        int[][] M = new int[n][n];
        int[][] siguiente = new int[n][n]; // primer salto de la ruta de i a j (para rearmar el camino)

        // M0: sin intermedios, sólo las conexiones directas habilitadas.
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                M[i][j] = (i == j) ? 0 : INFINITO;
                siguiente[i][j] = -1;
            }
            siguiente[i][i] = i;
        }
        for (int i = 0; i < n; i++) {
            for (Conexion c : red.getConexionesSalientes(i)) {
                if (c.isHabilitada() && c.getDestino() != i) {
                    M[i][c.getDestino()] = c.getTiempo();
                    siguiente[i][c.getDestino()] = c.getDestino();
                }
            }
        }
        if (conTraza) {
            System.out.println("  M0, sólo conexiones directas habilitadas:");
            System.out.print(TablaRutas.formatear(red, M));
        }

        // Mk[i][j] = min(Mk-1[i][j], Mk-1[i][k] + Mk-1[k][j]).
        // Alcanza una sola matriz: como M[k][k] = 0, la fila y la columna k no cambian en la etapa k.
        int mejoras = 0;
        for (int k = 0; k < n; k++) {
            int mejorasEtapa = 0;
            for (int i = 0; i < n; i++) {
                if (M[i][k] == INFINITO) {
                    continue; // no se llega a k: sumar desbordaría y no puede mejorar nada
                }
                for (int j = 0; j < n; j++) {
                    if (i == j || M[k][j] == INFINITO) {
                        continue;
                    }
                    int candidato = M[i][k] + M[k][j];
                    if (candidato < M[i][j]) {
                        if (conTraza) {
                            String antes = (M[i][j] == INFINITO) ? "sin conexión" : M[i][j] + " min";
                            System.out.println("  k = " + red.getUbicacion(k) + ": " + red.getUbicacion(i)
                                    + " -> " + red.getUbicacion(j) + ": " + antes + " -> " + candidato + " min");
                        }
                        M[i][j] = candidato;
                        siguiente[i][j] = siguiente[i][k];
                        mejorasEtapa++;
                    }
                }
            }
            if (conTraza && mejorasEtapa == 0) {
                System.out.println("  k = " + red.getUbicacion(k) + ": sin mejoras");
            }
            mejoras += mejorasEtapa;
        }
        return new TablaRutas(red, red.getVersion(), M, siguiente, mejoras);
    }
}
