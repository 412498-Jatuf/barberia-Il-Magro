package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.agenda.TramoSemanal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TramoSemanalJpa extends JpaRepository<TramoSemanal, Long> {

    List<TramoSemanal> findAllByOrderByDiaSemanaAscDesdeAsc();
}
