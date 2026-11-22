package com.ilmagro.barberia.domain.agenda;

import java.util.Comparator;
import java.util.List;

/**
 * Las horas en las que la barberia atiende un dia concreto: el horario de ese dia de la semana menos
 * lo que Luca bloqueo (un dia entero cerrado o unas horas). Sin tramos, ese dia no se atiende.
 */
public record Jornada(List<Tramo> tramos) {

    public Jornada {
        tramos = List.copyOf(tramos);
    }

    public static Jornada de(List<Tramo> delDiaDeLaSemana, List<Bloqueo> bloqueos) {
        List<Tramo> actuales = delDiaDeLaSemana.stream().sorted(Comparator.comparing(Tramo::desde)).toList();
        for (Bloqueo bloqueo : bloqueos) {
            actuales = actuales.stream().flatMap(t -> t.restar(bloqueo.tramo()).stream()).toList();
        }
        return new Jornada(actuales);
    }

    public boolean cerrada() {
        return tramos.isEmpty();
    }
}
