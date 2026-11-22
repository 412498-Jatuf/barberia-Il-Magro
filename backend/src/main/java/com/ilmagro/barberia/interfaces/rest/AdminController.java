package com.ilmagro.barberia.interfaces.rest;

import com.ilmagro.barberia.application.agenda.AdministracionAgendaService;
import com.ilmagro.barberia.application.agenda.AdministracionTurnosService;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos.BloqueoCreadoResponse;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos.BloqueoRequest;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos.BloqueoResponse;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos.SemanaGuardadaResponse;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos.SemanaRequest;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos.TurnoAdminResponse;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.DiaResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Panel de administracion de Luca. Todo lo que cuelga de /api/admin pide sesion iniciada (ver SeguridadConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdministracionTurnosService turnos;
    private final AdministracionAgendaService agenda;

    public AdminController(AdministracionTurnosService turnos, AdministracionAgendaService agenda) {
        this.turnos = turnos;
        this.agenda = agenda;
    }

    /** Turnos del rango (sin fechas: hoy y los proximos 14 dias), confirmados y cancelados. */
    @GetMapping("/turnos")
    public List<TurnoAdminResponse> turnos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return turnos.turnos(desde, hasta).stream().map(TurnoAdminResponse::de).toList();
    }

    /** Cancela el turno y libera el horario. */
    @PostMapping("/turnos/{id}/cancelar")
    public TurnoAdminResponse cancelar(@PathVariable Long id) {
        return TurnoAdminResponse.de(turnos.cancelar(id));
    }

    @GetMapping("/semana")
    public List<DiaResponse> semana() {
        return agenda.semana().stream().map(AdminDtos::dia).toList();
    }

    /** Reemplaza el horario semanal. No cancela turnos: devuelve los que quedaron fuera del horario nuevo. */
    @PutMapping("/semana")
    public SemanaGuardadaResponse guardarSemana(@Valid @RequestBody SemanaRequest request) {
        return SemanaGuardadaResponse.de(agenda.guardarSemana(request.aDominio()));
    }

    @GetMapping("/bloqueos")
    public List<BloqueoResponse> bloqueos() {
        return agenda.bloqueos().stream().map(BloqueoResponse::de).toList();
    }

    /** Cierra un dia entero (sin horas) o unas horas de un dia. Devuelve los turnos reservados que caen ahi. */
    @PostMapping("/bloqueos")
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueoCreadoResponse bloquear(@Valid @RequestBody BloqueoRequest request) {
        return BloqueoCreadoResponse.de(
                agenda.bloquear(request.fecha(), request.desde(), request.hasta(), request.motivo()));
    }

    @DeleteMapping("/bloqueos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarBloqueo(@PathVariable Long id) {
        agenda.quitarBloqueo(id);
    }
}
