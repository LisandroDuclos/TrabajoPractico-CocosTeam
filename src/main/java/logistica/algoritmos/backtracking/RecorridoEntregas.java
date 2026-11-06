package logistica.algoritmos.backtracking;

import logistica.algoritmos.dinamica.FloydWarshall;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.resultado.ResultadoConsultaRuta;
import logistica.resultado.ResultadoRecorrido;
import logistica.resultado.TablaRutas;

public class RecorridoEntregas {

    public static final int MAXIMO_CENTROS = 8;
    private static final int SIN_CONEXION = TablaRutas.SIN_CONEXION;

    private final RedVial red;
    private final Ubicacion[] paradas;  // paradas[0] es el depósito, paradas[1..k] los centros
    private final int[][] tiempo;       // tiempo mínimo entre paradas (tabla de A1); SIN_CONEXION si no hay
    private final boolean conTraza;

    private final boolean[] visitado;   // centros que ya están en el recorrido parcial
    private final int[] orden;          // recorrido parcial: posiciones de 'paradas', orden[0] = depósito
    private int[] mejorOrden = null;    // mejor recorrido completo encontrado
    private int mejorTiempo = SIN_CONEXION;
    private int nodosExplorados = 0;
    private int recorridosCompletos = 0;
    private int podasPorCota = 0;
    private int podasSinConexion = 0;

    private RecorridoEntregas(RedVial red, Ubicacion[] paradas, int[][] tiempo, boolean conTraza) {
        this.red = red;
        this.paradas = paradas;
        this.tiempo = tiempo;
        this.conTraza = conTraza;
        this.visitado = new boolean[paradas.length];
        this.orden = new int[paradas.length];
    }

    public static ResultadoRecorrido calcular(RedVial red, String deposito, String[] centros) {
        return calcular(red, deposito, centros, false);
    }

    // Calcula primero la tabla de tiempos mínimos (Floyd-Warshall) y después el recorrido.
    public static ResultadoRecorrido calcular(RedVial red, String deposito, String[] centros, boolean conTraza) {
        if (red == null) {
            throw new IllegalArgumentException("La red vial no puede ser null");
        }
        return calcular(red, FloydWarshall.calcular(red), deposito, centros, conTraza);
    }

    //Tabla de tiempos mínimos ya calculada (por ejemplo, la que guarda el sistema para A1).
    public static ResultadoRecorrido calcular(RedVial red, TablaRutas tabla, String deposito,
                                              String[] centros, boolean conTraza) {
        if (red == null) {
            throw new IllegalArgumentException("La red vial no puede ser null");
        }
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla de rutas no puede ser null");
        }
        if (!tabla.isVigente()) {
            throw new IllegalStateException("La red cambió: hay que volver a calcular la tabla de rutas");
        }
        Ubicacion[] paradas = validarParadas(red, deposito, centros);

        // Tiempo mínimo entre cada par de paradas: se lee de la tabla, no se recalcula.
        int n = paradas.length;
        int[][] tiempo = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                ResultadoConsultaRuta tramo = tabla.consultar(paradas[i].getId(), paradas[j].getId());
                tiempo[i][j] = tramo.isExiste() ? tramo.getTiempoTotal() : SIN_CONEXION;
            }
        }

        RecorridoEntregas busqueda = new RecorridoEntregas(red, paradas, tiempo, conTraza);
        busqueda.traza(0, "Inicio: salgo de " + paradas[0] + " y tengo que visitar " + (n - 1) + " centros");
        busqueda.visitado[0] = true;
        busqueda.orden[0] = 0;
        busqueda.explorar(0, 1, 0);
        return busqueda.resultado(tabla, centros);
    }

    // Devuelve las paradas con el depósito en la posición 0. Valida cantidad, existencia y repetidos.
    private static Ubicacion[] validarParadas(RedVial red, String deposito, String[] centros) {
        if (centros == null || centros.length == 0) {
            throw new IllegalArgumentException("Hay que seleccionar al menos un centro de entrega");
        }
        if (centros.length > MAXIMO_CENTROS) {
            throw new IllegalArgumentException("Se admiten como máximo " + MAXIMO_CENTROS
                    + " centros de entrega: se pidieron " + centros.length);
        }
        Ubicacion[] paradas = new Ubicacion[centros.length + 1];
        paradas[0] = red.getUbicacion(red.getId(deposito)); // lanza IllegalArgumentException si no existe
        for (int i = 0; i < centros.length; i++) {
            Ubicacion centro = red.getUbicacion(red.getId(centros[i]));
            if (centro.getId() == paradas[0].getId()) {
                throw new IllegalArgumentException("El depósito no puede ser un centro de entrega: " + centro);
            }
            for (int j = 1; j <= i; j++) {
                if (paradas[j].getId() == centro.getId()) {
                    throw new IllegalArgumentException("El centro está repetido: " + centro);
                }
            }
            paradas[i + 1] = centro;
        }
        return paradas;
    }

    // Estado: el recorrido parcial orden[0..cantidad-1] termina en 'ultima' con 'tiempoAcumulado' minutos.
    private void explorar(int ultima, int cantidad, int tiempoAcumulado) {
        nodosExplorados++;
        if (cantidad == paradas.length) {
            // Caso base: ya se visitaron todos los centros, falta volver al depósito.
            cerrarRecorrido(ultima, cantidad, tiempoAcumulado);
            return;
        }
        for (int p = 1; p < paradas.length; p++) {
            if (visitado[p]) {
                continue; // cada centro aparece una sola vez como parada
            }
            if (tiempo[ultima][p] == SIN_CONEXION) {
                podasSinConexion++;
                traza(cantidad, paradas[ultima] + " -> " + paradas[p] + ": no hay camino, se poda");
                continue;
            }
            int nuevo = tiempoAcumulado + tiempo[ultima][p];
            if (nuevo >= mejorTiempo) {
                // Los tiempos son positivos: seguir sólo puede sumar, así que no puede mejorar al mejor.
                podasPorCota++;
                traza(cantidad, paradas[ultima] + " -> " + paradas[p] + ": " + nuevo
                        + " min no mejora el mejor recorrido (" + mejorTiempo + " min), se poda");
                continue;
            }
            // Elegir, explorar y deshacer
            visitado[p] = true;
            orden[cantidad] = p;
            traza(cantidad, "Visito " + paradas[p] + " (" + nuevo + " min)");
            explorar(p, cantidad + 1, nuevo);
            visitado[p] = false;
        }
    }

    private void cerrarRecorrido(int ultima, int cantidad, int tiempoAcumulado) {
        if (tiempo[ultima][0] == SIN_CONEXION) {
            podasSinConexion++;
            traza(cantidad, paradas[ultima] + " -> " + paradas[0] + ": no hay camino de vuelta, se descarta");
            return;
        }
        int total = tiempoAcumulado + tiempo[ultima][0];
        recorridosCompletos++;
        // Con '<' estricto, ante un empate se conserva el primero encontrado.
        if (total < mejorTiempo) {
            mejorTiempo = total;
            mejorOrden = orden.clone();
            traza(cantidad, "Vuelvo a " + paradas[0] + ": recorrido completo de " + total + " min, es el nuevo mejor");
        } else {
            traza(cantidad, "Vuelvo a " + paradas[0] + ": recorrido completo de " + total + " min, no mejora");
        }
    }

    private ResultadoRecorrido resultado(TablaRutas tabla, String[] centros) {
        Ubicacion[] seleccion = new Ubicacion[centros.length];
        for (int i = 0; i < seleccion.length; i++) {
            seleccion[i] = paradas[i + 1];
        }
        Ubicacion[] enOrden = null;
        ResultadoConsultaRuta[] tramos = null;
        if (mejorOrden != null) {
            // El recorrido empieza y termina en el depósito.
            enOrden = new Ubicacion[paradas.length + 1];
            for (int i = 0; i < paradas.length; i++) {
                enOrden[i] = paradas[mejorOrden[i]];
            }
            enOrden[paradas.length] = paradas[0];
            tramos = new ResultadoConsultaRuta[paradas.length];
            for (int i = 0; i < tramos.length; i++) {
                tramos[i] = tabla.consultar(enOrden[i].getId(), enOrden[i + 1].getId());
            }
        }
        return new ResultadoRecorrido(paradas[0], seleccion, enOrden, tramos,
                mejorOrden == null ? 0 : mejorTiempo,
                nodosExplorados, recorridosCompletos, podasPorCota, podasSinConexion);
    }

    private void traza(int nivel, String texto) {
        if (conTraza) {
            System.out.println("  ".repeat(nivel) + texto);
        }
    }
}
