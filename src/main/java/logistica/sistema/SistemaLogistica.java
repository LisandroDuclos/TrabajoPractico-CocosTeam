package logistica.sistema;

import java.util.ArrayList;
import java.util.List;

import logistica.algoritmos.dinamica.SeleccionCargas;
import logistica.algoritmos.dyc.BusquedaBinaria;
import logistica.algoritmos.dyc.MergeSort;
import logistica.algoritmos.greedy.Dijkstra;
import logistica.modelo.Lote;
import logistica.modelo.Pedido;
import logistica.modelo.Pedido.Criterio;
import logistica.modelo.RedVial;
import logistica.modelo.Vehiculo;
import logistica.resultado.ResultadoBusqueda;
import logistica.resultado.ResultadoCarga;
import logistica.resultado.ResultadoOrdenamiento;
import logistica.resultado.ResultadoRuta;
import logistica.resultado.ResultadoViaje;

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

    // Funcionalidad 1: organización y consulta de pedidos (Divide y Conquista)

    public ResultadoOrdenamiento pedidosPorFecha() {
        return pedidosPorFecha(false);
    }

    public ResultadoOrdenamiento pedidosPorFecha(boolean conTraza) {
        return MergeSort.ordenar(getPedidosPendientes(), Criterio.FECHA, conTraza);
    }

    public ResultadoOrdenamiento pedidosPorPrioridad() {
        return pedidosPorPrioridad(false);
    }

    public ResultadoOrdenamiento pedidosPorPrioridad(boolean conTraza) {
        return MergeSort.ordenar(getPedidosPendientes(), Criterio.PRIORIDAD, conTraza);
    }

    public ResultadoBusqueda buscarPedido(int id) {
        return buscarPedido(id, false);
    }

    public ResultadoBusqueda buscarPedido(int id, boolean conTraza) {
        // La búsqueda binaria necesita los pedidos ordenados por id.
        Pedido[] porId = MergeSort.ordenar(getPedidosPendientes(), Criterio.ID).getPedidos();
        return BusquedaBinaria.buscarPorId(porId, id, conTraza);
    }

    /**
     * El pedido que encabeza el orden por prioridad: mayor prioridad y, ante empates,
     * fecha límite más cercana y luego id menor. Devuelve null si no hay pendientes.
     */
    public Pedido pedidoMasUrgente() {
        Pedido[] ordenados = pedidosPorPrioridad().getPedidos();
        return ordenados.length == 0 ? null : ordenados[0];
    }

    // Funcionalidad 2: ruta más rápida (Greedy - Dijkstra)

    public ResultadoRuta rutaMasRapida(String origen, String destino) {
        return rutaMasRapida(origen, destino, false);
    }

    public ResultadoRuta rutaMasRapida(String origen, String destino, boolean conTraza) {
        return Dijkstra.calcular(red, origen, destino, conTraza);
    }

    /** Devuelve true si la conexión existe y quedó deshabilitada. */
    public boolean deshabilitarConexion(String origen, String destino) {
        return red.deshabilitarConexion(origen, destino);
    }

    /** Devuelve true si la conexión existe y quedó habilitada. */
    public boolean habilitarConexion(String origen, String destino) {
        return red.habilitarConexion(origen, destino);
    }

    // Funcionalidad 3: selección de cargas prioritarias (Programación Dinámica)

    public ResultadoCarga armarCarga(int idVehiculo) {
        return armarCarga(idVehiculo, false);
    }

    /**
     * Elige los lotes del stock con la mayor prioridad total que entran en el vehículo.
     * Sólo consulta: el stock no cambia hasta que se despacha el viaje.
     */
    public ResultadoCarga armarCarga(int idVehiculo, boolean conTraza) {
        Vehiculo vehiculo = getVehiculo(idVehiculo);
        return SeleccionCargas.seleccionar(getStock(), vehiculo.getCapacidad(), conTraza);
    }

    // Integración: un viaje encadena F1 -> F3 -> F2

    public ResultadoViaje planificarViaje(int idVehiculo) {
        return planificarViaje(idVehiculo, false);
    }

    /**
     * Planifica y despacha un viaje del vehículo:
     *   1. F1 elige el pedido más urgente, que fija el destino.
     *   2. F3 elige los lotes del stock con mayor prioridad total que entran en el vehículo.
     *   3. F2 calcula la ruta más rápida desde el depósito del vehículo hasta el destino.
     * Si los tres pasos salen bien, el pedido deja de estar pendiente y los lotes cargados
     * salen del stock. Si alguno falla, no se modifica nada y se informa el motivo.
     */
    public ResultadoViaje planificarViaje(int idVehiculo, boolean conTraza) {
        Vehiculo vehiculo = getVehiculo(idVehiculo);

        Pedido pedido = pedidoMasUrgente();
        if (pedido == null) {
            return ResultadoViaje.noDespachado(vehiculo, null, null, null, "No hay pedidos pendientes");
        }

        ResultadoCarga carga = armarCarga(idVehiculo, conTraza);
        if (carga.isVacia()) {
            return ResultadoViaje.noDespachado(vehiculo, pedido, carga, null,
                    "Ningún lote del stock entra en el vehículo");
        }

        ResultadoRuta ruta = rutaMasRapida(vehiculo.getDeposito(), pedido.getCentroDestino(), conTraza);
        if (!ruta.isExiste()) {
            return ResultadoViaje.noDespachado(vehiculo, pedido, carga, ruta,
                    "No hay ruta disponible hasta " + pedido.getCentroDestino());
        }

        quitarPedido(pedido.getId());
        for (Lote lote : carga.getSeleccion()) {
            quitarLote(lote.getId());
        }
        return ResultadoViaje.despachado(vehiculo, pedido, carga, ruta);
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

    // Recorridos lineales para validar ids repetidos, ubicar un vehículo y quitar lo despachado:
    // son colecciones chicas y no hace falta tenerlas ordenadas.

    private void quitarPedido(int id) {
        for (int i = 0; i < pedidos.size(); i++) {
            if (pedidos.get(i).getId() == id) {
                pedidos.remove(i);
                return;
            }
        }
    }

    private void quitarLote(int id) {
        for (int i = 0; i < stock.size(); i++) {
            if (stock.get(i).getId() == id) {
                stock.remove(i);
                return;
            }
        }
    }

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
