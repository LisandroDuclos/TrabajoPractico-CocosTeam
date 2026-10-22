package logistica;

import logistica.modelo.Vehiculo;

/**
 * Casos de prueba de la integración de las funcionalidades.
 */
public class PruebasSistema {

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        System.out.println("=== Pruebas: vehículo ===");
        pruebasVehiculo();

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
