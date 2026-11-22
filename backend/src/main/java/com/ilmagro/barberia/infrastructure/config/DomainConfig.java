package com.ilmagro.barberia.infrastructure.config;

import com.ilmagro.barberia.domain.agenda.AgendaService;
import com.ilmagro.barberia.domain.agenda.ReglasDeReserva;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Registra como beans los objetos de dominio, que no conocen Spring. */
@Configuration
public class DomainConfig {

    @Bean
    public ReglasDeReserva reglasDeReserva(HorarioProperties p) {
        return new ReglasDeReserva(p.intervaloMinutos(), p.diasAnticipacion(), p.zona());
    }

    @Bean
    public AgendaService agendaService(ReglasDeReserva reglas) {
        return new AgendaService(reglas);
    }

    /** Reloj inyectable para poder fijar la hora en los tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
