package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.agenda.Turno;
import com.ilmagro.barberia.domain.agenda.TurnoRepository;
import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Lo que Luca hace con los turnos desde el panel: verlos y cancelarlos para liberar el horario. */
@Service
public class AdministracionTurnosService {

    private static final int DIAS_POR_DEFECTO = 14;
    private static final int DIAS_MAXIMOS = 90;

    private final TurnoRepository turnos;
    private final RelojBarberia reloj;

    public AdministracionTurnosService(TurnoRepository turnos, RelojBarberia reloj) {
        this.turnos = turnos;
        this.reloj = reloj;
    }

    /** Los turnos del rango, en orden. Sin fechas: desde hoy y los proximos 14 dias. */
    @Transactional(readOnly = true)
    public List<TurnoAdmin> turnos(LocalDate desde, LocalDate hasta) {
        LocalDateTime ahora = reloj.ahora();
        LocalDate inicio = desde != null ? desde : ahora.toLocalDate();
        LocalDate fin = hasta != null ? hasta : inicio.plusDays(DIAS_POR_DEFECTO);
        if (fin.isBefore(inicio)) {
            throw new DatoInvalidoException("La fecha final no puede ser anterior a la inicial");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) > DIAS_MAXIMOS) {
            throw new DatoInvalidoException("Se pueden ver como mucho " + DIAS_MAXIMOS + " dias a la vez");
        }
        return turnos.turnosEntre(inicio, fin).stream().map(t -> TurnoAdmin.de(t, ahora)).toList();
    }

    /** Cancela el turno y libera su horario. Cancelar uno ya cancelado no hace nada; uno que ya paso no se puede. */
    @Transactional
    public TurnoAdmin cancelar(Long id) {
        Turno turno = turnos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));
        LocalDateTime ahora = reloj.ahora();
        turno.cancelar(ahora);
        return TurnoAdmin.de(turnos.guardar(turno), ahora);
    }
}
