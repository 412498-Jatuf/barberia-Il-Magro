package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Bloqueo;

import java.time.LocalDate;
import java.time.LocalTime;

/** Un cierre de la agenda tal como lo ve Luca: un dia entero (sin horas) o unas horas de un dia. */
public record BloqueoAdmin(Long id, LocalDate fecha, LocalTime desde, LocalTime hasta, String motivo,
                           boolean diaCompleto) {
    static BloqueoAdmin de(Bloqueo b) {
        return new BloqueoAdmin(b.getId(), b.getFecha(), b.getDesde(), b.getHasta(), b.getMotivo(), b.esDiaCompleto());
    }
}
