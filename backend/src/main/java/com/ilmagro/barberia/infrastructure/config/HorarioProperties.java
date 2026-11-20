package com.ilmagro.barberia.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

/** Lee el horario de atencion de application.properties (prefijo barberia.horario). */
@ConfigurationProperties(prefix = "barberia.horario")
public record HorarioProperties(
        @DateTimeFormat(pattern = "HH:mm") LocalTime apertura,
        @DateTimeFormat(pattern = "HH:mm") LocalTime cierre,
        int intervaloMinutos,
        Set<Integer> dias,
        int diasAnticipacion,
        ZoneId zona
) {
}
