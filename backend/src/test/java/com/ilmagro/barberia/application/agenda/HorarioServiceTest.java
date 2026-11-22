package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Bloqueo;
import com.ilmagro.barberia.domain.agenda.TurnoNoDisponibleException;
import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import com.ilmagro.barberia.infrastructure.persistence.BloqueoJpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Lo que Luca cierra (un dia entero o unas horas) deja de poder reservarse, con la base real. */
@SpringBootTest
@ActiveProfiles("test")
class HorarioServiceTest {

    // Lunes 23/11/2026 08:00 en Buenos Aires; los cierres son el viernes (se atiende de 15 a 22)
    static final Instant AHORA = ZonedDateTime.of(2026, 11, 23, 8, 0, 0, 0,
            ZoneId.of("America/Argentina/Buenos_Aires")).toInstant();
    static final LocalDate VIERNES = LocalDate.of(2026, 11, 27);

    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        Clock relojFijo() {
            return Clock.fixed(AHORA, ZoneOffset.UTC);
        }
    }

    @Autowired
    DisponibilidadService disponibilidad;
    @Autowired
    ReservaTurnosService reservas;
    @Autowired
    HorarioService horario;
    @Autowired
    BloqueoJpa bloqueos;
    @Autowired
    BarberoRepository barberos;
    @Autowired
    ServicioRepository servicios;

    @AfterEach
    void limpiar() {
        bloqueos.deleteAll();
    }

    @Test
    void unDiaCerradoNoTieneHorariosNiSePuedeReservar() {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long corteId = servicios.buscarTodos().getFirst().getId();
        assertThat(disponibilidad.horariosDisponibles(VIERNES, barberoId, corteId)).isNotEmpty();

        bloqueos.save(Bloqueo.diaCompleto(VIERNES, "Feriado"));

        assertThat(disponibilidad.horariosDisponibles(VIERNES, barberoId, corteId)).isEmpty();
        assertThat(horario.horarioPublico().cerrados()).containsExactly(VIERNES);
        assertThatThrownBy(() -> reservas.reservar(new ReservarTurnoCommand("Ana Gomez", "351 555 0101", null,
                corteId, barberoId, VIERNES, LocalTime.of(16, 0), null)))
                .isInstanceOf(TurnoNoDisponibleException.class);
    }

    @Test
    void unBloqueoDeHorasSoloSacaEsasHoras() {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long corteId = servicios.buscarTodos().getFirst().getId();

        bloqueos.save(Bloqueo.horas(VIERNES, LocalTime.of(18, 0), LocalTime.of(20, 0), "Tramite"));

        assertThat(disponibilidad.horariosDisponibles(VIERNES, barberoId, corteId))
                .contains(LocalTime.of(17, 30), LocalTime.of(20, 0))
                .doesNotContain(LocalTime.of(18, 0), LocalTime.of(18, 30), LocalTime.of(19, 0), LocalTime.of(19, 30));
        // no cuenta como dia cerrado
        assertThat(horario.horarioPublico().cerrados()).isEmpty();
    }
}
