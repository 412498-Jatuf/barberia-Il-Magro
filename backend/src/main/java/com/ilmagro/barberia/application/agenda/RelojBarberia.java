package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.ReglasDeReserva;
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
    private final ReglasDeReserva reglas;

    public RelojBarberia(Clock clock, ReglasDeReserva reglas) {
        this.clock = clock;
        this.reglas = reglas;
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock.withZone(reglas.zona()));
    }
}
