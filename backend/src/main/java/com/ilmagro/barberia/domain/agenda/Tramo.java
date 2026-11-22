package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Un intervalo de horas del dia, [desde, hasta). Ej.: de 15:00 a 18:00. */
public record Tramo(LocalTime desde, LocalTime hasta) {

    public Tramo {
        if (desde == null || hasta == null || !hasta.isAfter(desde)) {
            throw new DatoInvalidoException("El horario de fin debe ser posterior al de inicio");
        }
    }

    public boolean seSuperponeCon(Tramo otro) {
        return desde.isBefore(otro.hasta) && otro.desde.isBefore(hasta);
    }

    /** Lo que queda de este tramo al sacarle {@code otro}: nada, el mismo, una parte o dos (si cae en el medio). */
    public List<Tramo> restar(Tramo otro) {
        if (!seSuperponeCon(otro)) {
            return List.of(this);
        }
        List<Tramo> resto = new ArrayList<>(2);
        if (desde.isBefore(otro.desde)) {
            resto.add(new Tramo(desde, otro.desde));
        }
        if (otro.hasta.isBefore(hasta)) {
            resto.add(new Tramo(otro.hasta, hasta));
        }
        return resto;
    }
}
