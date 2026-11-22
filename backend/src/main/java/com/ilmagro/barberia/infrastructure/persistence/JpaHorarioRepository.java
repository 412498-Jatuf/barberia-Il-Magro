package com.ilmagro.barberia.infrastructure.persistence;

import com.ilmagro.barberia.domain.agenda.Bloqueo;
import com.ilmagro.barberia.domain.agenda.HorarioRepository;
import com.ilmagro.barberia.domain.agenda.TramoSemanal;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Override
    public void reemplazarSemana(List<TramoSemanal> nuevos) {
        tramos.deleteAllInBatch();
        tramos.saveAll(nuevos);
    }

    @Override
    public List<Bloqueo> bloqueosDesde(LocalDate desde) {
        return bloqueos.findByFechaGreaterThanEqualOrderByFechaAscDesdeAsc(desde);
    }

    @Override
    public Bloqueo guardarBloqueo(Bloqueo bloqueo) {
        return bloqueos.save(bloqueo);
    }

    @Override
    public Optional<Bloqueo> buscarBloqueo(Long id) {
        return bloqueos.findById(id);
    }

    @Override
    public void eliminarBloqueo(Bloqueo bloqueo) {
        bloqueos.delete(bloqueo);
    }
}
