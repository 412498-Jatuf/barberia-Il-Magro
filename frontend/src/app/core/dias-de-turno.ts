import { aIsoLocal, diaIso } from './formato';
import { Horario } from './models';

/** Un dia elegible en el selector de la reserva. */
export interface DiaOpcion {
  iso: string;
  diaSemana: string;
  numero: number;
  mes: string;
}

/** Los proximos dias en los que la barberia atiende, desde hoy hasta los dias de anticipacion permitidos. */
export function proximosDias(h: Horario, desde: Date = new Date()): DiaOpcion[] {
  const hoy = new Date(desde);
  hoy.setHours(0, 0, 0, 0);
  const opciones: DiaOpcion[] = [];
  for (let i = 0; i <= h.diasAnticipacion; i++) {
    const d = new Date(hoy);
    d.setDate(hoy.getDate() + i);
    if (!h.semana.some((s) => s.dia === diaIso(d)) || h.cerrados.includes(aIsoLocal(d))) continue;
    opciones.push({
      iso: aIsoLocal(d),
      diaSemana: i === 0 ? 'Hoy' : i === 1 ? 'Mañana' : d.toLocaleDateString('es-AR', { weekday: 'short' }),
      numero: d.getDate(),
      mes: d.toLocaleDateString('es-AR', { month: 'short' }),
    });
  }
  return opciones;
}
