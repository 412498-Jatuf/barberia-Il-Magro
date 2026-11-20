package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.catalogo.Barbero;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BarberoJpa extends JpaRepository<Barbero, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Barbero b where b.id = :id")
    Optional<Barbero> findByIdForUpdate(@Param("id") Long id);
}
