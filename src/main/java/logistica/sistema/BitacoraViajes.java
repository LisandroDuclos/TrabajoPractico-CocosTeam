package logistica.sistema;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import logistica.modelo.Lote;
import logistica.modelo.Pedido;
import logistica.modelo.Ubicacion;
import logistica.modelo.Vehiculo;
import logistica.resultado.ResultadoCarga;
import logistica.resultado.ResultadoRuta;
import logistica.resultado.ResultadoViaje;

/**
 * Presenta en consola, como una bitácora, los viajes que planifica el sistema:
 * para cada viaje muestra qué se decide en cada paso, con qué estrategia y por qué,
 * y al final un resumen de todos los viajes. Sólo se ocupa de mostrar: la lógica
 * está en SistemaLogistica y en los algoritmos.
 */
public class BitacoraViajes {

    private static final String LINEA = "=".repeat(70);

    private final SistemaLogistica sistema;
    private final List<ResultadoViaje> viajes = new ArrayList<>();

    public BitacoraViajes(SistemaLogistica sistema) {
        this.sistema = sistema;
    }

    // Presentación: objetivo del programa y estado inicial

    public void presentar(int... idsVehiculos) {
        System.out.println();
        System.out.println(LINEA);
        System.out.println("SISTEMA DE DISTRIBUCIÓN DE AYUDA - Planificación de viajes");
        System.out.println(LINEA);
        System.out.println("Objetivo: atender primero los pedidos más urgentes, llevando en cada viaje");
        System.out.println("la carga de mayor prioridad que entra en el vehículo, por la ruta más rápida.");
        System.out.println();
        System.out.println("Cada viaje se arma en tres pasos:");
        System.out.println("  [1/3] Qué pedido atender   -> F1, Divide y Conquista (Merge Sort por prioridad)");
        System.out.println("  [2/3] Qué se carga         -> F3, Programación Dinámica (mochila 0/1)");
        System.out.println("  [3/3] Por dónde ir         -> F2, Greedy (Dijkstra)");
        System.out.println("Si los tres pasos salen bien, el viaje se despacha: el pedido deja de estar");
        System.out.println("pendiente y los lotes cargados salen del stock.");

        System.out.println();
        System.out.println("Estado inicial");
        for (int id : idsVehiculos) {
            System.out.println("  Vehículo   " + sistema.getVehiculo(id));
        }
        imprimirPendientes("  Pedidos    ");
        imprimirStock("  Stock      ");
    }

    // Eventos que cambian el escenario entre un viaje y otro

    public void evento(String descripcion) {
        System.out.println();
        System.out.println("* Evento: " + descripcion);
    }

    // Un viaje, paso a paso

    public ResultadoViaje viaje(int idVehiculo) {
        return viaje(idVehiculo, false);
    }

    /** Planifica el viaje con el sistema y lo muestra. Con traza, agrega las decisiones de la ruta. */
    public ResultadoViaje viaje(int idVehiculo, boolean conTraza) {
        // Estado antes del viaje, para poder explicar las decisiones.
        Pedido[] ranking = sistema.pedidosPorPrioridad().getPedidos();
        Lote[] stockAntes = sistema.getStock();

        // La traza de Dijkstra se imprime durante el cálculo: se captura para mostrarla en su paso.
        ResultadoViaje viaje;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            viaje = sistema.planificarViaje(idVehiculo, conTraza);
        } finally {
            System.setOut(original);
        }
        String traza = buffer.toString(StandardCharsets.UTF_8);

        viajes.add(viaje);
        Vehiculo v = viaje.getVehiculo();

        System.out.println();
        System.out.println(LINEA);
        System.out.println("VIAJE " + viajes.size() + " - Vehículo V" + v.getId() + " (" + v.getCapacidad()
                + " kg, sale de " + v.getDeposito() + ")");
        System.out.println(LINEA);

        pasoPedido(viaje.getPedido(), ranking);
        pasoCarga(viaje.getCarga(), stockAntes, v);
        pasoRuta(viaje.getRuta(), traza);

        System.out.println();
        if (viaje.isDespachado()) {
            System.out.println(">> DESPACHADO. Quedan " + cantidad(sistema.getPedidosPendientes().length, "pedido pendiente", "pedidos pendientes")
                    + " y " + cantidad(sistema.getStock().length, "lote", "lotes") + " en stock.");
        } else {
            System.out.println(">> NO SE DESPACHA: " + viaje.getMotivo() + ".");
            System.out.println("   No se modificaron los pedidos pendientes ni el stock.");
        }
        return viaje;
    }

    private void pasoPedido(Pedido pedido, Pedido[] ranking) {
        System.out.println("[1/3] Qué pedido atender?                  (F1 - Divide y Conquista)");
        if (pedido == null) {
            System.out.println("      No hay pedidos pendientes.");
            return;
        }
        System.out.println("      Más urgente: #" + pedido.getId() + " -> " + pedido.getCentroDestino()
                + "  (prioridad " + pedido.getPrioridad() + ", límite " + pedido.getFechaLimite()
                + ", " + pedido.getPeso() + " kg)");
        System.out.println("      " + motivoEleccion(pedido, ranking));
    }

    /** Explica por qué el pedido encabeza el orden por prioridad (y qué regla de desempate decidió). */
    private String motivoEleccion(Pedido elegido, Pedido[] ranking) {
        if (ranking.length == 1) {
            return "Es el único pedido pendiente.";
        }
        Pedido segundo = ranking[1];
        if (segundo.getPrioridad() != elegido.getPrioridad()) {
            return "Tiene la mayor prioridad entre los " + ranking.length + " pendientes.";
        }
        if (!segundo.getFechaLimite().equals(elegido.getFechaLimite())) {
            return "Empata en prioridad con #" + segundo.getId() + "; gana por fecha límite más cercana ("
                    + elegido.getFechaLimite() + " contra " + segundo.getFechaLimite() + ").";
        }
        return "Empata en prioridad y fecha con #" + segundo.getId() + "; gana por id menor.";
    }

    private void pasoCarga(ResultadoCarga carga, Lote[] stockAntes, Vehiculo v) {
        System.out.println("[2/3] Qué se carga?                        (F3 - Programación Dinámica)");
        if (carga == null) {
            System.out.println("      No se llegó a este paso.");
            return;
        }
        System.out.println("      Stock disponible: " + stockAntes.length + " lotes, " + pesoTotal(stockAntes) + " kg.");
        if (carga.isVacia()) {
            System.out.println("      Ningún lote entra en los " + v.getCapacidad() + " kg del vehículo.");
            return;
        }
        StringBuilder lotes = new StringBuilder();
        for (Lote lote : carga.getSeleccion()) {
            if (lotes.length() > 0) lotes.append(", ");
            lotes.append("#").append(lote.getId()).append(" (").append(lote.getPeso()).append(" kg, prio ")
                    .append(lote.getPrioridad()).append(")");
        }
        System.out.println("      Se cargan: " + lotes);
        System.out.println("      Ocupa " + carga.getPesoTotal() + "/" + v.getCapacidad() + " kg | Prioridad total "
                + carga.getPrioridadTotal() + " (la máxima posible) | Libre " + carga.getCapacidadRestante() + " kg");
    }

    private void pasoRuta(ResultadoRuta ruta, String traza) {
        System.out.println("[3/3] Por dónde ir?                        (F2 - Greedy, Dijkstra)");
        if (ruta == null) {
            System.out.println("      No se llegó a este paso.");
            return;
        }
        if (ruta.isExiste()) {
            System.out.println("      " + recorrido(ruta) + "  (" + ruta.getTiempoTotal() + " min)");
        } else {
            System.out.println("      No existe una ruta disponible hasta el destino.");
        }
        if (!traza.isBlank()) {
            System.out.println("      Decisiones del cálculo:");
            for (String linea : traza.split("\\R")) {
                if (!linea.isBlank()) {
                    System.out.println("      | " + linea.stripLeading());
                }
            }
        }
    }

    // Resumen final

    public void resumen() {
        System.out.println();
        System.out.println(LINEA);
        System.out.println("RESUMEN DE LA JORNADA");
        System.out.println(LINEA);
        System.out.printf("%-6s %-9s %-7s %-14s %5s %5s %10s  %s%n",
                "Viaje", "Vehículo", "Pedido", "Destino", "Min", "Kg", "Prioridad", "Estado");
        int despachados = 0;
        int minutos = 0;
        int prioridad = 0;
        for (int i = 0; i < viajes.size(); i++) {
            ResultadoViaje v = viajes.get(i);
            Pedido p = v.getPedido();
            String pedido = p == null ? "-" : "#" + p.getId();
            String destino = p == null ? "-" : p.getCentroDestino();
            if (v.isDespachado()) {
                despachados++;
                minutos += v.getRuta().getTiempoTotal();
                prioridad += v.getCarga().getPrioridadTotal();
                System.out.printf("%-6d V%-8d %-7s %-14s %5d %5d %10d  %s%n", i + 1, v.getVehiculo().getId(),
                        pedido, destino, v.getRuta().getTiempoTotal(), v.getCarga().getPesoTotal(),
                        v.getCarga().getPrioridadTotal(), "despachado");
            } else {
                System.out.printf("%-6d V%-8d %-7s %-14s %5s %5s %10s  %s%n", i + 1, v.getVehiculo().getId(),
                        pedido, destino, "-", "-", "-", "no despachado");
            }
        }
        System.out.println();
        System.out.println("Viajes despachados: " + despachados + " de " + viajes.size()
                + " | Tiempo total de viaje: " + minutos + " min | Prioridad entregada: " + prioridad);
        imprimirPendientes("Pedidos pendientes: ");
        imprimirStock("Stock restante:     ");
    }

    // Auxiliares

    private void imprimirPendientes(String etiqueta) {
        Pedido[] pendientes = sistema.pedidosPorPrioridad().getPedidos();
        if (pendientes.length == 0) {
            System.out.println(etiqueta + "ninguno");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Pedido p : pendientes) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("#").append(p.getId()).append(" ").append(p.getCentroDestino())
                    .append(" (prio ").append(p.getPrioridad()).append(")");
        }
        System.out.println(etiqueta + pendientes.length + " por prioridad: " + sb);
    }

    private void imprimirStock(String etiqueta) {
        Lote[] stock = sistema.getStock();
        if (stock.length == 0) {
            System.out.println(etiqueta + "vacío");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Lote l : stock) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("#").append(l.getId());
        }
        System.out.println(etiqueta + cantidad(stock.length, "lote", "lotes") + ", " + pesoTotal(stock) + " kg: " + sb);
    }

    private static String cantidad(int n, String singular, String plural) {
        return n + " " + (n == 1 ? singular : plural);
    }

    private static int pesoTotal(Lote[] lotes) {
        int total = 0;
        for (Lote l : lotes) total += l.getPeso();
        return total;
    }

    private static String recorrido(ResultadoRuta ruta) {
        StringBuilder sb = new StringBuilder();
        for (Ubicacion u : ruta.getCamino()) {
            if (sb.length() > 0) sb.append(" -> ");
            sb.append(u);
        }
        return sb.toString();
    }
}
