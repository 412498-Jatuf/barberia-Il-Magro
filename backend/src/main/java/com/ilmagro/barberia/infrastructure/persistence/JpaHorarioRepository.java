package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.agenda.Bloqueo;
import com.ilmagro.barberia.domain.agenda.HorarioRepository;
import com.ilmagro.barberia.domain.agenda.TramoSemanal;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
class JpaHorarioRepository implements HorarioRepository {

    private final TramoSemanalJpa tramos;
    private final BloqueoJpa bloqueos;

    JpaHorarioRepository(TramoSemanalJpa tramos, BloqueoJpa bloqueos) {
        this.tramos = tramos;
        this.bloqueos = bloqueos;
    }

    @Override
    public List<TramoSemanal> semana() {
        return tramos.findAllByOrderByDiaSemanaAscDesdeAsc();
    }

    @Override
    public List<Bloqueo> bloqueosEntre(LocalDate desde, LocalDate hasta) {
        return bloqueos.findByFechaBetweenOrderByFechaAscDesdeAsc(desde, hasta);
    }
}
