package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.DiaDeAtencion;

import java.util.List;

/** El horario semanal ya guardado y los turnos reservados que quedaron fuera de el (Luca decide que hacer). */
public record SemanaGuardada(List<DiaDeAtencion> semana, List<TurnoAdmin> turnosAfectados) {
}
