package com.ilmagro.barberia.domain.shared;

/** Se lanza cuando un dato viola una regla del dominio. */
public class DatoInvalidoException extends RuntimeException {
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
