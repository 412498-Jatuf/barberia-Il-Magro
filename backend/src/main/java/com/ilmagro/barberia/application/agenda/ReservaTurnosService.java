package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.agenda.AgendaService;
import com.ilmagro.barberia.domain.agenda.Cliente;
import com.ilmagro.barberia.domain.agenda.Turno;
import com.ilmagro.barberia.domain.agenda.TurnoRepository;
import com.ilmagro.barberia.domain.catalogo.Barbero;
import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import com.ilmagro.barberia.domain.catalogo.Servicio;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso: reservar un turno. Las consultas de horarios libres estan en {@link DisponibilidadService}
 * y la cancelacion en {@link CancelacionTurnosService}.
 */
@Service
public class ReservaTurnosService {

    private final AgendaService agenda;
    private final TurnoRepository turnos;
    private final BarberoRepository barberos;
    private final ServicioRepository servicios;
    private final RelojBarberia reloj;
    private final ApplicationEventPublisher eventos;

    public ReservaTurnosService(AgendaService agenda, TurnoRepository turnos, BarberoRepository barberos,
                                ServicioRepository servicios, RelojBarberia reloj, ApplicationEventPublisher eventos) {
        this.agenda = agenda;
        this.turnos = turnos;
        this.barberos = barberos;
        this.servicios = servicios;
        this.reloj = reloj;
        this.eventos = eventos;
    }

    @Transactional
    public TurnoReservado reservar(ReservarTurnoCommand cmd) {
        Barbero barbero = barberos.buscarParaReservar(cmd.barberoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Barbero no encontrado"));
        Servicio servicio = servicios.buscarPorId(cmd.servicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
        Cliente cliente = new Cliente(cmd.clienteNombre(), cmd.clienteTelefono(), cmd.clienteEmail());
        LocalDateTime ahora = reloj.ahora();

        List<Turno> ocupados = turnos.confirmadosDelDia(barbero.getId(), cmd.fecha());
        agenda.verificarDisponible(cmd.fecha(), cmd.hora(), servicio.getDuracionMinutos(), ocupados, ahora);

        Turno turno = turnos.guardar(
                Turno.reservar(cliente, servicio, barbero, cmd.fecha(), cmd.hora(), cmd.notas(), ahora));
        // los avisos a la barberia salen recien cuando la transaccion se confirma (AvisosListener)
        eventos.publishEvent(TurnoConfirmado.de(turno));
        return TurnoReservado.de(turno);
    }
}
