package com.ilmagro.barberia.infrastructure.config;

import com.ilmagro.barberia.domain.agenda.AgendaService;
import com.ilmagro.barberia.domain.agenda.HorarioAtencion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Registra como beans los objetos de dominio, que no conocen Spring. */
@Configuration
public class DomainConfig {

    @Bean
    public HorarioAtencion horarioAtencion(HorarioProperties p) {
        return new HorarioAtencion(p.apertura(), p.cierre(), p.intervaloMinutos(), p.dias(),
                p.diasAnticipacion(), p.zona());
    }

    @Bean
    public AgendaService agendaService(HorarioAtencion horario) {
        return new AgendaService(horario);
    }

    /** Reloj inyectable para poder fijar la hora en los tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
