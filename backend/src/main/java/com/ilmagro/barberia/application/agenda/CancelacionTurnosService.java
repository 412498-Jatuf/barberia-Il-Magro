package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.agenda.Turno;
import com.ilmagro.barberia.domain.agenda.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso: cancelar un turno desde su link (el codigo secreto que va en el mensaje de WhatsApp).
 * Al quedar CANCELADO el turno deja de ocupar el horario, que vuelve a aparecer libre.
 */
@Service
public class CancelacionTurnosService {

    private final TurnoRepository turnos;
    private final RelojBarberia reloj;

    public CancelacionTurnosService(TurnoRepository turnos, RelojBarberia reloj) {
        this.turnos = turnos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public TurnoParaCancelar consultar(String codigo) {
        return TurnoParaCancelar.de(buscar(codigo), reloj.ahora());
    }

    @Transactional
    public TurnoParaCancelar cancelar(String codigo) {
        Turno turno = buscar(codigo);
        LocalDateTime ahora = reloj.ahora();
        turno.cancelar(ahora);
        return TurnoParaCancelar.de(turnos.guardar(turno), ahora);
    }

    private Turno buscar(String codigo) {
        if (codigo == null || codigo.isBlank() || codigo.length() > 32) {
            throw new RecursoNoEncontradoException("Turno no encontrado");
        }
        return turnos.buscarPorCodigoCancelacion(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));
    }
}
