package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.agenda.*;
import com.ilmagro.barberia.domain.catalogo.Barbero;
import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import com.ilmagro.barberia.domain.catalogo.Servicio;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Casos de uso de la agenda: consultar horarios libres y reservar un turno. */
@Service
public class ReservaTurnosService {

    private final AgendaService agenda;
    private final TurnoRepository turnos;
    private final BarberoRepository barberos;
    private final ServicioRepository servicios;
    private final Clock clock;
    private final ApplicationEventPublisher eventos;

    public ReservaTurnosService(AgendaService agenda, TurnoRepository turnos, BarberoRepository barberos,
                                ServicioRepository servicios, Clock clock, ApplicationEventPublisher eventos) {
        this.agenda = agenda;
        this.turnos = turnos;
        this.barberos = barberos;
        this.servicios = servicios;
        this.clock = clock;
        this.eventos = eventos;
    }

    public HorarioAtencion horarioDeAtencion() {
        return agenda.horario();
    }

    @Transactional(readOnly = true)
    public List<LocalTime> horariosDisponibles(LocalDate fecha, Long barberoId, Long servicioId) {
        if (!barberos.existe(barberoId)) {
            throw new RecursoNoEncontradoException("Barbero no encontrado");
        }
        Servicio servicio = buscarServicio(servicioId);
        List<Turno> ocupados = turnos.confirmadosDelDia(barberoId, fecha);
        return agenda.horariosLibres(fecha, servicio.getDuracionMinutos(), ocupados, ahora());
    }

    @Transactional
    public TurnoReservado reservar(ReservarTurnoCommand cmd) {
        Barbero barbero = barberos.buscarParaReservar(cmd.barberoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Barbero no encontrado"));
        Servicio servicio = buscarServicio(cmd.servicioId());
        Cliente cliente = new Cliente(cmd.clienteNombre(), cmd.clienteTelefono(), cmd.clienteEmail());
        LocalDateTime ahora = ahora();

        List<Turno> ocupados = turnos.confirmadosDelDia(barbero.getId(), cmd.fecha());
        agenda.verificarDisponible(cmd.fecha(), cmd.hora(), servicio.getDuracionMinutos(), ocupados, ahora);

        Turno turno = turnos.guardar(
                Turno.reservar(cliente, servicio, barbero, cmd.fecha(), cmd.hora(), cmd.notas(), ahora));
        // los avisos a la barberia salen recien cuando la transaccion se confirma (AvisosListener)
        eventos.publishEvent(TurnoConfirmado.de(turno));
        return TurnoReservado.de(turno);
    }

    private Servicio buscarServicio(Long servicioId) {
        return servicios.buscarPorId(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock.withZone(agenda.horario().zona()));
    }
}
