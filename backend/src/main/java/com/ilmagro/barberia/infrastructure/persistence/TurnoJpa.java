package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.agenda.EstadoTurno;
import com.ilmagro.barberia.domain.agenda.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnoJpa extends JpaRepository<Turno, Long> {

    List<Turno> findByBarberoIdAndFechaAndEstado(Long barberoId, LocalDate fecha, EstadoTurno estado);

    Optional<Turno> findByCodigoCancelacion(String codigoCancelacion);
}
