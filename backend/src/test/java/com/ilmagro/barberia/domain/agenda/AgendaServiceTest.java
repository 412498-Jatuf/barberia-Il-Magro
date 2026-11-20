package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.catalogo.Barbero;
import com.ilmagro.barberia.domain.catalogo.Servicio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgendaServiceTest {

    // Martes a sabado, 10 a 13 hs, turnos cada 30 min
    private final HorarioAtencion horario = new HorarioAtencion(LocalTime.of(10, 0), LocalTime.of(13, 0), 30,
            Set.of(2, 3, 4, 5, 6), 30, ZoneId.of("America/Argentina/Buenos_Aires"));
    private final AgendaService agenda = new AgendaService(horario);

    private final LocalDate martes = LocalDate.of(2026, 11, 24);
    private final LocalDateTime lunesAnterior = LocalDateTime.of(2026, 11, 23, 9, 0);
    private final Servicio corte = new Servicio("Corte", null, 30, new BigDecimal("100"));
    private final Servicio combo = new Servicio("Corte + Barba", null, 60, new BigDecimal("150"));
    private final Barbero barbero = new Barbero("Test", null, null);
    private final Cliente cliente = new Cliente("Juan", "1122334455", null);

    @Test
    void diaLibreDevuelveTodosLosHorariosQueEntranAntesDelCierre() {
        assertThat(agenda.horariosLibres(martes, 60, List.of(), lunesAnterior))
                .containsExactly(t(10, 0), t(10, 30), t(11, 0), t(11, 30), t(12, 0));
    }

    @Test
    void unTurnoOcupadoBloqueaLosHorariosQueSeSuperponen() {
        Turno ocupado = Turno.reservar(cliente, corte, barbero, martes, t(11, 0), null, lunesAnterior);

        assertThat(agenda.horariosLibres(martes, 60, List.of(ocupado), lunesAnterior))
                .containsExactly(t(10, 0), t(11, 30), t(12, 0));
    }

    @Test
    void unTurnoCanceladoLiberaElHorario() {
        Turno cancelado = Turno.reservar(cliente, combo, barbero, martes, t(10, 0), null, lunesAnterior);
        cancelado.cancelar(lunesAnterior);

        assertThat(agenda.horariosLibres(martes, 30, List.of(cancelado), lunesAnterior)).contains(t(10, 0), t(10, 30));
    }

    @Test
    void hoyNoOfreceHorariosQueYaPasaron() {
        LocalDateTime martesMediodia = martes.atTime(11, 15);

        assertThat(agenda.horariosLibres(martes, 30, List.of(), martesMediodia))
                .containsExactly(t(11, 30), t(12, 0), t(12, 30));
    }

    @Test
    void diasSinAtencionPasadosOFueraDeLaVentanaNoTienenHorarios() {
        assertThat(agenda.horariosLibres(martes.minusDays(2), 30, List.of(), lunesAnterior)).isEmpty(); // domingo
        assertThat(agenda.horariosLibres(martes.minusWeeks(1), 30, List.of(), lunesAnterior)).isEmpty(); // pasado
        assertThat(agenda.horariosLibres(martes.plusDays(35), 30, List.of(), lunesAnterior)).isEmpty(); // muy lejos
    }

    @Test
    void verificarDisponibleRechazaHorarioOcupadoOFueraDeGrilla() {
        Turno ocupado = Turno.reservar(cliente, corte, barbero, martes, t(10, 0), null, lunesAnterior);

        assertThatThrownBy(() -> agenda.verificarDisponible(martes, t(10, 0), 30, List.of(ocupado), lunesAnterior))
                .isInstanceOf(TurnoNoDisponibleException.class);
        assertThatThrownBy(() -> agenda.verificarDisponible(martes, t(10, 15), 30, List.of(), lunesAnterior))
                .isInstanceOf(TurnoNoDisponibleException.class);
    }

    private static LocalTime t(int h, int m) {
        return LocalTime.of(h, m);
    }
}
