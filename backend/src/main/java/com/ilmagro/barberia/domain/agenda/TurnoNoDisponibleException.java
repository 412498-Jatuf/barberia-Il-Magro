package com.ilmagro.barberia.domain.agenda;

public class TurnoNoDisponibleException extends RuntimeException {
    public TurnoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
