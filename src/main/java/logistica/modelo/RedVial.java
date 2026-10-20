package logistica.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedVial {

    private final List<Ubicacion> ubicaciones = new ArrayList<>();
    private final List<List<Conexion>> adyacencia = new ArrayList<>();
    private final Map<String, Integer> idPorNombre = new HashMap<>();
    private int cantidadConexiones = 0;

    // Construcción de la red

    public void agregarUbicacion(String nombre) {
        if (idPorNombre.containsKey(nombre)) {
            throw new IllegalArgumentException("Ya existe la ubicación: " + nombre);
        }
        Ubicacion u = new Ubicacion(ubicaciones.size(), nombre);
        ubicaciones.add(u);
        adyacencia.add(new ArrayList<>());
        idPorNombre.put(nombre, u.getId());
    }

    public void agregarConexion(int origen, int destino, int tiempo) {
        validar(origen);
        validar(destino);
        if (buscarConexion(origen, destino) != null) {
            throw new IllegalArgumentException("Ya existe la conexión "
                    + ubicaciones.get(origen) + " -> " + ubicaciones.get(destino));
        }
        Conexion c = new Conexion(destino, tiempo);
        adyacencia.get(origen).add(c);
        cantidadConexiones++;
    }

    public void agregarConexion(String origen, String destino, int tiempo) {
        agregarConexion(getId(origen), getId(destino), tiempo);
    }

    // Deshabilitar / habilitar conexiones
    public boolean deshabilitarConexion(String origen, String destino) { // true si existe la conexion y quedó dehabilitada
        return cambiarEstado(getId(origen), getId(destino), false);
    }

    public void habilitarConexion(String origen, String destino) {   // true si existe la conexion y quedó habilitada
        cambiarEstado(getId(origen), getId(destino), true);
    }

    private boolean cambiarEstado(int origen, int destino, boolean habilitada) {
        Conexion c = buscarConexion(origen, destino);
        if (c == null) return false;
        c.setHabilitada(habilitada);
        return true;
    }

    // Consultas

    public int cantidadUbicaciones() { return ubicaciones.size(); }
    public int cantidadConexiones() { return cantidadConexiones; }

    public Ubicacion getUbicacion(int id) {
        validar(id);
        return ubicaciones.get(id);
    }

    public int getId(String nombre) {
        Integer id = idPorNombre.get(nombre);
        if (id == null) {
            throw new IllegalArgumentException("No existe la ubicación: " + nombre);
        }
        return id;
    }

    public List<Conexion> getConexionesSalientes(int id) {
        validar(id);
        return Collections.unmodifiableList(adyacencia.get(id));
    }

    private Conexion buscarConexion(int origen, int destino) {
        for (Conexion c : adyacencia.get(origen)) {
            if (c.getDestino() == destino) return c;
        }
        return null;
    }

    private void validar(int id) {
        if (id < 0 || id >= ubicaciones.size()) {
            throw new IllegalArgumentException("Ubicación inexistente (id " + id + ")");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Red vial: ").append(ubicaciones.size()).append(" ubicaciones, ")
                .append(cantidadConexiones).append(" conexiones\n");
        for (Ubicacion u : ubicaciones) {
            for (Conexion c : adyacencia.get(u.getId())) {
                sb.append(String.format("  %-16s -> %-16s %3d min%s%n",
                        u, ubicaciones.get(c.getDestino()), c.getTiempo(),
                        c.isHabilitada() ? "" : "   [DESHABILITADA]"));
            }
        }
        return sb.toString();
    }
}