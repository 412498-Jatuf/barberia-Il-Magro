package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.catalogo.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioJpa extends JpaRepository<Servicio, Long> {
}
