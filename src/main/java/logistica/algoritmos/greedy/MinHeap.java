package logistica.algoritmos.greedy;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class MinHeap {

    public static final class Entrada {
        public final int nodo;
        public final int clave;

        Entrada(int nodo, int clave) {
            this.nodo = nodo;
            this.clave = clave;
        }
    }

    private int[] nodos;
    private int[] claves;
    private int tamanio = 0;

    public MinHeap(int capacidadInicial) {
        int capacidad = Math.max(capacidadInicial, 4);
        nodos = new int[capacidad];
        claves = new int[capacidad];
    }

    public boolean estaVacia() { return tamanio == 0; }
    public int tamanio() { return tamanio; }

    public void insertar(int nodo, int clave) {
        if (tamanio == nodos.length) {
            nodos = Arrays.copyOf(nodos, tamanio * 2);
            claves = Arrays.copyOf(claves, tamanio * 2);
        }
        nodos[tamanio] = nodo;
        claves[tamanio] = clave;
        subir(tamanio);
        tamanio++;
    }

    public Entrada extraerMin() {
        if (tamanio == 0) {
            throw new NoSuchElementException("El heap está vacío");
        }
        Entrada min = new Entrada(nodos[0], claves[0]);
        tamanio--;
        if (tamanio > 0) {
            nodos[0] = nodos[tamanio];
            claves[0] = claves[tamanio];
            bajar(0);
        }
        return min;
    }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (!menor(i, padre)) break;
            intercambiar(i, padre);
            i = padre;
        }
    }

    private void bajar(int i) {
        while (true) {
            int izq = 2 * i + 1;
            int der = izq + 1;
            int menor = i;
            if (izq < tamanio && menor(izq, menor)) menor = izq;
            if (der < tamanio && menor(der, menor)) menor = der;
            if (menor == i) break;
            intercambiar(i, menor);
            i = menor;
        }
    }

    private boolean menor(int i, int j) {
        if (claves[i] != claves[j]) return claves[i] < claves[j];
        return nodos[i] < nodos[j];
    }

    private void intercambiar(int i, int j) {
        int n = nodos[i]; nodos[i] = nodos[j]; nodos[j] = n;
        int c = claves[i]; claves[i] = claves[j]; claves[j] = c;
    }
}