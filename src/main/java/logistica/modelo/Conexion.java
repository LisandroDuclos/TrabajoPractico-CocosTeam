package logistica.modelo;

public class Conexion {

    private final int destino;
    private final int tiempo;
    private boolean habilitada = true;

    public Conexion(int origen, int destino, int tiempo) {
        if (tiempo <= 0) {
            throw new IllegalArgumentException("El tiempo de viaje debe ser positivo: " + tiempo);
        }
        this.destino = destino;
        this.tiempo = tiempo;
    }

    public int getDestino() {
        return destino;
    }

    public int getTiempo() {
        return tiempo;
    }

    public boolean isHabilitada() {
        return habilitada;
    }

    public void setHabilitada(boolean habilitada) {
        this.habilitada = habilitada;
    }
}