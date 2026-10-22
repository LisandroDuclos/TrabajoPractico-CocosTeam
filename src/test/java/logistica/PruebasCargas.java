package logistica;

import java.util.Random;

import logistica.algoritmos.dinamica.SeleccionCargas;
import logistica.modelo.Lote;
import logistica.resultado.ResultadoCarga;

/**
 * Casos de prueba de la funcionalidad 3 (selección de cargas con Programación Dinámica).
 */
public class PruebasCargas {

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas: casos borde ===");
        pruebasBorde();
        System.out.println("=== Pruebas: optimalidad ===");
        pruebasOptimalidad();
        System.out.println("=== Pruebas: validaciones ===");
        pruebasValidaciones();
        System.out.println("=== Pruebas: contra fuerza bruta ===");
        pruebasFuerzaBruta();
        System.out.println("=== Prueba de volumen ===");
        pruebaVolumen();

        System.out.println();
        System.out.println("Resultado: " + ok + "/" + (ok + fallas) + " OK");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    //  Casos borde

    private static void pruebasBorde() {
        ResultadoCarga vacio = SeleccionCargas.seleccionar(new Lote[0], 10);
        verificar("arreglo vacío: no selecciona nada", vacio.isVacia() && vacio.getPrioridadTotal() == 0
                && vacio.getCapacidadRestante() == 10 && vacio.getCeldasCalculadas() == 0);

        Lote[] lotes = { new Lote(1, 3, 10), new Lote(2, 4, 20), new Lote(3, 2, 5) };
        ResultadoCarga sinCapacidad = SeleccionCargas.seleccionar(lotes, 0);
        verificar("capacidad 0: no selecciona nada", sinCapacidad.isVacia() && sinCapacidad.getPrioridadTotal() == 0);

        ResultadoCarga todos = SeleccionCargas.seleccionar(lotes, 100);
        verificar("todos los lotes entran", ids(todos.getSeleccion(), 1, 2, 3)
                && todos.getPesoTotal() == 9 && todos.getPrioridadTotal() == 35 && todos.getCapacidadRestante() == 91);

        Lote[] pesados = { new Lote(1, 11, 10), new Lote(2, 20, 20), new Lote(3, 15, 5) };
        ResultadoCarga ninguno = SeleccionCargas.seleccionar(pesados, 10);
        verificar("ningún lote entra", ninguno.isVacia() && ninguno.getPesoTotal() == 0
                && ninguno.getCapacidadRestante() == 10);

        Lote[] uno = { new Lote(7, 5, 9) };
        verificar("un solo lote que entra justo", ids(SeleccionCargas.seleccionar(uno, 5).getSeleccion(), 7));
        verificar("un solo lote que no entra", SeleccionCargas.seleccionar(uno, 4).isVacia());

        verificar("celdas calculadas = n * (W + 1)",
                SeleccionCargas.seleccionar(lotes, 20).getCeldasCalculadas() == 3 * 21);
    }

    //  Optimalidad

    private static void pruebasOptimalidad() {
        // Llenado exacto: 4 + 6 = 10 da 50, mejor que cualquier otra combinación.
        Lote[] exacto = { new Lote(1, 4, 20), new Lote(2, 5, 22), new Lote(3, 6, 30), new Lote(4, 7, 28) };
        ResultadoCarga r = SeleccionCargas.seleccionar(exacto, 10);
        verificar("llenado exacto de la capacidad", ids(r.getSeleccion(), 1, 3)
                && r.getPesoTotal() == 10 && r.getCapacidadRestante() == 0 && r.getPrioridadTotal() == 50);

        // Greedy por prioridad/peso toma 1 y 2 (160); el óptimo es 2 y 3 (220).
        Lote[] contraejemplo = { new Lote(1, 10, 60), new Lote(2, 20, 100), new Lote(3, 30, 120) };
        r = SeleccionCargas.seleccionar(contraejemplo, 50);
        verificar("contraejemplo de Greedy: óptimo 220 con lotes 2 y 3",
                ids(r.getSeleccion(), 2, 3) && r.getPrioridadTotal() == 220 && r.getPesoTotal() == 50);

        // Lotes con prioridad 0: no cambian el óptimo y no se seleccionan.
        Lote[] conCeros = { new Lote(9, 1, 0), new Lote(1, 10, 60), new Lote(8, 2, 0),
                            new Lote(2, 20, 100), new Lote(3, 30, 120), new Lote(7, 5, 0) };
        r = SeleccionCargas.seleccionar(conCeros, 50);
        verificar("lotes con prioridad 0 no alteran el óptimo", r.getPrioridadTotal() == 220);
        verificar("lotes con prioridad 0 no se seleccionan", ids(r.getSeleccion(), 2, 3));

        // Ejemplo de la clase: pesos (1, 4, 5, 7), valores (10, 3, 5, 8), capacidad 8 -> 18.
        Lote[] clase = { new Lote(1, 1, 10), new Lote(2, 4, 3), new Lote(3, 5, 5), new Lote(4, 7, 8) };
        r = SeleccionCargas.seleccionar(clase, 8);
        verificar("ejemplo de la clase: valor 18 con objetos 1 y 4",
                r.getPrioridadTotal() == 18 && ids(r.getSeleccion(), 1, 4));
    }

    //  Validaciones (sin lambdas: cada caso con su try/catch)

    private static void pruebasValidaciones() {
        verificar("lote con peso 0 lanza excepción", loteInvalidoLanza(0, 10));
        verificar("lote con peso negativo lanza excepción", loteInvalidoLanza(-3, 10));
        verificar("lote con prioridad negativa lanza excepción", loteInvalidoLanza(5, -1));
        verificar("lote con prioridad 0 es válido", !loteInvalidoLanza(5, 0));
        verificar("capacidad negativa lanza excepción", seleccionInvalidaLanza(new Lote[0], -1));
        verificar("arreglo de lotes null lanza excepción", seleccionInvalidaLanza(null, 10));
    }

    //  Fuerza bruta

    private static void pruebasFuerzaBruta() {
        Random random = new Random(42);
        int casos = 200;
        boolean igualOptimo = true;
        boolean respetaCapacidad = true;
        boolean sumaCoincide = true;
        for (int c = 0; c < casos; c++) {
            int n = random.nextInt(16); // de 0 a 15 lotes
            Lote[] lotes = new Lote[n];
            for (int i = 0; i < n; i++) {
                lotes[i] = new Lote(i + 1, 1 + random.nextInt(30), random.nextInt(100));
            }
            int capacidad = random.nextInt(120);

            ResultadoCarga r = SeleccionCargas.seleccionar(lotes, capacidad);
            int peso = 0;
            int suma = 0;
            for (Lote lote : r.getSeleccion()) {
                peso += lote.getPeso();
                suma += lote.getPrioridad();
            }
            if (r.getPrioridadTotal() != fuerzaBruta(lotes, 0, capacidad)) igualOptimo = false;
            if (peso > capacidad || peso != r.getPesoTotal()) respetaCapacidad = false;
            if (suma != r.getPrioridadTotal()) sumaCoincide = false;
        }
        verificar(casos + " casos aleatorios: misma prioridad que la fuerza bruta", igualOptimo);
        verificar(casos + " casos aleatorios: la selección nunca supera la capacidad", respetaCapacidad);
        verificar(casos + " casos aleatorios: suma de prioridades = valor de la tabla", sumaCoincide);
    }

    /** Prueba todos los subconjuntos: para cada lote, sin llevarlo y llevándolo. O(2^n). */
    private static int fuerzaBruta(Lote[] lotes, int i, int capacidad) {
        if (i == lotes.length) {
            return 0;
        }
        int sinLote = fuerzaBruta(lotes, i + 1, capacidad);
        if (lotes[i].getPeso() > capacidad) {
            return sinLote;
        }
        int conLote = lotes[i].getPrioridad() + fuerzaBruta(lotes, i + 1, capacidad - lotes[i].getPeso());
        return Math.max(sinLote, conLote);
    }

    //  Volumen

    private static void pruebaVolumen() {
        int n = 1000;
        int capacidad = 10_000;
        Random random = new Random(42);
        Lote[] lotes = new Lote[n];
        for (int i = 0; i < n; i++) {
            lotes[i] = new Lote(i + 1, 1 + random.nextInt(100), random.nextInt(500));
        }

        long inicio = System.nanoTime();
        ResultadoCarga r = SeleccionCargas.seleccionar(lotes, capacidad);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        verificar("1.000 lotes y capacidad 10.000: no supera la capacidad", r.getPesoTotal() <= capacidad);
        verificar("1.000 lotes: celdas calculadas = n * (W + 1)", r.getCeldasCalculadas() == (long) n * (capacidad + 1));
        System.out.println("    n = " + n + ", W = " + capacidad + ", tiempo = " + ms + " ms, celdas = "
                + r.getCeldasCalculadas() + ", lotes elegidos = " + r.getSeleccion().length
                + ", prioridad = " + r.getPrioridadTotal());
    }

    // ---- Auxiliares ----

    private static boolean loteInvalidoLanza(int peso, int prioridad) {
        try {
            new Lote(1, peso, prioridad);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean seleccionInvalidaLanza(Lote[] lotes, int capacidad) {
        try {
            SeleccionCargas.seleccionar(lotes, capacidad);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean ids(Lote[] lotes, int... esperados) {
        if (lotes.length != esperados.length) return false;
        for (int i = 0; i < lotes.length; i++) {
            if (lotes[i].getId() != esperados[i]) return false;
        }
        return true;
    }

    private static void verificar(String nombre, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("  [OK]    " + nombre);
        } else {
            fallas++;
            System.out.println("  [FALLA] " + nombre);
        }
    }
}
