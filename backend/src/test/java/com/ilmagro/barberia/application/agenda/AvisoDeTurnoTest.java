package com.ilmagro.barberia.application.agenda;

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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Al confirmar un turno se le avisa a la barberia, con los datos del turno, despues del commit. */
@SpringBootTest
@ActiveProfiles("test")
class AvisoDeTurnoTest {

    // Lunes 23/11/2026 08:00 en Buenos Aires; el turno es el miercoles
    static final Instant AHORA = ZonedDateTime.of(2026, 11, 23, 8, 0, 0, 0,
            ZoneId.of("America/Argentina/Buenos_Aires")).toInstant();
    static final LocalDate MIERCOLES = LocalDate.of(2026, 11, 25);

    static final List<TurnoConfirmado> recibidos = new CopyOnWriteArrayList<>();
    static final CountDownLatch llego = new CountDownLatch(1);

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        Clock relojFijo() {
            return Clock.fixed(AHORA, ZoneOffset.UTC);
        }

        @Bean
        AvisoDeTurno avisoDePrueba() {
            return new AvisoDeTurno() {
                public String canal() { return "prueba"; }
                public void avisar(TurnoConfirmado turno) {
                    recibidos.add(turno);
                    llego.countDown();
                }
            };
        }
    }

    @Autowired
    ReservaTurnosService reservas;
    @Autowired
    BarberoRepository barberos;
    @Autowired
    ServicioRepository servicios;

    @Test
    void avisaALaBarberiaCuandoSeConfirmaUnTurno() throws InterruptedException {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long corteId = servicios.buscarTodos().getFirst().getId();

        TurnoReservado turno = reservas.reservar(new ReservarTurnoCommand("Ana Gomez", "351 555 0101",
                null, corteId, barberoId, MIERCOLES, LocalTime.of(20, 0), "Fade bajo"));

        assertThat(llego.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(recibidos).singleElement().satisfies(t -> {
            assertThat(t.id()).isEqualTo(turno.id());
            assertThat(t.clienteNombre()).isEqualTo("Ana Gomez");
            assertThat(t.clienteTelefono()).isEqualTo("351 555 0101");
            assertThat(t.fecha()).isEqualTo(MIERCOLES);
            assertThat(t.horaInicio()).isEqualTo(LocalTime.of(20, 0));
            assertThat(t.notas()).isEqualTo("Fade bajo");
        });
    }
}
