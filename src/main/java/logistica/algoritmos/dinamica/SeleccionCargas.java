package logistica.algoritmos.dinamica;

import logistica.modelo.Lote;
import logistica.resultado.ResultadoCarga;

public class SeleccionCargas {

    public static ResultadoCarga seleccionar(Lote[] lotes, int capacidad) {
        return seleccionar(lotes, capacidad, false);
    }

    public static ResultadoCarga seleccionar(Lote[] lotes, int capacidad, boolean conTraza) {
        if (lotes == null) {
            throw new IllegalArgumentException("El arreglo de lotes no puede ser null");
        }
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa: " + capacidad);
        }

        int n = lotes.length;
        // M[i][j] = mejor prioridad usando los lotes 0..i con capacidad j.
    
        int[][] M = new int[n][capacidad + 1];

        for (int i = 0; i < n; i++) {
            int peso = lotes[i].getPeso();
            int valor = lotes[i].getPrioridad();
            for (int j = 0; j <= capacidad; j++) {
                if (i == 0 && peso > j) {
                    M[i][j] = 0;                 
                } else if (i == 0) {
                    M[i][j] = valor;           
                } else if (peso > j) {
                    M[i][j] = M[i - 1][j];      
                } else {
                    
                    M[i][j] = Math.max(M[i - 1][j], M[i - 1][j - peso] + valor);
                }
            }
        }
        long celdas = (long) n * (capacidad + 1);

        if (n == 0) {
            return new ResultadoCarga(new Lote[0], capacidad, 0, 0, celdas);
        }

        // Reconstrucción: desde M[n-1][capacidad] hacia la primera fila 
        boolean[] incluido = new boolean[n];
        int cantidad = 0;
        int j = capacidad;
        for (int i = n - 1; i >= 1; i--) {
            boolean entra = M[i][j] != M[i - 1][j];
            // El texto sólo se arma si hay traza, para no gastar tiempo en el cálculo normal.
            String comparacion = conTraza ? "Lote #" + lotes[i].getId() + ": M[" + i + "][" + j + "]=" + M[i][j]
                    + (entra ? " != " : " == ") + "M[" + (i - 1) + "][" + j + "]=" + M[i - 1][j] : null;
            if (entra) {
                incluido[i] = true;
                cantidad++;
                j -= lotes[i].getPeso();
            }
            if (conTraza) {
                traza(conTraza, comparacion + decision(entra, lotes[i], j));
            }
        }
        // Primera fila
        boolean entraPrimero = M[0][j] > 0;
        String comparacionPrimero = conTraza ? "Lote #" + lotes[0].getId() + ": M[0][" + j + "]=" + M[0][j]
                + (entraPrimero ? " > 0" : " = 0") : null;
        if (entraPrimero) {
            incluido[0] = true;
            cantidad++;
            j -= lotes[0].getPeso();
        }
        if (conTraza) {
            traza(conTraza, comparacionPrimero + decision(entraPrimero, lotes[0], j));
        }

       
        Lote[] seleccion = new Lote[cantidad];
        int k = 0;
        int pesoTotal = 0;
        for (int i = 0; i < n; i++) {
            if (incluido[i]) {
                seleccion[k++] = lotes[i];
                pesoTotal += lotes[i].getPeso();
            }
        }
        
        return new ResultadoCarga(seleccion, capacidad, pesoTotal, M[n - 1][capacidad], celdas);
    }

    //Traza para visualizar las decisiones de la reconstrucción

    private static String decision(boolean entra, Lote lote, int capacidadRestante) {
        if (entra) {
            return " -> se incluye (peso " + lote.getPeso() + "), capacidad restante " + capacidadRestante;
        }
        return " -> no se incluye";
    }

    private static void traza(boolean activa, String mensaje) {
        if (activa) {
            System.out.println("  " + mensaje);
        }
    }
}
