package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.agenda.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BloqueoJpa extends JpaRepository<Bloqueo, Long> {

    List<Bloqueo> findByFechaBetweenOrderByFechaAscDesdeAsc(LocalDate desde, LocalDate hasta);

    List<Bloqueo> findByFechaGreaterThanEqualOrderByFechaAscDesdeAsc(LocalDate desde);
}
