package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Turno;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Evento de aplicacion: se confirmo un turno. Lo publica {@link ReservaTurnosService} y lo escuchan
 * los avisos a la barberia (infrastructure/avisos) una vez que la transaccion se confirmo.
 * Lleva los datos ya resueltos para no depender de la sesion de JPA.
 */
public record TurnoConfirmado(
        Long id,
        String clienteNombre,
        String clienteTelefono,
        String clienteEmail,
        String servicio,
        BigDecimal precio,
        int duracionMinutos,
        String barbero,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String notas,
        String codigoCancelacion
) {
    static TurnoConfirmado de(Turno turno) {
        return new TurnoConfirmado(
                turno.getId(),
                turno.getCliente().getNombre(),
                turno.getCliente().getTelefono(),
                turno.getCliente().getEmail(),
                turno.getServicio().getNombre(),
                turno.getServicio().getPrecio(),
                turno.getServicio().getDuracionMinutos(),
                turno.getBarbero().getNombre(),
                turno.getFecha(),
                turno.getHoraInicio(),
                turno.getHoraFin(),
                turno.getNotas(),
                turno.getCodigoCancelacion());
    }
}
