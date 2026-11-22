package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.agenda.AgendaService;
import com.ilmagro.barberia.domain.agenda.HorarioAtencion;
import com.ilmagro.barberia.domain.agenda.Turno;
import com.ilmagro.barberia.domain.agenda.TurnoRepository;
import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import com.ilmagro.barberia.domain.catalogo.Servicio;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Casos de uso de consulta de la agenda: el horario de atencion y los horarios libres de un dia. */
@Service
@Transactional(readOnly = true)
public class DisponibilidadService {

    private final AgendaService agenda;
    private final TurnoRepository turnos;
    private final BarberoRepository barberos;
    private final ServicioRepository servicios;
    private final RelojBarberia reloj;

    public DisponibilidadService(AgendaService agenda, TurnoRepository turnos, BarberoRepository barberos,
                                 ServicioRepository servicios, RelojBarberia reloj) {
        this.agenda = agenda;
        this.turnos = turnos;
        this.barberos = barberos;
        this.servicios = servicios;
        this.reloj = reloj;
    }

    public HorarioAtencion horarioDeAtencion() {
        return agenda.horario();
    }

    public List<LocalTime> horariosDisponibles(LocalDate fecha, Long barberoId, Long servicioId) {
        if (!barberos.existe(barberoId)) {
            throw new RecursoNoEncontradoException("Barbero no encontrado");
        }
        Servicio servicio = servicios.buscarPorId(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
        List<Turno> ocupados = turnos.confirmadosDelDia(barberoId, fecha);
        return agenda.horariosLibres(fecha, servicio.getDuracionMinutos(), ocupados, reloj.ahora());
    }
}
