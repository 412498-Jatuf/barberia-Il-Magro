package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.agenda.EstadoTurno;
import com.ilmagro.barberia.domain.agenda.Turno;
import com.ilmagro.barberia.domain.agenda.TurnoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
class JpaTurnoRepository implements TurnoRepository {

    private final TurnoJpa jpa;

    JpaTurnoRepository(TurnoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Turno> confirmadosDelDia(Long barberoId, LocalDate fecha) {
        return jpa.findByBarberoIdAndFechaAndEstado(barberoId, fecha, EstadoTurno.CONFIRMADO);
    }

    @Override
    public Turno guardar(Turno turno) {
        return jpa.save(turno);
    }

    @Override
    public Optional<Turno> buscarPorCodigoCancelacion(String codigo) {
        return jpa.findByCodigoCancelacion(codigo);
    }
}
