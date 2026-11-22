package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Turno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Un turno tal como lo ve Luca en el panel: con los datos del cliente para poder avisarle. */
public record TurnoAdmin(
        Long id,
        String clienteNombre,
        String clienteTelefono,
        String clienteEmail,
        String servicio,
        int duracionMinutos,
        String barbero,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        String notas,
        boolean cancelable
) {
    static TurnoAdmin de(Turno turno, LocalDateTime ahora) {
        return new TurnoAdmin(
                turno.getId(),
                turno.getCliente().getNombre(),
                turno.getCliente().getTelefono(),
                turno.getCliente().getEmail(),
                turno.getServicio().getNombre(),
                turno.getServicio().getDuracionMinutos(),
                turno.getBarbero().getNombre(),
                turno.getFecha(),
                turno.getHoraInicio(),
                turno.getHoraFin(),
                turno.getEstado().name(),
                turno.getNotas(),
                turno.sePuedeCancelar(ahora));
    }
}
