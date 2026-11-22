package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Un dia de la semana con sus horas de atencion. Dos tramos significan que hay pausa; ninguno, que
 * ese dia esta cerrado. Los tramos no pueden superponerse y quedan ordenados.
 *
 * @param dia dia segun ISO-8601 (1 = lunes ... 7 = domingo)
 */
public record DiaDeAtencion(int dia, List<Tramo> tramos) {

    public static final int MAX_TRAMOS = 4;

    public DiaDeAtencion {
        if (dia < 1 || dia > 7) {
            throw new DatoInvalidoException("El dia de la semana debe estar entre 1 y 7");
        }
        if (tramos == null) {
            throw new DatoInvalidoException("Faltan los horarios del dia");
        }
        if (tramos.size() > MAX_TRAMOS) {
            throw new DatoInvalidoException("Un dia no puede tener mas de " + MAX_TRAMOS + " tramos de horario");
        }
        tramos = tramos.stream().sorted(Comparator.comparing(Tramo::desde)).toList();
        for (int i = 1; i < tramos.size(); i++) {
            if (tramos.get(i - 1).seSuperponeCon(tramos.get(i))) {
                throw new DatoInvalidoException("Los horarios del " + nombre(dia) + " se superponen");
            }
        }
    }

    public boolean cerrado() {
        return tramos.isEmpty();
    }

    /** True si el turno que va de {@code desde} a {@code hasta} cae entero dentro de alguno de los tramos. */
    public boolean contiene(Tramo turno) {
        return tramos.stream().anyMatch(t -> !turno.desde().isBefore(t.desde()) && !turno.hasta().isAfter(t.hasta()));
    }

    // estatico: se usa dentro del constructor, cuando los campos del record todavia no estan asignados
    private static String nombre(int dia) {
        return DayOfWeek.of(dia).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR"));
    }
}
