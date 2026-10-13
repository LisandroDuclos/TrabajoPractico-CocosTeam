package logistica.modelo;

import java.time.LocalDate;

/**
 * Pedido de ayuda destinado a un centro comunitario.
 */
public class Pedido {

    /** Criterios por los que se pueden ordenar los pedidos. */
    public enum Criterio { FECHA, PRIORIDAD, ID }

    private final int id;
    private final String centroDestino;
    private final LocalDate fechaLimite;
    private final int prioridad; // mayor valor = más urgente
    private final int peso;

    public Pedido(int id, String centroDestino, LocalDate fechaLimite, int prioridad, int peso) {
        this.id = id;
        this.centroDestino = centroDestino;
        this.fechaLimite = fechaLimite;
        this.prioridad = prioridad;
        this.peso = peso;
    }

    public int getId() { return id; }
    public String getCentroDestino() { return centroDestino; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public int getPrioridad() { return prioridad; }
    public int getPeso() { return peso; }

    /**
     * Compara este pedido con otro según el criterio indicado.
     * Devuelve un número negativo si este pedido debe ir ANTES que el otro,
     * positivo si debe ir DESPUÉS y 0 si son equivalentes.
     *
     * Reglas de desempate:
     *   FECHA:     fecha límite más cercana; empate -> id menor.
     *   PRIORIDAD: prioridad mayor; empate -> fecha más cercana; empate -> id menor.
     *   ID:        id menor.
     */
    public int compararPor(Criterio criterio, Pedido otro) {
        switch (criterio) {
            case FECHA: {
                int porFecha = this.fechaLimite.compareTo(otro.fechaLimite);
                if (porFecha != 0) return porFecha;
                return Integer.compare(this.id, otro.id);
            }
            case PRIORIDAD: {
                // Descendente: se invierte el orden de los operandos.
                int porPrioridad = Integer.compare(otro.prioridad, this.prioridad);
                if (porPrioridad != 0) return porPrioridad;
                int porFecha = this.fechaLimite.compareTo(otro.fechaLimite);
                if (porFecha != 0) return porFecha;
                return Integer.compare(this.id, otro.id);
            }
            case ID:
            default:
                return Integer.compare(this.id, otro.id);
        }
    }

    @Override
    public String toString() {
        return String.format("#%-4d %-14s límite %s  prioridad %d  peso %4d kg",
                id, centroDestino, fechaLimite, prioridad, peso);
    }
}
