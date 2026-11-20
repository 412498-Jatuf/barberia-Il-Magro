package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.catalogo.Corte;
import com.ilmagro.barberia.domain.catalogo.CorteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaCorteRepository implements CorteRepository {

    private final CorteJpa jpa;

    JpaCorteRepository(CorteJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Corte> buscarTodos() {
        return jpa.findAll();
    }

    @Override
    public Corte guardar(Corte corte) {
        return jpa.save(corte);
    }

    @Override
    public long contar() {
        return jpa.count();
    }
}
