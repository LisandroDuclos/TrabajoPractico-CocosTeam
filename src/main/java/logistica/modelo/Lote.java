package logistica.modelo;

public class Lote {

    private final int id;
    private final int peso;      // en kg
    private final int prioridad; // nunca negativo

    public Lote(int id, int peso, int prioridad) {
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso del lote debe ser positivo: " + peso);
        }
        if (prioridad < 0) {
            throw new IllegalArgumentException("La prioridad del lote no puede ser negativa: " + prioridad);
        }
        this.id = id;
        this.peso = peso;
        this.prioridad = prioridad;
    }

    public int getId() { return id; }
    public int getPeso() { return peso; }
    public int getPrioridad() { return prioridad; }

    @Override
    public String toString() {
        return String.format("#%-4d peso %4d kg  prioridad %d", id, peso, prioridad);
    }
}
