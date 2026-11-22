package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** La jornada de un dia es su horario semanal menos lo que Luca bloqueo. */
class JornadaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 24);
    private final List<Tramo> semanal = List.of(new Tramo(t(15, 0), t(22, 0)));

    @Test
    void sinBloqueosQuedaElHorarioSemanal() {
        assertThat(Jornada.de(semanal, List.of()).tramos()).containsExactly(new Tramo(t(15, 0), t(22, 0)));
    }

    @Test
    void unDiaCompletoBloqueadoDejaLaJornadaCerrada() {
        Jornada j = Jornada.de(semanal, List.of(Bloqueo.diaCompleto(DIA, "Feriado")));

        assertThat(j.cerrada()).isTrue();
    }

    @Test
    void unBloqueoDeHorasEnElMedioPartePorDosLaJornada() {
        Jornada j = Jornada.de(semanal, List.of(Bloqueo.horas(DIA, t(18, 0), t(19, 0), "Tramite")));

        assertThat(j.tramos()).containsExactly(new Tramo(t(15, 0), t(18, 0)), new Tramo(t(19, 0), t(22, 0)));
    }

    @Test
    void unBloqueoEnLaPuntaAcortaElTramoYUnoQueNoSeToca_noLoCambia() {
        Jornada j = Jornada.de(semanal, List.of(
                Bloqueo.horas(DIA, t(20, 0), t(23, 0), null),
                Bloqueo.horas(DIA, t(8, 0), t(9, 0), null)));

        assertThat(j.tramos()).containsExactly(new Tramo(t(15, 0), t(20, 0)));
    }

    @Test
    void unDiaSinHorarioSemanalQuedaCerradoAunSinBloqueos() {
        assertThat(Jornada.de(List.of(), List.of()).cerrada()).isTrue();
    }

    @Test
    void elFinDeUnTramoDebeSerPosteriorAlInicio() {
        assertThatThrownBy(() -> new Tramo(t(18, 0), t(18, 0))).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> new Tramo(t(18, 0), t(17, 0))).isInstanceOf(DatoInvalidoException.class);
    }

    private static LocalTime t(int h, int m) {
        return LocalTime.of(h, m);
    }
}
