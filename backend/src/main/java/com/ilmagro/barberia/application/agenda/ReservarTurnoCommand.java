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
        String notas
) {
}
