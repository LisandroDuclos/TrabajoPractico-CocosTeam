package logistica.modelo;

/**
 * Vehículo que sale de un depósito con una capacidad máxima de carga.
 */
public class Vehiculo {

    private final int id;
    private final int capacidad;      // en kg
    private final String deposito;    // ubicación de salida en la red

    public Vehiculo(int id, int capacidad, String deposito) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad del vehículo debe ser positiva: " + capacidad);
        }
        if (deposito == null || deposito.isEmpty()) {
            throw new IllegalArgumentException("El vehículo debe tener un depósito de salida");
        }
        this.id = id;
        this.capacidad = capacidad;
        this.deposito = deposito;
    }

    public int getId() { return id; }
    public int getCapacidad() { return capacidad; }
    public String getDeposito() { return deposito; }

    @Override
    public String toString() {
        return String.format("V%d capacidad %d kg  sale de %s", id, capacidad, deposito);
    }
}
