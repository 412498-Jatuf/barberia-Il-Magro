package com.ilmagro.barberia.application.agenda;

import java.util.List;

/** El cierre guardado y los turnos ya reservados que caen en ese cierre (Luca decide si los cancela). */
public record BloqueoCreado(BloqueoAdmin bloqueo, List<TurnoAdmin> turnosAfectados) {
}
