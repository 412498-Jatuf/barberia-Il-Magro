package com.ilmagro.barberia.infrastructure.avisos;

import com.ilmagro.barberia.application.agenda.AvisoDeTurno;
import com.ilmagro.barberia.application.agenda.TurnoConfirmado;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Aviso por WhatsApp al celular de la barberia usando CallMeBot (gratis, para mensajes a tu propio
 * numero). Se activa solo si hay apikey: barberia.avisos.whatsapp.apikey (env AVISO_WHATSAPP_APIKEY).
 * Para obtenerla, desde el WhatsApp de la barberia se manda un mensaje al bot (ver README).
 */
@Component
@ConditionalOnExpression("'${barberia.avisos.whatsapp.apikey:}' != ''")
class AvisoWhatsapp implements AvisoDeTurno {

    private final AvisosProperties props;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    AvisoWhatsapp(AvisosProperties props) {
        this.props = props;
    }

    @Override
    public String canal() {
        return "whatsapp";
    }

    @Override
    public void avisar(TurnoConfirmado turno) throws Exception {
        String url = "https://api.callmebot.com/whatsapp.php"
                + "?phone=" + enc(props.whatsapp().telefono())
                + "&text=" + enc(MensajeTurno.texto(turno))
                + "&apikey=" + enc(props.whatsapp().apikey());
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new IllegalStateException("CallMeBot respondio " + res.statusCode() + ": " + res.body());
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
