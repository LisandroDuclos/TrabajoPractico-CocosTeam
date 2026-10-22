package logistica.resultado;

import logistica.modelo.Lote;

public class ResultadoCarga {

    private final Lote[] seleccion;    // vacío si no entra ningún lote
    private final int capacidad;
    private final int pesoTotal;
    private final int prioridadTotal;
    private final long celdasCalculadas; // celdas llenadas de la tabla: n * (capacidad + 1)

    public ResultadoCarga(Lote[] seleccion, int capacidad, int pesoTotal,
                          int prioridadTotal, long celdasCalculadas) {
        this.seleccion = seleccion;
        this.capacidad = capacidad;
        this.pesoTotal = pesoTotal;
        this.prioridadTotal = prioridadTotal;
        this.celdasCalculadas = celdasCalculadas;
    }

    public boolean isVacia() { return seleccion.length == 0; }
    public Lote[] getSeleccion() { return seleccion; }
    public int getCapacidad() { return capacidad; }
    public int getPesoTotal() { return pesoTotal; }
    public int getPrioridadTotal() { return prioridadTotal; }
    public int getCapacidadRestante() { return capacidad - pesoTotal; }
    public long getCeldasCalculadas() { return celdasCalculadas; }

    @Override
    public String toString() {
        if (isVacia()) {
            return "No se selecciona ningún lote: ninguno aporta prioridad dentro de la capacidad de "
                    + capacidad + " kg (" + celdasCalculadas + " celdas calculadas)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Carga seleccionada (").append(seleccion.length).append(seleccion.length == 1 ? " lote" : " lotes")
                .append(") para una capacidad de ")
                .append(capacidad).append(" kg:\n");
        for (Lote lote : seleccion) {
            sb.append("  ").append(lote).append("\n");
        }
        sb.append("  Peso total: ").append(pesoTotal).append(" kg | Prioridad total: ").append(prioridadTotal)
                .append(" | Capacidad restante: ").append(getCapacidadRestante()).append(" kg\n");
        sb.append("  (").append(celdasCalculadas).append(" celdas calculadas)");
        return sb.toString();
    }
}
