package com.ilmagro.barberia.infrastructure.avisos;

import com.ilmagro.barberia.application.agenda.AvisoDeTurno;
import com.ilmagro.barberia.application.agenda.TurnoConfirmado;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Aviso por email a la barberia. Se activa si hay destinatario (barberia.avisos.email.para, env
 * AVISO_EMAIL_PARA) y servidor de correo (spring.mail.host, env MAIL_HOST; con Brevo:
 * smtp-relay.brevo.com y la SMTP key; con Gmail: smtp.gmail.com y una "contraseña de aplicacion").
 */
@Component
@ConditionalOnExpression("'${barberia.avisos.email.para:}' != '' and '${spring.mail.host:}' != ''")
class AvisoEmail implements AvisoDeTurno {

    private final AvisosProperties props;
    private final JavaMailSender mail;

    AvisoEmail(AvisosProperties props, JavaMailSender mail) {
        this.props = props;
        this.mail = mail;
    }

    @Override
    public String canal() {
        return "email";
    }

    @Override
    public void avisar(TurnoConfirmado turno) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setTo(props.email().para());
        if (props.email().de() != null && !props.email().de().isBlank()) {
            m.setFrom(props.email().de());
        }
        if (turno.clienteEmail() != null && !turno.clienteEmail().isBlank()) {
            m.setReplyTo(turno.clienteEmail());
        }
        m.setSubject(MensajeTurno.asunto(turno));
        m.setText(MensajeTurno.texto(turno));
        mail.send(m);
    }
}
