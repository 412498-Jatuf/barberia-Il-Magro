package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.TurnoNoDisponibleException;
import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
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

/** Caso de uso completo contra una base H2 en memoria con los datos iniciales. */
@SpringBootTest
@ActiveProfiles("test")
class ReservaTurnosServiceTest {

    // Lunes 23/11/2026 08:00 en Buenos Aires
    static final Instant AHORA = ZonedDateTime.of(2026, 11, 23, 8, 0, 0, 0,
            ZoneId.of("America/Argentina/Buenos_Aires")).toInstant();
    static final LocalDate MARTES = LocalDate.of(2026, 11, 24);

    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        Clock relojFijo() {
            return Clock.fixed(AHORA, ZoneOffset.UTC);
        }
    }

    @Autowired
    ReservaTurnosService reservas;
    @Autowired
    BarberoRepository barberos;
    @Autowired
    ServicioRepository servicios;

    @Test
    void reservaUnTurnoYElHorarioDejaDeEstarDisponible() {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long comboId = servicios.buscarTodos().stream()
                .filter(s -> s.getDuracionMinutos() == 60).findFirst().orElseThrow().getId();

        TurnoReservado turno = reservas.reservar(new ReservarTurnoCommand("Juan Perez", "11 2233-4455",
                "juan@example.com", comboId, barberoId, MARTES, LocalTime.of(18, 0), null));

        assertThat(turno.id()).isNotNull();
        assertThat(turno.horaFin()).isEqualTo(LocalTime.of(19, 0));
        assertThat(reservas.horariosDisponibles(MARTES, barberoId, comboId))
                .doesNotContain(LocalTime.of(17, 30), LocalTime.of(18, 0), LocalTime.of(18, 30))
                .contains(LocalTime.of(17, 0), LocalTime.of(19, 0));

        assertThatThrownBy(() -> reservas.reservar(new ReservarTurnoCommand("Otro", "1199998888", null,
                comboId, barberoId, MARTES, LocalTime.of(18, 30), null)))
                .isInstanceOf(TurnoNoDisponibleException.class);
    }

    @Test
    void rechazaDatosDeClienteInvalidos() {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long servicioId = servicios.buscarTodos().getFirst().getId();

        assertThatThrownBy(() -> reservas.reservar(new ReservarTurnoCommand("J", "abc", null,
                servicioId, barberoId, MARTES, LocalTime.of(15, 0), null)))
                .isInstanceOf(DatoInvalidoException.class);
    }
}
