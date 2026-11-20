package com.ilmagro.barberia.application.agenda;

/**
 * Puerto de salida: avisarle a la barberia que entro un turno. Las implementaciones (WhatsApp,
 * email, log) viven en infrastructure/avisos y se activan por configuracion.
 */
public interface AvisoDeTurno {

    /** Nombre corto del canal, para los logs ("whatsapp", "email"...). */
    String canal();

    void avisar(TurnoConfirmado turno) throws Exception;
}
