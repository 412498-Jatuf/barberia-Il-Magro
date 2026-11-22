package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.HorarioAtencion;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * La fecha y hora actual en la zona horaria de la barberia (Cordoba), que es la que importa para
 * saber si un horario ya paso. Un solo lugar para eso, y en los tests se fija la hora con el Clock.
 */
@Component
public class RelojBarberia {

    private final Clock clock;
    private final HorarioAtencion horario;

    public RelojBarberia(Clock clock, HorarioAtencion horario) {
        this.clock = clock;
        this.horario = horario;
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock.withZone(horario.zona()));
    }
}
