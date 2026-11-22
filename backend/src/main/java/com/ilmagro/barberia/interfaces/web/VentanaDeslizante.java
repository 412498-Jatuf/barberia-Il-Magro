package com.ilmagro.barberia.interfaces.web;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cuenta pedidos por clave (por ejemplo "reserva|IP") en una ventana de tiempo que se va corriendo:
 * deja pasar hasta el limite dentro de la ultima ventana y despues frena. Es seguro con varios hilos.
 */
class VentanaDeslizante {

    private static final int MAX_CLAVES = 50_000;

    private final Duration ventana;
    private final Clock clock;
    private final Map<String, Deque<Long>> pedidos = new ConcurrentHashMap<>();

    VentanaDeslizante(Duration ventana, Clock clock) {
        this.ventana = ventana;
        this.clock = clock;
    }

    Duration ventana() {
        return ventana;
    }

    /** Registra el pedido y devuelve true si entra en el limite; si no, no lo registra y devuelve false. */
    boolean permitir(String clave, int limite) {
        long ahora = clock.millis();
        long desde = ahora - ventana.toMillis();
        if (pedidos.size() > MAX_CLAVES) {
            limpiar(desde);
        }
        Deque<Long> marcas = pedidos.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (marcas) {
            while (!marcas.isEmpty() && marcas.peekFirst() < desde) {
                marcas.pollFirst();
            }
            if (marcas.size() >= limite) {
                return false;
            }
            marcas.addLast(ahora);
            return true;
        }
    }

    /** Si hay demasiadas claves (muchas IPs distintas), borra las que ya no tienen pedidos recientes. */
    private void limpiar(long desde) {
        pedidos.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast() < desde;
            }
        });
    }
}
