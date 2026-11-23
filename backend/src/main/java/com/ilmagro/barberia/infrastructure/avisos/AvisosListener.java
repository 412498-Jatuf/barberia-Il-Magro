package com.ilmagro.barberia.infrastructure.avisos;

import com.ilmagro.barberia.application.agenda.AvisoDeTurno;
import com.ilmagro.barberia.application.agenda.TurnoConfirmado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Cuando un turno queda confirmado (despues del commit, asi nunca se avisa un turno que no se
 * guardo) le avisa a la barberia por cada canal activo. Corre en segundo plano: el cliente no espera
 * al WhatsApp ni al email, y si un canal falla la reserva sigue firme y queda en el log.
 */
@Component
class AvisosListener {

    private static final Logger log = LoggerFactory.getLogger(AvisosListener.class);

    private final List<AvisoDeTurno> avisos;

    AvisosListener(List<AvisoDeTurno> avisos) {
        this.avisos = avisos;
        if (avisos.isEmpty()) {
            log.warn("No hay avisos de turnos configurados: los turnos nuevos solo quedan en la base. "
                    + "Configura AVISO_WHATSAPP_APIKEY y/o AVISO_EMAIL_PARA + MAIL_HOST.");
        } else {
            log.info("Avisos de turnos activos: {}", avisos.stream().map(AvisoDeTurno::canal).toList());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void alConfirmar(TurnoConfirmado turno) {
        log.info("Turno #{} confirmado: {} {} {}", turno.id(), turno.clienteNombre(), turno.fecha(), turno.horaInicio());
        for (AvisoDeTurno aviso : avisos) {
            if (!corresponde(aviso, turno)) continue;
            try {
                aviso.avisar(turno);
                log.info("Aviso del turno #{} enviado por {}", turno.id(), aviso.canal());
            } catch (Exception e) {
                log.error("No se pudo avisar el turno #{} por {}: {}", turno.id(), aviso.canal(), e.getMessage(), e);
            }
        }
    }

    /**
     * El cliente elige un solo canal: si eligio WhatsApp el aviso lo manda el mismo cliente desde su
     * celular (wa.me) y el backend no manda nada; si eligio email, solo sale el mail.
     */
    private static boolean corresponde(AvisoDeTurno aviso, TurnoConfirmado turno) {
        return turno.avisoPor() == null || turno.avisoPor().equalsIgnoreCase(aviso.canal())
                && !"whatsapp".equalsIgnoreCase(turno.avisoPor());
    }
}
