package com.ilmagro.barberia.interfaces.rest.dto;

import com.ilmagro.barberia.application.agenda.ReservarTurnoCommand;
import com.ilmagro.barberia.application.agenda.TurnoParaCancelar;
import com.ilmagro.barberia.application.agenda.TurnoReservado;
import com.ilmagro.barberia.application.agenda.HorarioPublico;
import com.ilmagro.barberia.domain.agenda.Tramo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class AgendaDtos {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private AgendaDtos() {
    }

    public static String hhmm(LocalTime hora) {
        return hora.format(HH_MM);
    }

    /** Un tramo de horas de un dia, ej. de 15:00 a 18:00. */
    public record TramoResponse(String desde, String hasta) {
        public static TramoResponse de(Tramo t) {
            return new TramoResponse(hhmm(t.desde()), hhmm(t.hasta()));
        }
    }

    /** Un dia de la semana (1 = lunes ... 7 = domingo) con sus tramos; mas de uno significa que hay pausa. */
    public record DiaResponse(int dia, List<TramoResponse> tramos) {
    }

    /** El horario para el cliente: la semana (un dia sin tramos esta cerrado) y las fechas cerradas enteras. */
    public record HorarioResponse(int intervaloMinutos, int diasAnticipacion, List<DiaResponse> semana,
                                  List<LocalDate> cerrados) {
        public static HorarioResponse de(HorarioPublico h) {
            List<DiaResponse> semana = h.semana().entrySet().stream()
                    .map(e -> new DiaResponse(e.getKey(), e.getValue().stream().map(TramoResponse::de).toList()))
                    .toList();
            return new HorarioResponse(h.intervaloMinutos(), h.diasAnticipacion(), semana, h.cerrados());
        }
    }

    public record ReservarTurnoRequest(
            @NotBlank @Size(min = 2, max = 100) String clienteNombre,
            @NotBlank @Pattern(regexp = "^[+]?[0-9 ()-]{6,20}$", message = "telefono invalido") String clienteTelefono,
            @Email @Size(max = 150) String clienteEmail,
            @NotNull Long servicioId,
            @NotNull Long barberoId,
            @NotNull LocalDate fecha,
            @NotNull LocalTime hora,
            @Size(max = 500) String notas
    ) {
        public ReservarTurnoCommand aComando() {
            return new ReservarTurnoCommand(clienteNombre, clienteTelefono, clienteEmail, servicioId, barberoId,
                    fecha, hora, notas);
        }
    }

    public record TurnoResponse(Long id, String clienteNombre, String servicio, BigDecimal precio, String barbero,
                                LocalDate fecha, String horaInicio, String horaFin, String estado,
                                String codigoCancelacion) {
        public static TurnoResponse de(TurnoReservado t) {
            return new TurnoResponse(t.id(), t.clienteNombre(), t.servicio(), t.precio(), t.barbero(), t.fecha(),
                    hhmm(t.horaInicio()), hhmm(t.horaFin()), t.estado(), t.codigoCancelacion());
        }
    }

    /** Lo que muestra la pagina del link de cancelacion (sin telefono ni email del cliente). */
    public record CancelacionResponse(String clienteNombre, String servicio, String barbero, LocalDate fecha,
                                      String horaInicio, String horaFin, String estado, boolean cancelable) {
        public static CancelacionResponse de(TurnoParaCancelar t) {
            return new CancelacionResponse(t.clienteNombre(), t.servicio(), t.barbero(), t.fecha(),
                    hhmm(t.horaInicio()), hhmm(t.horaFin()), t.estado(), t.cancelable());
        }
    }
}
