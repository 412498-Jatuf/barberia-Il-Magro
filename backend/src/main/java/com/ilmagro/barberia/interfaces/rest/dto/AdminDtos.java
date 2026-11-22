package com.ilmagro.barberia.interfaces.rest.dto;

import com.ilmagro.barberia.application.agenda.BloqueoAdmin;
import com.ilmagro.barberia.application.agenda.BloqueoCreado;
import com.ilmagro.barberia.application.agenda.SemanaGuardada;
import com.ilmagro.barberia.application.agenda.TurnoAdmin;
import com.ilmagro.barberia.domain.agenda.DiaDeAtencion;
import com.ilmagro.barberia.domain.agenda.Tramo;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.DiaResponse;
import com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.TramoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.ilmagro.barberia.interfaces.rest.dto.AgendaDtos.hhmm;

/** Pedidos y respuestas del panel de administracion (/api/admin). */
public final class AdminDtos {

    private AdminDtos() {
    }

    public record TurnoAdminResponse(Long id, String clienteNombre, String clienteTelefono, String clienteEmail,
                                     String servicio, int duracionMinutos, String barbero, LocalDate fecha,
                                     String horaInicio, String horaFin, String estado, String notas,
                                     boolean cancelable) {
        public static TurnoAdminResponse de(TurnoAdmin t) {
            return new TurnoAdminResponse(t.id(), t.clienteNombre(), t.clienteTelefono(), t.clienteEmail(),
                    t.servicio(), t.duracionMinutos(), t.barbero(), t.fecha(), hhmm(t.horaInicio()),
                    hhmm(t.horaFin()), t.estado(), t.notas(), t.cancelable());
        }
    }

    public record BloqueoResponse(Long id, LocalDate fecha, String desde, String hasta, String motivo,
                                  boolean diaCompleto) {
        public static BloqueoResponse de(BloqueoAdmin b) {
            return new BloqueoResponse(b.id(), b.fecha(), b.desde() == null ? null : hhmm(b.desde()),
                    b.hasta() == null ? null : hhmm(b.hasta()), b.motivo(), b.diaCompleto());
        }
    }

    public static DiaResponse dia(DiaDeAtencion d) {
        return new DiaResponse(d.dia(), d.tramos().stream().map(TramoResponse::de).toList());
    }

    public record TramoRequest(@NotNull LocalTime desde, @NotNull LocalTime hasta) {
        Tramo aDominio() {
            return new Tramo(desde, hasta);
        }
    }

    public record DiaRequest(@NotNull @Min(1) @Max(7) Integer dia,
                             @NotNull @Size(max = DiaDeAtencion.MAX_TRAMOS) List<@Valid @NotNull TramoRequest> tramos) {
        DiaDeAtencion aDominio() {
            return new DiaDeAtencion(dia, tramos.stream().map(TramoRequest::aDominio).toList());
        }
    }

    public record SemanaRequest(@NotNull @Size(max = 7) List<@Valid @NotNull DiaRequest> dias) {
        public List<DiaDeAtencion> aDominio() {
            return dias.stream().map(DiaRequest::aDominio).toList();
        }
    }

    public record SemanaGuardadaResponse(List<DiaResponse> semana, List<TurnoAdminResponse> turnosAfectados) {
        public static SemanaGuardadaResponse de(SemanaGuardada s) {
            return new SemanaGuardadaResponse(s.semana().stream().map(AdminDtos::dia).toList(),
                    s.turnosAfectados().stream().map(TurnoAdminResponse::de).toList());
        }
    }

    /** Sin horas: se cierra el dia entero. */
    public record BloqueoRequest(@NotNull LocalDate fecha, LocalTime desde, LocalTime hasta,
                                 @Size(max = 200) String motivo) {
    }

    public record BloqueoCreadoResponse(BloqueoResponse bloqueo, List<TurnoAdminResponse> turnosAfectados) {
        public static BloqueoCreadoResponse de(BloqueoCreado c) {
            return new BloqueoCreadoResponse(BloqueoResponse.de(c.bloqueo()),
                    c.turnosAfectados().stream().map(TurnoAdminResponse::de).toList());
        }
    }
}
