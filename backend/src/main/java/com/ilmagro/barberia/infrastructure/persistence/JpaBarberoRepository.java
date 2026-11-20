package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.catalogo.Barbero;
import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaBarberoRepository implements BarberoRepository {

    private final BarberoJpa jpa;

    JpaBarberoRepository(BarberoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Barbero> buscarTodos() {
        return jpa.findAll();
    }

    @Override
    public Optional<Barbero> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Barbero> buscarParaReservar(Long id) {
        return jpa.findByIdForUpdate(id);
    }

    @Override
    public boolean existe(Long id) {
        return jpa.existsById(id);
    }

    @Override
    public Barbero guardar(Barbero barbero) {
        return jpa.save(barbero);
    }

    @Override
    public long contar() {
        return jpa.count();
    }
}
