package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
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

/** Cancelar por link libera el horario; un codigo inventado no encuentra nada. */
@SpringBootTest
@ActiveProfiles("test")
class CancelacionTurnosServiceTest {

    // Lunes 23/11/2026 08:00 en Buenos Aires; el turno es el jueves
    static final Instant AHORA = ZonedDateTime.of(2026, 11, 23, 8, 0, 0, 0,
            ZoneId.of("America/Argentina/Buenos_Aires")).toInstant();
    static final LocalDate JUEVES = LocalDate.of(2026, 11, 26);

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
    CancelacionTurnosService cancelaciones;
    @Autowired
    BarberoRepository barberos;
    @Autowired
    ServicioRepository servicios;

    @Test
    void cancelarPorLinkLiberaElHorario() {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long corteId = servicios.buscarTodos().getFirst().getId();
        LocalTime hora = LocalTime.of(17, 0);

        TurnoReservado turno = reservas.reservar(new ReservarTurnoCommand("Ana Gomez", "351 555 0101",
                null, corteId, barberoId, JUEVES, hora, null));
        assertThat(turno.codigoCancelacion()).hasSize(24);
        assertThat(reservas.horariosDisponibles(JUEVES, barberoId, corteId)).doesNotContain(hora);

        TurnoParaCancelar antes = cancelaciones.consultar(turno.codigoCancelacion());
        assertThat(antes.cancelable()).isTrue();

        TurnoParaCancelar despues = cancelaciones.cancelar(turno.codigoCancelacion());
        assertThat(despues.estado()).isEqualTo("CANCELADO");
        assertThat(despues.cancelable()).isFalse();
        assertThat(reservas.horariosDisponibles(JUEVES, barberoId, corteId)).contains(hora);

        // cancelar de nuevo no falla
        assertThat(cancelaciones.cancelar(turno.codigoCancelacion()).estado()).isEqualTo("CANCELADO");
    }

    @Test
    void unCodigoInventadoNoEncuentraNingunTurno() {
        assertThatThrownBy(() -> cancelaciones.cancelar("codigo-que-no-existe"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
