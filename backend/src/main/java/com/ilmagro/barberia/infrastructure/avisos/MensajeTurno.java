package com.ilmagro.barberia.infrastructure.avisos;

import com.ilmagro.barberia.application.agenda.TurnoConfirmado;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Arma el texto del aviso de un turno nuevo, igual para todos los canales. */
final class MensajeTurno {

    private static final Locale ES_AR = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES_AR);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private MensajeTurno() {
    }

    static String asunto(TurnoConfirmado t) {
        return "Turno nuevo: " + t.clienteNombre() + ", " + fecha(t) + " " + t.horaInicio().format(HORA);
    }

    static String texto(TurnoConfirmado t) {
        NumberFormat pesos = NumberFormat.getCurrencyInstance(ES_AR);
        pesos.setMaximumFractionDigits(0);
        StringBuilder sb = new StringBuilder()
                .append("💈 Turno nuevo en Il Magro\n\n")
                .append("Cliente: ").append(t.clienteNombre()).append('\n')
                .append("Teléfono: ").append(t.clienteTelefono()).append('\n');
        if (t.clienteEmail() != null && !t.clienteEmail().isBlank()) {
            sb.append("Email: ").append(t.clienteEmail()).append('\n');
        }
        sb.append("Servicio: ").append(t.servicio())
                .append(" (").append(t.duracionMinutos()).append(" min, ").append(pesos.format(t.precio())).append(")\n")
                .append("Día: ").append(fecha(t)).append('\n')
                .append("Hora: ").append(t.horaInicio().format(HORA)).append(" a ").append(t.horaFin().format(HORA)).append('\n')
                .append("Barbero: ").append(t.barbero()).append('\n');
        if (t.notas() != null && !t.notas().isBlank()) {
            sb.append("Comentarios: ").append(t.notas()).append('\n');
        }
        return sb.toString();
    }

    private static String fecha(TurnoConfirmado t) {
        String f = t.fecha().format(FECHA);
        return Character.toUpperCase(f.charAt(0)) + f.substring(1);
    }
}
