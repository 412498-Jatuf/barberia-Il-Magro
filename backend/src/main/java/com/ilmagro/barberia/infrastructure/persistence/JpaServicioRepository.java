package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.catalogo.Servicio;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaServicioRepository implements ServicioRepository {

    private final ServicioJpa jpa;

    JpaServicioRepository(ServicioJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Servicio> buscarTodos() {
        return jpa.findAll();
    }

    @Override
    public Optional<Servicio> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Servicio guardar(Servicio servicio) {
        return jpa.save(servicio);
    }

    @Override
    public long contar() {
        return jpa.count();
    }
}
