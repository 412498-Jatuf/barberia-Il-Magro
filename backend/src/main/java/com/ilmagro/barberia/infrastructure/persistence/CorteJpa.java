package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.catalogo.Corte;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data: detalle tecnico, solo lo usa el adaptador {@link JpaCorteRepository}. */
public interface CorteJpa extends JpaRepository<Corte, Long> {
}
