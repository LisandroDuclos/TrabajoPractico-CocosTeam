package logistica.modelo;

public class Ubicacion {

    public enum Tipo { DEPOSITO, CENTRO }

    private final int id;
    private final String nombre;
    private final Tipo tipo;

    public Ubicacion(int id, String nombre, Tipo tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public Tipo getTipo() { return tipo; }

    @Override
    public String toString() {
        return nombre;
    }
}