package logistica.modelo;

public class Ubicacion {

    public enum Tipo { DEPOSITO, CENTRO }

    private final int id;
    private final String nombre;

    public Ubicacion(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}