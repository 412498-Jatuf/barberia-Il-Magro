import { Component, DestroyRef, computed, effect, inject, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminService } from '../../core/admin.service';
import { DatosService } from '../../core/datos.service';
import { aIsoLocal, fechaLarga } from '../../core/formato';
import { linkWhatsappCliente, mensajeCancelacionAlCliente } from '../../core/mensaje-admin';
import { ApiError, TurnoAdmin } from '../../core/models';

interface Grupo {
  fecha: string;
  titulo: string;
  turnos: TurnoAdmin[];
}

/** Mientras no llegue el horario del sitio: lo mismo que se puede reservar por defecto. */
const DIAS_POR_DEFECTO = 7;

/** Los turnos de los dias que los clientes pueden reservar (hoy + la anticipacion del sitio), por dia. Luca puede cancelar uno (libera el horario) y avisarle al cliente. */
@Component({
  selector: 'app-admin-turnos',
  templateUrl: './admin-turnos.html',
})
export class AdminTurnos {
  private readonly admin = inject(AdminService);
  private readonly datos = inject(DatosService);
  private readonly destroyRef = inject(DestroyRef);

  /** Hasta donde se puede reservar: no hay turnos mas lejos que eso. */
  protected readonly diasAVer = computed(() => this.datos.horario()?.diasAnticipacion ?? DIAS_POR_DEFECTO);
  protected readonly turnos = signal<TurnoAdmin[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly verCancelados = signal(false);
  /** El turno que se esta por cancelar (se pide confirmacion en la misma tarjeta). */
  protected readonly confirmando = signal<number | null>(null);
  protected readonly cancelando = signal<number | null>(null);
  /** Los que se cancelaron en esta sesion: siguen a la vista para avisarle al cliente. */
  protected readonly recientes = signal<ReadonlySet<number>>(new Set());
  protected readonly errorAccion = signal<string | null>(null);

  protected readonly visibles = computed(() =>
    this.turnos().filter((t) => t.estado === 'CONFIRMADO' || this.verCancelados() || this.recientes().has(t.id)),
  );

  protected readonly grupos = computed<Grupo[]>(() => {
    const porDia = new Map<string, TurnoAdmin[]>();
    for (const t of this.visibles()) porDia.set(t.fecha, [...(porDia.get(t.fecha) ?? []), t]);
    return [...porDia].map(([fecha, turnos]) => ({ fecha, titulo: this.titulo(fecha), turnos }));
  });

  protected readonly cantidadCancelados = computed(() => this.turnos().filter((t) => t.estado === 'CANCELADO').length);

  constructor() {
    // carga al abrir y otra vez si el horario del sitio trae una anticipacion distinta
    effect(() => {
      this.diasAVer();
      untracked(() => this.cargar());
    });
  }

  protected cargar(): void {
    const hoy = new Date();
    const hasta = new Date(hoy);
    hasta.setDate(hoy.getDate() + this.diasAVer());
    this.cargando.set(true);
    this.error.set(null);
    this.admin
      .turnos(aIsoLocal(hoy), aIsoLocal(hasta))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (t) => {
          this.turnos.set(t);
          this.cargando.set(false);
        },
        error: () => {
          this.cargando.set(false);
          this.error.set('No pudimos cargar los turnos. Probá de nuevo.');
        },
      });
  }

  protected cancelar(t: TurnoAdmin): void {
    if (this.cancelando() !== null) return;
    this.cancelando.set(t.id);
    this.errorAccion.set(null);
    this.admin
      .cancelarTurno(t.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (actualizado) => {
          this.cancelando.set(null);
          this.confirmando.set(null);
          this.turnos.update((lista) => lista.map((x) => (x.id === actualizado.id ? actualizado : x)));
          this.recientes.update((s) => new Set(s).add(actualizado.id));
        },
        error: (err: HttpErrorResponse) => {
          this.cancelando.set(null);
          this.errorAccion.set((err.error as ApiError | null)?.mensaje ?? 'No pudimos cancelar el turno. Probá de nuevo.');
        },
      });
  }

  /** wa.me al cliente con el aviso de cancelacion ya escrito. */
  protected avisoAlCliente(t: TurnoAdmin): string {
    return linkWhatsappCliente(t, mensajeCancelacionAlCliente(t, `${location.origin}/turnos`));
  }

  protected primerNombre(t: TurnoAdmin): string {
    return t.clienteNombre.split(' ')[0];
  }

  private titulo(iso: string): string {
    const hoy = new Date();
    const manana = new Date(hoy);
    manana.setDate(hoy.getDate() + 1);
    const f = fechaLarga(iso);
    const larga = f.charAt(0).toUpperCase() + f.slice(1);
    if (iso === aIsoLocal(hoy)) return `Hoy · ${larga}`;
    if (iso === aIsoLocal(manana)) return `Mañana · ${larga}`;
    return larga;
  }
}
