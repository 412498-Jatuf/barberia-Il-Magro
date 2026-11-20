package com.ilmagro.barberia.infrastructure.avisos;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuracion de los avisos de turnos nuevos (barberia.avisos.* en application.properties). */
@ConfigurationProperties("barberia.avisos")
public record AvisosProperties(Whatsapp whatsapp, Email email) {

    public AvisosProperties {
        whatsapp = whatsapp == null ? new Whatsapp(null, null) : whatsapp;
        email = email == null ? new Email(null, null) : email;
    }

    /** telefono: con codigo de pais y "+" (ej. +5493516598837). apikey: la que da CallMeBot. */
    public record Whatsapp(String telefono, String apikey) {
    }

    /** para: a quien le llega el aviso. de: remitente (si el servidor lo exige). */
    public record Email(String para, String de) {
    }
}
