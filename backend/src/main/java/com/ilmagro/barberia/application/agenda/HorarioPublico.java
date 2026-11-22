package com.ilmagro.barberia.application.agenda;

import com.ilmagro.barberia.domain.agenda.Tramo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * El horario tal como lo ve el cliente.
 *
 * @param semana  tramos de cada dia de la semana (1 = lunes ... 7 = domingo); un dia que no esta, esta cerrado
 * @param cerrados fechas proximas que Luca cerro enteras (feriado, viaje)
 */
public record HorarioPublico(int intervaloMinutos, int diasAnticipacion, Map<Integer, List<Tramo>> semana,
                             List<LocalDate> cerrados) {
}
