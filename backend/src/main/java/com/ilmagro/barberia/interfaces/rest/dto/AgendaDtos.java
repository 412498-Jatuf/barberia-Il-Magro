package com.ilmagro.barberia.interfaces.rest.dto;

import com.ilmagro.barberia.application.agenda.ReservarTurnoCommand;
import com.ilmagro.barberia.application.agenda.TurnoParaCancelar;
import com.ilmagro.barberia.application.agenda.TurnoReservado;
import com.ilmagro.barberia.domain.agenda.HorarioAtencion;
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

    public record HorarioResponse(String apertura, String cierre, int intervaloMinutos,
                                  List<Integer> diasAtencion, int diasAnticipacion) {
        public static HorarioResponse de(HorarioAtencion h) {
            return new HorarioResponse(hhmm(h.apertura()), hhmm(h.cierre()), h.intervaloMinutos(),
                    h.diasAtencion().stream().sorted().toList(), h.diasAnticipacion());
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
