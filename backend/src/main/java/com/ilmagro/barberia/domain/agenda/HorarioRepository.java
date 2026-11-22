package com.ilmagro.barberia.domain.agenda;

import java.time.LocalDate;
import java.util.List;

/** Donde se guarda el horario semanal de la barberia y los cierres puntuales de la agenda. */
public interface HorarioRepository {

    /** Todos los tramos del horario semanal, ordenados por dia y hora. */
    List<TramoSemanal> semana();

    /** Los cierres puntuales con fecha entre {@code desde} y {@code hasta} (inclusive). */
    List<Bloqueo> bloqueosEntre(LocalDate desde, LocalDate hasta);
}
