package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Turno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** El turno visto desde el link de cancelacion: que es, cuando, y si todavia se puede cancelar. */
public record TurnoParaCancelar(
        String clienteNombre,
        String servicio,
        String barbero,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        boolean cancelable
) {
    static TurnoParaCancelar de(Turno turno, LocalDateTime ahora) {
        return new TurnoParaCancelar(
                turno.getCliente().getNombre(),
                turno.getServicio().getNombre(),
                turno.getBarbero().getNombre(),
                turno.getFecha(),
                turno.getHoraInicio(),
                turno.getHoraFin(),
                turno.getEstado().name(),
                turno.sePuedeCancelar(ahora));
    }
}
