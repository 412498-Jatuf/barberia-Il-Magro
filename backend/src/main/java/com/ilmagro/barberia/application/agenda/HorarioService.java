package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Bloqueo;
import com.ilmagro.barberia.domain.agenda.HorarioRepository;
import com.ilmagro.barberia.domain.agenda.Jornada;
import com.ilmagro.barberia.domain.agenda.ReglasDeReserva;
import com.ilmagro.barberia.domain.agenda.Tramo;
import com.ilmagro.barberia.domain.agenda.TramoSemanal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Consultas del horario: la jornada de un dia concreto y el horario que se le muestra al cliente. */
@Service
@Transactional(readOnly = true)
public class HorarioService {

    private final HorarioRepository horarios;
    private final ReglasDeReserva reglas;
    private final RelojBarberia reloj;

    public HorarioService(HorarioRepository horarios, ReglasDeReserva reglas, RelojBarberia reloj) {
        this.horarios = horarios;
        this.reglas = reglas;
        this.reloj = reloj;
    }

    /** Las horas que se atienden ese dia: el horario de su dia de la semana menos los bloqueos de esa fecha. */
    public Jornada jornadaDe(LocalDate fecha) {
        int dia = fecha.getDayOfWeek().getValue();
        List<Tramo> delDia = horarios.semana().stream()
                .filter(t -> t.getDiaSemana() == dia)
                .map(TramoSemanal::tramo)
                .toList();
        return Jornada.de(delDia, horarios.bloqueosEntre(fecha, fecha));
    }

    /** Lo que necesita la pagina para mostrar el horario y los dias elegibles. */
    public HorarioPublico horarioPublico() {
        LocalDate hoy = reloj.ahora().toLocalDate();
        Map<Integer, List<Tramo>> semana = new TreeMap<>();
        for (TramoSemanal t : horarios.semana()) {
            semana.computeIfAbsent(t.getDiaSemana(), d -> new java.util.ArrayList<>()).add(t.tramo());
        }
        List<LocalDate> cerrados = horarios.bloqueosEntre(hoy, hoy.plusDays(reglas.diasAnticipacion())).stream()
                .filter(Bloqueo::esDiaCompleto)
                .map(Bloqueo::getFecha)
                .distinct()
                .toList();
        return new HorarioPublico(reglas.intervaloMinutos(), reglas.diasAnticipacion(), semana, cerrados);
    }
}
