package logistica;

import java.time.LocalDate;

import logistica.modelo.Lote;
import logistica.modelo.Pedido;
import logistica.modelo.RedVial;
import logistica.modelo.Vehiculo;
import logistica.resultado.ResultadoBusqueda;
import logistica.sistema.SistemaLogistica;

/**
 * Casos de prueba de la integración de las funcionalidades.
 */
public class PruebasSistema {

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas: vehículo ===");
        pruebasVehiculo();
        System.out.println("=== Pruebas: carga de datos en el sistema ===");
        pruebasCargaDeDatos();
        System.out.println("=== Pruebas: F1 desde el sistema ===");
        pruebasPedidos();

        System.out.println();
        System.out.println("Resultado: " + ok + "/" + (ok + fallas) + " OK");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    //  Vehículo

    private static void pruebasVehiculo() {
        Vehiculo v = new Vehiculo(1, 30, "Depósito Central");
        verificar("guarda id, capacidad y depósito", v.getId() == 1 && v.getCapacidad() == 30
                && v.getDeposito().equals("Depósito Central"));

        verificar("capacidad 0 lanza excepción", vehiculoInvalidoLanza(0, "Depósito Central"));
        verificar("capacidad negativa lanza excepción", vehiculoInvalidoLanza(-5, "Depósito Central"));
        verificar("depósito null lanza excepción", vehiculoInvalidoLanza(30, null));
        verificar("depósito vacío lanza excepción", vehiculoInvalidoLanza(30, ""));
        verificar("capacidad 1 es válida", !vehiculoInvalidoLanza(1, "Depósito Central"));
    }

    private static boolean vehiculoInvalidoLanza(int capacidad, String deposito) {
        try {
            new Vehiculo(1, capacidad, deposito);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    //  Carga de datos en la fachada

    private static void pruebasCargaDeDatos() {
        SistemaLogistica s = new SistemaLogistica(redChica());
        s.agregarPedido(new Pedido(1, "Centro Norte", LocalDate.of(2026, 11, 20), 5, 100));
        s.agregarPedido(new Pedido(2, "Centro Este", LocalDate.of(2026, 11, 15), 3, 50));
        s.agregarLote(new Lote(10, 8, 40));
        s.agregarVehiculo(new Vehiculo(1, 30, "Depósito Central"));

        verificar("guarda los pedidos pendientes", s.getPedidosPendientes().length == 2);
        verificar("guarda el stock", s.getStock().length == 1 && s.getStock()[0].getId() == 10);
        verificar("encuentra el vehículo por id", s.getVehiculo(1).getCapacidad() == 30);

        verificar("pedido con destino fuera de la red lanza excepción",
                agregarPedidoLanza(s, new Pedido(3, "Centro Sur", LocalDate.of(2026, 11, 20), 1, 10)));
        verificar("pedido con id repetido lanza excepción",
                agregarPedidoLanza(s, new Pedido(1, "Centro Este", LocalDate.of(2026, 11, 20), 1, 10)));
        verificar("lote con id repetido lanza excepción", agregarLoteLanza(s, new Lote(10, 5, 5)));
        verificar("vehículo con depósito fuera de la red lanza excepción",
                agregarVehiculoLanza(s, new Vehiculo(2, 30, "Depósito Sur")));
        verificar("vehículo con id repetido lanza excepción",
                agregarVehiculoLanza(s, new Vehiculo(1, 50, "Depósito Central")));
        verificar("vehículo inexistente lanza excepción", getVehiculoLanza(s, 99));
        verificar("red null lanza excepción", sistemaSinRedLanza());
        verificar("los rechazados no se guardan", s.getPedidosPendientes().length == 2
                && s.getStock().length == 1);

        Pedido[] copia = s.getPedidosPendientes();
        copia[0] = null;
        verificar("modificar la copia no altera el sistema", s.getPedidosPendientes()[0] != null);
    }

    //  F1 desde la fachada

    private static void pruebasPedidos() {
        SistemaLogistica vacio = new SistemaLogistica(redChica());
        verificar("sin pedidos: no hay pedido más urgente", vacio.pedidoMasUrgente() == null);
        verificar("sin pedidos: la búsqueda no encuentra nada", !vacio.buscarPedido(1).isEncontrado());

        // 3 y 1 empatan en prioridad: gana la fecha más cercana (3).
        SistemaLogistica s = new SistemaLogistica(redChica());
        s.agregarPedido(new Pedido(1, "Centro Norte", LocalDate.of(2026, 11, 20), 5, 100));
        s.agregarPedido(new Pedido(2, "Centro Este", LocalDate.of(2026, 11, 12), 2, 50));
        s.agregarPedido(new Pedido(3, "Centro Este", LocalDate.of(2026, 11, 18), 5, 70));
        s.agregarPedido(new Pedido(4, "Centro Norte", LocalDate.of(2026, 11, 15), 3, 30));

        verificar("por fecha: 2, 4, 3, 1", ids(s.pedidosPorFecha().getPedidos(), 2, 4, 3, 1));
        verificar("por prioridad: 3, 1, 4, 2", ids(s.pedidosPorPrioridad().getPedidos(), 3, 1, 4, 2));
        verificar("pedido más urgente: 3", s.pedidoMasUrgente().getId() == 3);

        ResultadoBusqueda encontrado = s.buscarPedido(4);
        verificar("busca un pedido existente", encontrado.isEncontrado()
                && encontrado.getPedido().getCentroDestino().equals("Centro Norte"));
        verificar("busca un pedido inexistente", !s.buscarPedido(9).isEncontrado());
        verificar("ordenar no altera los pendientes", ids(s.getPedidosPendientes(), 1, 2, 3, 4));
    }

    private static boolean ids(Pedido[] pedidos, int... esperados) {
        if (pedidos.length != esperados.length) return false;
        for (int i = 0; i < pedidos.length; i++) {
            if (pedidos[i].getId() != esperados[i]) return false;
        }
        return true;
    }

    private static RedVial redChica() {
        RedVial red = new RedVial();
        red.agregarUbicacion("Depósito Central");
        red.agregarUbicacion("Centro Norte");
        red.agregarUbicacion("Centro Este");
        red.agregarConexion("Depósito Central", "Centro Norte", 10);
        red.agregarConexion("Centro Norte", "Centro Este", 8);
        return red;
    }

    private static boolean agregarPedidoLanza(SistemaLogistica s, Pedido pedido) {
        try {
            s.agregarPedido(pedido);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean agregarLoteLanza(SistemaLogistica s, Lote lote) {
        try {
            s.agregarLote(lote);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean agregarVehiculoLanza(SistemaLogistica s, Vehiculo vehiculo) {
        try {
            s.agregarVehiculo(vehiculo);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean getVehiculoLanza(SistemaLogistica s, int id) {
        try {
            s.getVehiculo(id);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean sistemaSinRedLanza() {
        try {
            new SistemaLogistica(null);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void verificar(String nombre, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("  [OK]    " + nombre);
        } else {
            fallas++;
            System.out.println("  [FALLA] " + nombre);
        }
    }
}
