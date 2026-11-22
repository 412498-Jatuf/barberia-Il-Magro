package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Reglas fijas para reservar (no cambian desde el panel): cada cuantos minutos arranca un turno,
 * con cuantos dias de anticipacion se puede reservar y la zona horaria de la barberia.
 * Las horas y los dias que se atienden estan en {@link Jornada}.
 */
public record ReglasDeReserva(int intervaloMinutos, int diasAnticipacion, ZoneId zona) {

    public ReglasDeReserva {
        if (intervaloMinutos <= 0) {
            throw new DatoInvalidoException("El intervalo de turnos debe ser positivo");
        }
        if (diasAnticipacion < 0) {
            throw new DatoInvalidoException("Los dias de anticipacion no pueden ser negativos");
        }
        if (zona == null) {
            throw new DatoInvalidoException("Falta la zona horaria");
        }
    }

    /** True si la fecha no paso y esta dentro de la ventana de reservas (hoy + dias de anticipacion). */
    public boolean admiteReservas(LocalDate fecha, LocalDate hoy) {
        return !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(diasAnticipacion));
    }
}
