package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

/**
 * Value object con las reglas de atencion de la barberia.
 *
 * @param diasAtencion dias segun ISO-8601 (1 = lunes ... 7 = domingo)
 */
public record HorarioAtencion(
        LocalTime apertura,
        LocalTime cierre,
        int intervaloMinutos,
        Set<Integer> diasAtencion,
        int diasAnticipacion,
        ZoneId zona
) {

    public HorarioAtencion {
        if (apertura == null || cierre == null || !cierre.isAfter(apertura)) {
            throw new DatoInvalidoException("El cierre debe ser posterior a la apertura");
        }
        if (intervaloMinutos <= 0) {
            throw new DatoInvalidoException("El intervalo de turnos debe ser positivo");
        }
        if (diasAtencion == null || diasAtencion.isEmpty()
                || diasAtencion.stream().anyMatch(d -> d < 1 || d > 7)) {
            throw new DatoInvalidoException("Los dias de atencion deben estar entre 1 y 7");
        }
        diasAtencion = Set.copyOf(diasAtencion);
    }

    /** True si la barberia atiende ese dia y esta dentro de la ventana de reservas. */
    public boolean admiteReservas(LocalDate fecha, LocalDate hoy) {
        return !fecha.isBefore(hoy)
                && !fecha.isAfter(hoy.plusDays(diasAnticipacion))
                && diasAtencion.contains(fecha.getDayOfWeek().getValue());
    }
}
