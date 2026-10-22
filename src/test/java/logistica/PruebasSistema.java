package logistica;

import java.time.LocalDate;

import logistica.modelo.Lote;
import logistica.modelo.Pedido;
import logistica.modelo.RedVial;
import logistica.modelo.Ubicacion;
import logistica.modelo.Vehiculo;
import logistica.resultado.ResultadoBusqueda;
import logistica.resultado.ResultadoCarga;
import logistica.resultado.ResultadoRuta;
import logistica.resultado.ResultadoViaje;
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
        System.out.println("=== Pruebas: F2 desde el sistema ===");
        pruebasRutas();
        System.out.println("=== Pruebas: F3 desde el sistema ===");
        pruebasCargas();
        System.out.println("=== Pruebas: viaje integrado F1 -> F3 -> F2 ===");
        pruebasViaje();
        System.out.println("=== Pruebas: viajes que no se despachan ===");
        pruebasViajeNoDespachado();

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

    //  F2 desde la fachada

    private static void pruebasRutas() {
        SistemaLogistica s = new SistemaLogistica(redChica());

        ResultadoRuta normal = s.rutaMasRapida("Depósito Central", "Centro Este");
        verificar("pasa por Centro Norte (18 min) y no por la directa (25 min)", normal.isExiste()
                && normal.getTiempoTotal() == 18
                && camino(normal, "Depósito Central", "Centro Norte", "Centro Este"));

        verificar("deshabilitar una conexión existente", s.deshabilitarConexion("Centro Norte", "Centro Este"));
        ResultadoRuta desvio = s.rutaMasRapida("Depósito Central", "Centro Este");
        verificar("recalcula por la directa (25 min)", desvio.isExiste() && desvio.getTiempoTotal() == 25
                && camino(desvio, "Depósito Central", "Centro Este"));

        s.deshabilitarConexion("Depósito Central", "Centro Este");
        verificar("sin conexiones habilitadas no hay ruta",
                !s.rutaMasRapida("Depósito Central", "Centro Este").isExiste());

        verificar("habilitar una conexión existente", s.habilitarConexion("Centro Norte", "Centro Este"));
        verificar("vuelve a la ruta de 18 min", s.rutaMasRapida("Depósito Central", "Centro Este").getTiempoTotal() == 18);

        verificar("conexión inexistente: no se deshabilita", !s.deshabilitarConexion("Centro Este", "Centro Norte"));
        verificar("ruta hacia una ubicación inexistente lanza excepción", rutaLanza(s, "Depósito Central", "Centro Sur"));
    }

    //  F3 desde la fachada

    private static void pruebasCargas() {
        SistemaLogistica s = new SistemaLogistica(redChica());
        s.agregarVehiculo(new Vehiculo(1, 30, "Depósito Central"));
        s.agregarVehiculo(new Vehiculo(2, 4, "Depósito Central"));
        verificar("stock vacío: no se carga nada", s.armarCarga(1).isVacia());

        // Sumar de mayor a menor prioridad (204 + 202) da 125; el óptimo es 140.
        s.agregarLote(new Lote(201, 8, 40));
        s.agregarLote(new Lote(202, 12, 55));
        s.agregarLote(new Lote(203, 5, 20));
        s.agregarLote(new Lote(204, 15, 70));
        s.agregarLote(new Lote(205, 10, 45));
        s.agregarLote(new Lote(206, 6, 30));

        ResultadoCarga carga = s.armarCarga(1);
        verificar("usa la capacidad del vehículo", carga.getCapacidad() == 30);
        verificar("elige 201 + 202 + 205 con prioridad 140", ids(carga.getSeleccion(), 201, 202, 205)
                && carga.getPrioridadTotal() == 140 && carga.getPesoTotal() == 30
                && carga.getCapacidadRestante() == 0);
        verificar("vehículo de 4 kg: ningún lote entra", s.armarCarga(2).isVacia());
        verificar("armar la carga no saca lotes del stock", s.getStock().length == 6);
        verificar("armar carga de un vehículo inexistente lanza excepción", armarCargaLanza(s, 9));
    }

    private static boolean armarCargaLanza(SistemaLogistica s, int idVehiculo) {
        try {
            s.armarCarga(idVehiculo);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    //  Viaje integrado

    private static void pruebasViaje() {
        SistemaLogistica s = sistemaConDatos();

        // Viaje 1: 104 y 107 empatan en prioridad 5, gana 104 por fecha -> Centro Este.
        ResultadoViaje v1 = s.planificarViaje(1);
        verificar("viaje 1: se despacha", v1.isDespachado());
        verificar("viaje 1: atiende el pedido más urgente (#104)", v1.getPedido().getId() == 104);
        verificar("viaje 1: carga 201 + 202 + 205 con prioridad 140",
                ids(v1.getCarga().getSeleccion(), 201, 202, 205) && v1.getCarga().getPrioridadTotal() == 140);
        verificar("viaje 1: ruta Central -> Norte -> Este en 18 min", v1.getRuta().getTiempoTotal() == 18
                && camino(v1.getRuta(), "Depósito Central", "Centro Norte", "Centro Este"));
        verificar("viaje 1: el pedido deja de estar pendiente", ids(s.getPedidosPendientes(), 107, 103)
                && !s.buscarPedido(104).isEncontrado());
        verificar("viaje 1: los lotes cargados salen del stock", ids(s.getStock(), 203, 204, 206));

        // Viaje 2: sigue con 107 -> Centro Norte y lo que queda del stock entra entero (26 kg).
        ResultadoViaje v2 = s.planificarViaje(1);
        verificar("viaje 2: atiende #107 con el stock restante", v2.isDespachado()
                && v2.getPedido().getId() == 107
                && ids(v2.getCarga().getSeleccion(), 203, 204, 206)
                && v2.getCarga().getPrioridadTotal() == 120);
        verificar("viaje 2: ruta directa a Centro Norte en 10 min", v2.getRuta().getTiempoTotal() == 10);
        verificar("viaje 2: queda #103 y el stock vacío", ids(s.getPedidosPendientes(), 103)
                && s.getStock().length == 0);

        // Viaje 3: hay pedido pero no queda mercadería.
        ResultadoViaje v3 = s.planificarViaje(1);
        verificar("viaje 3: sin stock no se despacha", !v3.isDespachado() && v3.getCarga().isVacia()
                && v3.getRuta() == null);
        verificar("viaje 3: el pedido sigue pendiente", ids(s.getPedidosPendientes(), 103));
    }

    private static void pruebasViajeNoDespachado() {
        SistemaLogistica sinPedidos = new SistemaLogistica(redChica());
        sinPedidos.agregarVehiculo(new Vehiculo(1, 30, "Depósito Central"));
        sinPedidos.agregarLote(new Lote(1, 5, 10));
        ResultadoViaje vacio = sinPedidos.planificarViaje(1);
        verificar("sin pedidos pendientes no se despacha", !vacio.isDespachado()
                && vacio.getPedido() == null && sinPedidos.getStock().length == 1);

        SistemaLogistica chico = sistemaConDatos();
        ResultadoViaje noEntra = chico.planificarViaje(2);
        verificar("vehículo de 4 kg: no se despacha", !noEntra.isDespachado()
                && noEntra.getPedido().getId() == 104 && noEntra.getCarga().isVacia());
        verificar("vehículo de 4 kg: no cambia nada", chico.getPedidosPendientes().length == 3
                && chico.getStock().length == 6);

        // A Centro Este se llega por Norte o por la directa: se cortan las dos.
        SistemaLogistica cortado = sistemaConDatos();
        cortado.deshabilitarConexion("Centro Norte", "Centro Este");
        cortado.deshabilitarConexion("Depósito Central", "Centro Este");
        ResultadoViaje sinRuta = cortado.planificarViaje(1);
        verificar("sin ruta al destino no se despacha", !sinRuta.isDespachado()
                && sinRuta.getRuta() != null && !sinRuta.getRuta().isExiste());
        verificar("sin ruta: el pedido y el stock no cambian", cortado.getPedidosPendientes().length == 3
                && cortado.getStock().length == 6);

        verificar("viaje de un vehículo inexistente lanza excepción", planificarViajeLanza(cortado, 9));
    }

    /** Pedidos, stock y vehículos del camino feliz sobre la red chica. */
    private static SistemaLogistica sistemaConDatos() {
        SistemaLogistica s = new SistemaLogistica(redChica());
        s.agregarPedido(new Pedido(107, "Centro Norte", LocalDate.of(2026, 11, 20), 5, 300));
        s.agregarPedido(new Pedido(104, "Centro Este", LocalDate.of(2026, 11, 18), 5, 250));
        s.agregarPedido(new Pedido(103, "Centro Norte", LocalDate.of(2026, 11, 25), 4, 150));
        s.agregarLote(new Lote(201, 8, 40));
        s.agregarLote(new Lote(202, 12, 55));
        s.agregarLote(new Lote(203, 5, 20));
        s.agregarLote(new Lote(204, 15, 70));
        s.agregarLote(new Lote(205, 10, 45));
        s.agregarLote(new Lote(206, 6, 30));
        s.agregarVehiculo(new Vehiculo(1, 30, "Depósito Central"));
        s.agregarVehiculo(new Vehiculo(2, 4, "Depósito Central"));
        return s;
    }

    private static boolean planificarViajeLanza(SistemaLogistica s, int idVehiculo) {
        try {
            s.planificarViaje(idVehiculo);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean ids(Lote[] lotes, int... esperados) {
        if (lotes.length != esperados.length) return false;
        for (int i = 0; i < lotes.length; i++) {
            if (lotes[i].getId() != esperados[i]) return false;
        }
        return true;
    }

    private static boolean camino(ResultadoRuta ruta, String... esperados) {
        Ubicacion[] camino = ruta.getCamino();
        if (camino.length != esperados.length) return false;
        for (int i = 0; i < camino.length; i++) {
            if (!camino[i].getNombre().equals(esperados[i])) return false;
        }
        return true;
    }

    private static boolean rutaLanza(SistemaLogistica s, String origen, String destino) {
        try {
            s.rutaMasRapida(origen, destino);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
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
        red.agregarConexion("Depósito Central", "Centro Este", 25);
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
