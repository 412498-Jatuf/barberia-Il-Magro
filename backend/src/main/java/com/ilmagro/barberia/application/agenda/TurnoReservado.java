package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Turno;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Resultado del caso de uso "reservar turno" (se arma dentro de la transaccion). */
public record TurnoReservado(
        Long id,
        String clienteNombre,
        String servicio,
        BigDecimal precio,
        String barbero,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        String codigoCancelacion
) {
    static TurnoReservado de(Turno turno) {
        return new TurnoReservado(
                turno.getId(),
                turno.getCliente().getNombre(),
                turno.getServicio().getNombre(),
                turno.getServicio().getPrecio(),
                turno.getBarbero().getNombre(),
                turno.getFecha(),
                turno.getHoraInicio(),
                turno.getHoraFin(),
                turno.getEstado().name(),
                turno.getCodigoCancelacion());
    }
}
