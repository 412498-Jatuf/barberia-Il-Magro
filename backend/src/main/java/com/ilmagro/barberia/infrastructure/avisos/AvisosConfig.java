package com.ilmagro.barberia.infrastructure.avisos;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Habilita los avisos en segundo plano (@Async). AvisosProperties la registra @ConfigurationPropertiesScan. */
@Configuration
@EnableAsync
class AvisosConfig {
}
