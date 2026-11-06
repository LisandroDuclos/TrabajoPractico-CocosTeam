package logistica.algoritmos.backtracking;

import java.util.ArrayList;
import java.util.List;

import logistica.modelo.Conexion;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.resultado.ResultadoRutasAlternativas;
import logistica.resultado.ResultadoRutasAlternativas.Ruta;

public class RutasAlternativas {

    private final RedVial red;
    private final int destino;
    private final int tiempoMaximo;
    private final boolean[] prohibida;
    private final boolean conTraza;

    private final boolean[] enRuta;     // ubicaciones de la ruta que se está armando
    private final int[] ruta;           // ruta parcial
    private final List<Ruta> encontradas = new ArrayList<>();
    private int nodosExplorados = 0;
    private int podasPorTiempo = 0;
    private int podasPorProhibida = 0;
    private int podasPorRepetida = 0;

    private RutasAlternativas(RedVial red, int destino, int tiempoMaximo, boolean[] prohibida, boolean conTraza) {
        this.red = red;
        this.destino = destino;
        this.tiempoMaximo = tiempoMaximo;
        this.prohibida = prohibida;
        this.conTraza = conTraza;
        this.enRuta = new boolean[red.cantidadUbicaciones()];
        this.ruta = new int[red.cantidadUbicaciones()];
    }

    public static ResultadoRutasAlternativas buscar(RedVial red, String origen, String destino,
                                                    int tiempoMaximo, String[] prohibidas) {
        return buscar(red, origen, destino, tiempoMaximo, prohibidas, false);
    }

    public static ResultadoRutasAlternativas buscar(RedVial red, String origen, String destino,
                                                    int tiempoMaximo, String[] prohibidas, boolean conTraza) {
        if (red == null) {
            throw new IllegalArgumentException("La red vial no puede ser null");
        }
        if (prohibidas == null) {
            throw new IllegalArgumentException("El conjunto de ubicaciones prohibidas no puede ser null (puede ser vacío)");
        }
        if (tiempoMaximo < 0) {
            throw new IllegalArgumentException("El tiempo máximo no puede ser negativo: " + tiempoMaximo);
        }
        int idOrigen = red.getId(origen);     // lanzan IllegalArgumentException si no existen
        int idDestino = red.getId(destino);
        boolean[] marcadas = new boolean[red.cantidadUbicaciones()];
        Ubicacion[] uProhibidas = new Ubicacion[prohibidas.length];
        for (int i = 0; i < prohibidas.length; i++) {
            int id = red.getId(prohibidas[i]);
            marcadas[id] = true;
            uProhibidas[i] = red.getUbicacion(id);
        }

        RutasAlternativas busqueda = new RutasAlternativas(red, idDestino, tiempoMaximo, marcadas, conTraza);
        if (marcadas[idOrigen] || marcadas[idDestino]) {
            busqueda.traza(0, "El origen o el destino está prohibido: no hay ninguna ruta posible");
        } else {
            busqueda.traza(0, "Inicio: de " + red.getUbicacion(idOrigen) + " a " + red.getUbicacion(idDestino)
                    + ", máximo " + tiempoMaximo + " min");
            busqueda.enRuta[idOrigen] = true;
            busqueda.ruta[0] = idOrigen;
            busqueda.explorar(idOrigen, 1, 0);
        }
        return busqueda.resultado(red.getUbicacion(idOrigen), red.getUbicacion(idDestino), uProhibidas);
    }

    /** Estado: la ruta parcial ruta[0..largo-1] termina en 'ultima' con 'tiempo' minutos acumulados. */
    private void explorar(int ultima, int largo, int tiempo) {
        nodosExplorados++;
        if (ultima == destino) {
            guardarRuta(largo, tiempo);
            return;
        }
        for (Conexion c : red.getConexionesSalientes(ultima)) {
            int v = c.getDestino();
            if (!c.isHabilitada()) {
                continue; // una conexión deshabilitada no existe para la búsqueda
            }
            if (prohibida[v]) {
                podasPorProhibida++;
                traza(largo, red.getUbicacion(ultima) + " -> " + red.getUbicacion(v) + ": prohibida, se poda");
                continue;
            }
            if (enRuta[v]) {
                podasPorRepetida++;
                traza(largo, red.getUbicacion(ultima) + " -> " + red.getUbicacion(v) + ": ya está en la ruta, se poda");
                continue;
            }
            int nuevo = tiempo + c.getTiempo();
            if (nuevo > tiempoMaximo) {
                podasPorTiempo++;
                traza(largo, red.getUbicacion(ultima) + " -> " + red.getUbicacion(v) + ": " + nuevo
                        + " min supera el máximo, se poda");
                continue;
            }
            // Elegir, explorar y deshacer
            enRuta[v] = true;
            ruta[largo] = v;
            traza(largo, "Avanzo a " + red.getUbicacion(v) + " (" + nuevo + " min)");
            explorar(v, largo + 1, nuevo);
            enRuta[v] = false;
        }
    }

    private void guardarRuta(int largo, int tiempo) {
        Ubicacion[] camino = new Ubicacion[largo];
        for (int i = 0; i < largo; i++) {
            camino[i] = red.getUbicacion(ruta[i]);
        }
        encontradas.add(new Ruta(camino, tiempo));
        traza(largo, "Destino alcanzado: ruta válida de " + tiempo + " min");
    }

    private ResultadoRutasAlternativas resultado(Ubicacion origen, Ubicacion destino, Ubicacion[] prohibidas) {
        Ruta[] rutas = encontradas.toArray(new Ruta[0]);
        int mejor = -1;
        for (int i = 0; i < rutas.length; i++) {
            if (mejor < 0 || rutas[i].getTiempoTotal() < rutas[mejor].getTiempoTotal()) {
                mejor = i;
            }
        }
        return new ResultadoRutasAlternativas(origen, destino, tiempoMaximo, prohibidas, rutas, mejor,
                nodosExplorados, podasPorTiempo, podasPorProhibida, podasPorRepetida);
    }

    private void traza(int nivel, String texto) {
        if (conTraza) {
            System.out.println("  ".repeat(nivel) + texto);
        }
    }
}
