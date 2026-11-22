package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.agenda.Bloqueo;
import com.ilmagro.barberia.domain.agenda.DiaDeAtencion;
import com.ilmagro.barberia.domain.agenda.HorarioRepository;
import com.ilmagro.barberia.domain.agenda.Tramo;
import com.ilmagro.barberia.domain.agenda.TramoSemanal;
import com.ilmagro.barberia.domain.agenda.Turno;
import com.ilmagro.barberia.domain.agenda.TurnoRepository;
import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lo que Luca administra desde el panel: el horario de la semana y los cierres puntuales de la agenda.
 * Cambiar el horario nunca cancela turnos ya reservados: el resultado avisa cuales quedaron afuera
 * para que Luca decida que hacer con cada uno.
 */
@Service
@Transactional
public class AdministracionAgendaService {

    /** Hasta cuantos dias adelante se buscan turnos que un cambio de horario deja afuera. */
    private static final int DIAS_A_REVISAR = 90;

    private final HorarioRepository horarios;
    private final TurnoRepository turnos;
    private final RelojBarberia reloj;

    public AdministracionAgendaService(HorarioRepository horarios, TurnoRepository turnos, RelojBarberia reloj) {
        this.horarios = horarios;
        this.turnos = turnos;
        this.reloj = reloj;
    }

    /** Los siete dias de la semana, con sus tramos (vacios si ese dia esta cerrado). */
    @Transactional(readOnly = true)
    public List<DiaDeAtencion> semana() {
        Map<Integer, List<Tramo>> porDia = horarios.semana().stream().collect(
                Collectors.groupingBy(TramoSemanal::getDiaSemana, Collectors.mapping(TramoSemanal::tramo, Collectors.toList())));
        List<DiaDeAtencion> semana = new ArrayList<>();
        for (int dia = 1; dia <= 7; dia++) {
            semana.add(new DiaDeAtencion(dia, porDia.getOrDefault(dia, List.of())));
        }
        return semana;
    }

    public SemanaGuardada guardarSemana(List<DiaDeAtencion> dias) {
        Set<Integer> vistos = new HashSet<>();
        for (DiaDeAtencion d : dias) {
            if (!vistos.add(d.dia())) {
                throw new DatoInvalidoException("Hay un dia repetido en el horario");
            }
        }
        List<TramoSemanal> nuevos = dias.stream()
                .flatMap(d -> d.tramos().stream().map(t -> new TramoSemanal(d.dia(), t.desde(), t.hasta())))
                .toList();
        horarios.reemplazarSemana(nuevos);

        List<DiaDeAtencion> semana = semana();
        return new SemanaGuardada(semana, turnosQueQuedaronAfuera(semana));
    }

    @Transactional(readOnly = true)
    public List<BloqueoAdmin> bloqueos() {
        return horarios.bloqueosDesde(hoy()).stream().map(BloqueoAdmin::de).toList();
    }

    /** Cierra un dia entero (sin horas) o unas horas de un dia. */
    public BloqueoCreado bloquear(LocalDate fecha, LocalTime desde, LocalTime hasta, String motivo) {
        if (fecha != null && fecha.isBefore(hoy())) {
            throw new DatoInvalidoException("No se puede cerrar un dia que ya paso");
        }
        if ((desde == null) != (hasta == null)) {
            throw new DatoInvalidoException("Para cerrar unas horas hacen falta el inicio y el fin");
        }
        Bloqueo bloqueo = desde == null ? Bloqueo.diaCompleto(fecha, motivo) : Bloqueo.horas(fecha, desde, hasta, motivo);
        Bloqueo guardado = horarios.guardarBloqueo(bloqueo);

        LocalDateTime ahora = reloj.ahora();
        List<TurnoAdmin> afectados = turnos.turnosEntre(fecha, fecha).stream()
                .filter(t -> t.sePuedeCancelar(ahora) && t.ocupa(guardado.tramo().desde(), guardado.tramo().hasta()))
                .map(t -> TurnoAdmin.de(t, ahora))
                .toList();
        return new BloqueoCreado(BloqueoAdmin.de(guardado), afectados);
    }

    public void quitarBloqueo(Long id) {
        Bloqueo bloqueo = horarios.buscarBloqueo(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cierre no encontrado"));
        horarios.eliminarBloqueo(bloqueo);
    }

    /** Turnos confirmados que todavia no empezaron y que caen fuera del horario semanal nuevo. */
    private List<TurnoAdmin> turnosQueQuedaronAfuera(List<DiaDeAtencion> semana) {
        Map<Integer, DiaDeAtencion> porDia = semana.stream().collect(Collectors.toMap(DiaDeAtencion::dia, d -> d));
        LocalDateTime ahora = reloj.ahora();
        LocalDate hoy = ahora.toLocalDate();
        List<Turno> futuros = turnos.turnosEntre(hoy, hoy.plusDays(DIAS_A_REVISAR));
        return futuros.stream()
                .filter(t -> t.sePuedeCancelar(ahora))
                .filter(t -> !porDia.get(t.getFecha().getDayOfWeek().getValue())
                        .contiene(new Tramo(t.getHoraInicio(), t.getHoraFin())))
                .map(t -> TurnoAdmin.de(t, ahora))
                .toList();
    }

    private LocalDate hoy() {
        return reloj.ahora().toLocalDate();
    }
}
