package com.ilmagro.barberia.interfaces.rest;

import com.ilmagro.barberia.application.agenda.CancelacionTurnosService;
import com.ilmagro.barberia.application.agenda.DisponibilidadService;
import com.ilmagro.barberia.application.agenda.ReservaTurnosService;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.CancelacionResponse;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.HorarioResponse;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.ReservarTurnoRequest;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.TurnoResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TurnoController {

    private final DisponibilidadService disponibilidad;
    private final ReservaTurnosService reservas;
    private final CancelacionTurnosService cancelaciones;

    public TurnoController(DisponibilidadService disponibilidad, ReservaTurnosService reservas,
                           CancelacionTurnosService cancelaciones) {
        this.disponibilidad = disponibilidad;
        this.reservas = reservas;
        this.cancelaciones = cancelaciones;
    }

    @GetMapping("/horario")
    public HorarioResponse horario() {
        return HorarioResponse.de(disponibilidad.horarioDeAtencion());
    }

    /** Horarios de inicio libres, en formato HH:mm. */
    @GetMapping("/turnos/disponibles")
    public List<String> disponibles(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                    @RequestParam Long barberoId,
                                    @RequestParam Long servicioId) {
        return disponibilidad.horariosDisponibles(fecha, barberoId, servicioId).stream().map(AgendaDtos::hhmm).toList();
    }

    @PostMapping("/turnos")
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoResponse reservar(@Valid @RequestBody ReservarTurnoRequest request) {
        return TurnoResponse.de(reservas.reservar(request.aComando()));
    }

    /** El turno del link de cancelacion, para mostrarlo antes de cancelar. */
    @GetMapping("/turnos/cancelacion/{codigo}")
    public CancelacionResponse turnoACancelar(@PathVariable String codigo) {
        return CancelacionResponse.de(cancelaciones.consultar(codigo));
    }

    /** Cancela el turno del link y libera el horario. */
    @PostMapping("/turnos/cancelacion/{codigo}")
    public CancelacionResponse cancelar(@PathVariable String codigo) {
        return CancelacionResponse.de(cancelaciones.cancelar(codigo));
    }
}
