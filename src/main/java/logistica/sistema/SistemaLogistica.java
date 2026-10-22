package logistica.sistema;

import java.util.ArrayList;
import java.util.List;

import logistica.modelo.Lote;
import logistica.modelo.Pedido;
import logistica.modelo.RedVial;
import logistica.modelo.Vehiculo;

/**
 * Fachada que reúne los datos de la organización y los mantiene consistentes:
 * la red vial, los pedidos pendientes, los lotes en stock y los vehículos.
 * Las funcionalidades se exponen desde acá llamando a los algoritmos existentes.
 */
public class SistemaLogistica {

    private final RedVial red;
    private final List<Pedido> pedidos = new ArrayList<>();
    private final List<Lote> stock = new ArrayList<>();
    private final List<Vehiculo> vehiculos = new ArrayList<>();

    public SistemaLogistica(RedVial red) {
        if (red == null) {
            throw new IllegalArgumentException("La red vial no puede ser null");
        }
        this.red = red;
    }

    // Carga de datos

    public void agregarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser null");
        }
        if (!red.existeUbicacion(pedido.getCentroDestino())) {
            throw new IllegalArgumentException("El destino del pedido #" + pedido.getId()
                    + " no existe en la red: " + pedido.getCentroDestino());
        }
        if (buscarPedidoPendiente(pedido.getId()) != null) {
            throw new IllegalArgumentException("Ya existe el pedido #" + pedido.getId());
        }
        pedidos.add(pedido);
    }

    public void agregarLote(Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote no puede ser null");
        }
        if (buscarLote(lote.getId()) != null) {
            throw new IllegalArgumentException("Ya existe el lote #" + lote.getId());
        }
        stock.add(lote);
    }

    public void agregarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser null");
        }
        if (!red.existeUbicacion(vehiculo.getDeposito())) {
            throw new IllegalArgumentException("El depósito del vehículo V" + vehiculo.getId()
                    + " no existe en la red: " + vehiculo.getDeposito());
        }
        if (buscarVehiculo(vehiculo.getId()) != null) {
            throw new IllegalArgumentException("Ya existe el vehículo V" + vehiculo.getId());
        }
        vehiculos.add(vehiculo);
    }

    // Consultas

    public RedVial getRed() { return red; }

    public Pedido[] getPedidosPendientes() {
        return pedidos.toArray(new Pedido[0]);
    }

    public Lote[] getStock() {
        return stock.toArray(new Lote[0]);
    }

    public Vehiculo getVehiculo(int id) {
        Vehiculo v = buscarVehiculo(id);
        if (v == null) {
            throw new IllegalArgumentException("No existe el vehículo V" + id);
        }
        return v;
    }

    // Recorridos lineales para validar ids repetidos y ubicar un vehículo:
    // son colecciones chicas y no hace falta tenerlas ordenadas.

    private Pedido buscarPedidoPendiente(int id) {
        for (Pedido p : pedidos) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private Lote buscarLote(int id) {
        for (Lote l : stock) {
            if (l.getId() == id) return l;
        }
        return null;
    }

    private Vehiculo buscarVehiculo(int id) {
        for (Vehiculo v : vehiculos) {
            if (v.getId() == id) return v;
        }
        return null;
    }
}
