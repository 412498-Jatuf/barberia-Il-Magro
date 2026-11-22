package com.ilmagro.barberia.domain.agenda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio: calcula que horarios estan libres en la agenda de un barbero.
 * No depende de Spring ni de la base de datos, por eso se testea con tests unitarios puros.
 * Recibe la {@link Jornada} del dia ya resuelta: no sabe de donde sale el horario.
 */
public class AgendaService {

    private final ReglasDeReserva reglas;

    public AgendaService(ReglasDeReserva reglas) {
        this.reglas = reglas;
    }

    public ReglasDeReserva reglas() {
        return reglas;
    }

    /**
     * @param jornada  las horas que se atienden ese dia
     * @param ocupados turnos ya tomados por el barbero ese dia
     * @param ahora    fecha y hora actual en la zona de la barberia
     */
    public List<LocalTime> horariosLibres(LocalDate fecha, int duracionMinutos, Jornada jornada,
                                          List<Turno> ocupados, LocalDateTime ahora) {
        if (!reglas.admiteReservas(fecha, ahora.toLocalDate())) {
            return List.of();
        }
        boolean esHoy = fecha.equals(ahora.toLocalDate());

        List<LocalTime> libres = new ArrayList<>();
        for (Tramo tramo : jornada.tramos()) {
            int cierre = minutosDelDia(tramo.hasta());
            for (int inicio = minutosDelDia(tramo.desde()); inicio + duracionMinutos <= cierre;
                 inicio += reglas.intervaloMinutos()) {
                LocalTime desde = LocalTime.of(inicio / 60, inicio % 60);
                LocalTime hasta = desde.plusMinutes(duracionMinutos);
                boolean yaPaso = esHoy && !desde.isAfter(ahora.toLocalTime());
                boolean ocupado = ocupados.stream().anyMatch(t -> t.ocupa(desde, hasta));
                if (!yaPaso && !ocupado) {
                    libres.add(desde);
                }
            }
        }
        return libres;
    }

    /** Lanza {@link TurnoNoDisponibleException} si el horario pedido no esta libre. */
    public void verificarDisponible(LocalDate fecha, LocalTime hora, int duracionMinutos, Jornada jornada,
                                    List<Turno> ocupados, LocalDateTime ahora) {
        if (!horariosLibres(fecha, duracionMinutos, jornada, ocupados, ahora).contains(hora)) {
            throw new TurnoNoDisponibleException("El horario elegido ya no esta disponible. Elegi otro, por favor.");
        }
    }

    private static int minutosDelDia(LocalTime hora) {
        return hora.getHour() * 60 + hora.getMinute();
    }
}
