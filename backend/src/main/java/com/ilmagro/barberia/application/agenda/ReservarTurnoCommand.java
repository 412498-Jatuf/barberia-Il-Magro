package com.ilmagro.barberia.application.agenda;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservarTurnoCommand(
        String clienteNombre,
        String clienteTelefono,
        String clienteEmail,
        Long servicioId,
        Long barberoId,
        LocalDate fecha,
        LocalTime hora,
        String notas,
        /** Canal por el que el cliente eligio avisar: "email" o "whatsapp" (null = todos los activos). */
        String avisoPor
) {
    public ReservarTurnoCommand(String clienteNombre, String clienteTelefono, String clienteEmail, Long servicioId,
                                Long barberoId, LocalDate fecha, LocalTime hora, String notas) {
        this(clienteNombre, clienteTelefono, clienteEmail, servicioId, barberoId, fecha, hora, notas, null);
    }
}
