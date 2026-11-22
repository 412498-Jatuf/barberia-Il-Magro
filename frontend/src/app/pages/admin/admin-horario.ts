import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminService } from '../../core/admin.service';
import { NOMBRES_DIAS } from '../../core/formato';
import { ApiError, DiaHorario, TramoHorario, TurnoAdmin } from '../../core/models';
import { AdminAfectados } from './admin-afectados';

/** Un dia en edicion. Si se cierra y se vuelve a abrir, sus tramos siguen ahi. */
interface DiaEditable {
  dia: number;
  abierto: boolean;
  tramos: TramoHorario[];
}

const TRAMO_INICIAL: TramoHorario = { desde: '15:00', hasta: '22:00' };
const MAX_TRAMOS = 4;

/**
 * El horario de la semana: cada dia se abre o se cierra, y puede tener mas de un tramo (una pausa en el
 * medio). Guardar no cancela turnos ya reservados: avisa cuales quedaron fuera del horario nuevo.
 */
@Component({
  selector: 'app-admin-horario',
  imports: [AdminAfectados],
  templateUrl: './admin-horario.html',
})
export class AdminHorario implements OnInit {
  private readonly admin = inject(AdminService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly nombre = (dia: number) => NOMBRES_DIAS[dia];
  protected readonly maxTramos = MAX_TRAMOS;

  protected readonly dias = signal<DiaEditable[]>([]);
  private readonly guardado = signal('');
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly recienGuardado = signal(false);
  protected readonly afectados = signal<TurnoAdmin[]>([]);

  /** Hay cambios sin guardar. */
  protected readonly sinGuardar = computed(() => this.firma(this.dias()) !== this.guardado());

  /** Por dia, el problema del horario cargado (si lo hay): asi se avisa antes de mandarlo. */
  protected readonly problemas = computed(() => new Map(this.dias().map((d) => [d.dia, this.problema(d)])));
  protected readonly hayProblemas = computed(() => [...this.problemas().values()].some((p) => p !== null));

  ngOnInit(): void {
    this.admin
      .semana()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (semana) => this.mostrar(semana),
        error: () => {
          this.cargando.set(false);
          this.error.set('No pudimos cargar el horario. Probá de nuevo.');
        },
      });
  }

  protected alternarDia(d: DiaEditable): void {
    this.cambiar(d.dia, (x) => {
      const abierto = !x.abierto;
      return { ...x, abierto, tramos: abierto && !x.tramos.length ? [{ ...TRAMO_INICIAL }] : x.tramos };
    });
  }

  protected agregarTramo(d: DiaEditable): void {
    if (d.tramos.length >= MAX_TRAMOS) return;
    const ultimo = d.tramos.at(-1);
    // el tramo nuevo arranca una hora despues de donde termina el anterior (la pausa) y dura hasta 3 horas
    const desde = ultimo ? this.sumar(ultimo.hasta, 60) : TRAMO_INICIAL.desde;
    const hasta = ultimo ? this.sumar(desde, 180) : TRAMO_INICIAL.hasta;
    this.cambiar(d.dia, (x) => ({ ...x, tramos: [...x.tramos, { desde, hasta }] }));
  }

  protected quitarTramo(d: DiaEditable, i: number): void {
    this.cambiar(d.dia, (x) => ({ ...x, tramos: x.tramos.filter((_, j) => j !== i) }));
  }

  protected cambiarHora(d: DiaEditable, i: number, campo: 'desde' | 'hasta', valor: string): void {
    this.cambiar(d.dia, (x) => ({ ...x, tramos: x.tramos.map((t, j) => (j === i ? { ...t, [campo]: valor } : t)) }));
  }

  protected guardar(): void {
    if (this.guardando() || this.hayProblemas() || !this.sinGuardar()) return;
    this.guardando.set(true);
    this.error.set(null);
    this.recienGuardado.set(false);
    const semana: DiaHorario[] = this.dias().map((d) => ({ dia: d.dia, tramos: d.abierto ? d.tramos : [] }));
    this.admin
      .guardarSemana(semana)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.mostrar(r.semana);
          this.afectados.set(r.turnosAfectados);
          this.recienGuardado.set(true);
        },
        error: (err: HttpErrorResponse) => {
          this.guardando.set(false);
          this.error.set((err.error as ApiError | null)?.mensaje ?? 'No pudimos guardar el horario. Probá de nuevo.');
        },
      });
  }

  private mostrar(semana: DiaHorario[]): void {
    const dias = semana.map((d) => ({ dia: d.dia, abierto: d.tramos.length > 0, tramos: d.tramos }));
    this.dias.set(dias);
    this.guardado.set(this.firma(dias));
    this.cargando.set(false);
  }

  private cambiar(dia: number, f: (d: DiaEditable) => DiaEditable): void {
    this.recienGuardado.set(false);
    this.dias.update((lista) => lista.map((d) => (d.dia === dia ? f(d) : d)));
  }

  /** Lo que se mandaria al guardar: sirve para saber si hay cambios sin guardar. */
  private firma(dias: DiaEditable[]): string {
    return JSON.stringify(dias.map((d) => [d.dia, d.abierto ? d.tramos : []]));
  }

  private problema(d: DiaEditable): string | null {
    if (!d.abierto) return null;
    const orden = [...d.tramos].sort((a, b) => a.desde.localeCompare(b.desde));
    if (orden.some((t) => !t.desde || !t.hasta || t.hasta <= t.desde)) return 'La hora de fin tiene que ser después de la de inicio.';
    if (orden.some((t, i) => i > 0 && t.desde < orden[i - 1].hasta)) return 'Los tramos no pueden superponerse.';
    return null;
  }

  /** "18:00" + 60 minutos = "19:00" (sin pasar de las 23:30). */
  private sumar(hhmm: string, minutos: number): string {
    const [h, m] = hhmm.split(':').map(Number);
    const total = Math.min(h * 60 + m + minutos, 23 * 60 + 30);
    return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`;
  }
}
