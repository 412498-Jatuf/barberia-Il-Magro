package com.ilmagro.barberia.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

/** Lee las reglas fijas de reserva de application.properties (prefijo barberia.horario). */
@ConfigurationProperties(prefix = "barberia.horario")
public record HorarioProperties(
        int intervaloMinutos,
        int diasAnticipacion,
        ZoneId zona
) {
}
