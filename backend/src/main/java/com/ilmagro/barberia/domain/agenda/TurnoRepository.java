package com.ilmagro.barberia.domain.agenda;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository {

    /** Turnos confirmados de un barbero en un dia. */
    List<Turno> confirmadosDelDia(Long barberoId, LocalDate fecha);

    Turno guardar(Turno turno);

    Optional<Turno> buscarPorId(Long id);

    /** Todos los turnos (confirmados y cancelados) con fecha entre {@code desde} y {@code hasta}, en orden. */
    List<Turno> turnosEntre(LocalDate desde, LocalDate hasta);

    /** El turno del link de cancelacion. */
    Optional<Turno> buscarPorCodigoCancelacion(String codigo);
}
