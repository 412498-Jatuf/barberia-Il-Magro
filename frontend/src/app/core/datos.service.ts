import { Injectable, computed, inject, signal } from '@angular/core';
import { catchError, forkJoin, of } from 'rxjs';
import { ApiService } from './api.service';
import { NOMBRES_DIAS } from './formato';
import { Barbero, Corte, Horario, Servicio } from './models';

export interface FilaHorario {
  dias: string;
  horas: string;
}

/** Datos del sitio cargados una sola vez y compartidos por la landing, el footer y los turnos. */
@Injectable({ providedIn: 'root' })
export class DatosService {
  private readonly api = inject(ApiService);

  readonly listo = signal(false);
  readonly error = signal(false);
  readonly cortes = signal<Corte[]>([]);
  readonly servicios = signal<Servicio[]>([]);
  readonly barberos = signal<Barbero[]>([]);
  readonly horario = signal<Horario | null>(null);

  /** "Martes a sábado · 10 a 20 h" y "Domingo y lunes · Cerrado". */
  readonly filasHorario = computed<FilaHorario[]>(() => {
    const h = this.horario();
    if (!h) return [];
    // los dias con el mismo horario van juntos ("Martes a sábado · 15 a 22 h"); los cerrados, al final
    const porHorario = new Map<string, number[]>();
    for (let d = 1; d <= 7; d++) {
      const tramos = h.semana.find((s) => s.dia === d)?.tramos ?? [];
      const horas = tramos.length
        ? `${tramos.map((t) => `${hora(t.desde)} a ${hora(t.hasta)}`).join(' y ')} h`
        : 'Cerrado';
      porHorario.set(horas, [...(porHorario.get(horas) ?? []), d]);
    }
    const filas = [...porHorario].map(([horas, dias]) => ({ dias: rango(dias), horas }));
    return [...filas.filter((f) => f.horas !== 'Cerrado'), ...filas.filter((f) => f.horas === 'Cerrado')];
  });

  readonly precioDesde = computed(() => {
    const precios = this.servicios().map((s) => s.precio);
    return precios.length ? Math.min(...precios) : null;
  });

  private pedido = false;

  cargar(): void {
    if (this.pedido) return;
    this.pedido = true;
    forkJoin({
      cortes: this.api.cortes(),
      servicios: this.api.servicios(),
      barberos: this.api.barberos(),
      horario: this.api.horario(),
    })
      .pipe(catchError(() => of(null)))
      .subscribe((r) => {
        if (r) {
          this.cortes.set(r.cortes);
          this.servicios.set(r.servicios);
          this.barberos.set(r.barberos);
          this.horario.set(r.horario);
        } else {
          this.error.set(true);
        }
        this.listo.set(true);
      });
  }
}

/** "10:00" -> "10", "10:30" -> "10:30" */
function hora(hhmm: string): string {
  return hhmm.endsWith(':00') ? String(Number(hhmm.slice(0, 2))) : hhmm;
}

function rango(dias: number[]): string {
  if (!dias.length) return '';
  const consecutivos = dias.every((d, i) => i === 0 || d === dias[i - 1] + 1);
  if (consecutivos && dias.length > 2) {
    return `${NOMBRES_DIAS[dias[0]]} a ${NOMBRES_DIAS[dias.at(-1)!].toLowerCase()}`;
  }
  // Domingo primero cuando cierra con el lunes ("Domingo y lunes")
  const orden = dias.includes(7) && dias.includes(1) ? [7, ...dias.filter((d) => d !== 7)] : dias;
  const nombres = orden.map((d, i) => (i === 0 ? NOMBRES_DIAS[d] : NOMBRES_DIAS[d].toLowerCase()));
  return nombres.length > 1 ? `${nombres.slice(0, -1).join(', ')} y ${nombres.at(-1)}` : nombres[0];
}
