package com.ilmagro.barberia.domain.agenda;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository {

    /** Turnos confirmados de un barbero en un dia. */
    List<Turno> confirmadosDelDia(Long barberoId, LocalDate fecha);

    Turno guardar(Turno turno);

    /** El turno del link de cancelacion. */
    Optional<Turno> buscarPorCodigoCancelacion(String codigo);
}
