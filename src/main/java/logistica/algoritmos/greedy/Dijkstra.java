package logistica.algoritmos.greedy;

import logistica.modelo.Conexion;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.resultado.ResultadoRuta;

public class Dijkstra {

    public static final int INFINITO = Integer.MAX_VALUE;

    public static ResultadoRuta calcular(RedVial red, String origen, String destino) {
        return calcular(red, origen, destino, false);
    }

    public static ResultadoRuta calcular(RedVial red, String origen, String destino, boolean conTraza) {
        return calcular(red, red.getId(origen), red.getId(destino), conTraza);
    }

    public static ResultadoRuta calcular(RedVial red, int origen, int destino, boolean conTraza) {
        Ubicacion uOrigen = red.getUbicacion(origen);   // validacion de los id
        Ubicacion uDestino = red.getUbicacion(destino);

        int n = red.cantidadUbicaciones();
        int[] tiempo = new int[n];       // mejor tiempo conocido desde el origen
        int[] previo = new int[n];       // de qué nodo se llega con ese tiempo (para rearmar el camino)
        boolean[] fijado = new boolean[n];
        for (int i = 0; i < n; i++) {
            tiempo[i] = INFINITO;
            previo[i] = -1;
        }

        MinHeap cola = new MinHeap(n);
        int nodosFijados = 0;
        int relajaciones = 0;

        tiempo[origen] = 0;
        cola.insertar(origen, 0);
        traza(conTraza, "Inicio: de " + uOrigen + " a " + uDestino);

        while (!cola.estaVacia()) {
            MinHeap.Entrada e = cola.extraerMin();
            int u = e.nodo;

            if (fijado[u]) {
                traza(conTraza, "  Descarto entrada vieja de " + red.getUbicacion(u) + " (" + e.clave + " min)");
                continue;
            }
            fijado[u] = true;
            nodosFijados++;
            traza(conTraza, "Fijo " + red.getUbicacion(u) + " con " + tiempo[u] + " min");

            if (u == destino) {
                traza(conTraza, "  Es el destino: se corta el cálculo");
                break;
            }

            for (Conexion c : red.getConexionesSalientes(u)) {
                int v = c.getDestino();
                // El texto sólo se arma si hay traza, para no gastar tiempo en el cálculo normal.
                String arista = conTraza ? red.getUbicacion(u) + " -> " + red.getUbicacion(v) : null;

                if (!c.isHabilitada()) {
                    traza(conTraza, "  " + arista + ": deshabilitada, se ignora");
                    continue;
                }
                if (fijado[v]) {
                    continue; // aca ya tiene su mejor tiempo definitivo
                }
                int candidato = tiempo[u] + c.getTiempo();
                if (candidato < tiempo[v]) {
                    String antes = (tiempo[v] == INFINITO) ? "sin camino" : tiempo[v] + " min";
                    tiempo[v] = candidato;
                    previo[v] = u;
                    cola.insertar(v, candidato);
                    relajaciones++;
                    traza(conTraza, "  " + arista + " (" + c.getTiempo() + " min): "
                            + antes + " -> " + candidato + " min, mejora");
                } else {
                    traza(conTraza, "  " + arista + " (" + c.getTiempo() + " min): "
                            + candidato + " min, no mejora los " + tiempo[v] + " min actuales");
                }
            }
        }

        // La cola se vació (o se cortó) sin que el destino tenga tiempo: no hay ruta disponible.
        if (tiempo[destino] == INFINITO) {
            traza(conTraza, "No quedan candidatos: " + uDestino + " es inalcanzable");
            return new ResultadoRuta(uOrigen, uDestino, null, 0, nodosFijados, relajaciones);
        }

        // Reconstrucción del camino desde el destino hacia el origen.
        int largo = 1;
        for (int v = destino; v != origen; v = previo[v]) {
            largo++;
        }
        Ubicacion[] camino = new Ubicacion[largo];
        int v = destino;
        for (int i = largo - 1; i >= 0; i--) {
            camino[i] = red.getUbicacion(v);
            v = previo[v];
        }
        return new ResultadoRuta(uOrigen, uDestino, camino, tiempo[destino], nodosFijados, relajaciones);
    }

    //Traza para visualizar las decisiones

    private static void traza(boolean activa, String mensaje) {
        if (activa) {
            System.out.println("  " + mensaje);
        }
    }
}