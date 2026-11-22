import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../core/admin.service';
import { aIsoLocal, fechaLarga } from '../../core/formato';
import { ApiError, BloqueoAgenda, TurnoAdmin } from '../../core/models';
import { AdminAfectados } from './admin-afectados';

/**
 * Cierres puntuales de la agenda: un dia entero (feriado, viaje) o unas horas de un dia. No tocan el
 * horario semanal. Si ya habia turnos reservados en lo que se cierra, se avisa cuales (no se cancelan solos).
 */
@Component({
  selector: 'app-admin-cierres',
  imports: [ReactiveFormsModule, AdminAfectados],
  templateUrl: './admin-cierres.html',
})
export class AdminCierres implements OnInit {
  private readonly admin = inject(AdminService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly hoy = aIsoLocal(new Date());
  protected readonly cierres = signal<BloqueoAgenda[]>([]);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly quitando = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly afectados = signal<TurnoAdmin[]>([]);
  protected readonly recienCerrado = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    fecha: ['', Validators.required],
    todoElDia: [true],
    desde: ['18:00'],
    hasta: ['20:00'],
    motivo: ['', Validators.maxLength(200)],
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected dia(iso: string): string {
    const f = fechaLarga(iso);
    return f.charAt(0).toUpperCase() + f.slice(1);
  }

  protected cerrar(): void {
    this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    if (this.form.invalid || this.guardando()) return;
    if (!v.todoElDia && (!v.desde || !v.hasta || v.hasta <= v.desde)) {
      this.error.set('La hora de fin tiene que ser después de la de inicio.');
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.recienCerrado.set(false);
    this.admin
      .bloquear({
        fecha: v.fecha,
        desde: v.todoElDia ? null : v.desde,
        hasta: v.todoElDia ? null : v.hasta,
        motivo: v.motivo.trim() || null,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.afectados.set(r.turnosAfectados);
          this.recienCerrado.set(true);
          this.form.patchValue({ fecha: '', motivo: '' });
          this.form.markAsUntouched();
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.guardando.set(false);
          this.error.set((err.error as ApiError | null)?.mensaje ?? 'No pudimos guardar el cierre. Probá de nuevo.');
        },
      });
  }

  protected quitar(c: BloqueoAgenda): void {
    if (this.quitando() !== null) return;
    this.quitando.set(c.id);
    this.error.set(null);
    this.admin
      .quitarBloqueo(c.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.quitando.set(null);
          this.afectados.set([]);
          this.recienCerrado.set(false);
          this.cierres.update((lista) => lista.filter((x) => x.id !== c.id));
        },
        error: () => {
          this.quitando.set(null);
          this.error.set('No pudimos quitar el cierre. Probá de nuevo.');
        },
      });
  }

  private cargar(): void {
    this.admin
      .bloqueos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (c) => {
          this.cierres.set(c);
          this.cargando.set(false);
        },
        error: () => {
          this.cargando.set(false);
          this.error.set('No pudimos cargar los cierres. Probá de nuevo.');
        },
      });
  }
}
