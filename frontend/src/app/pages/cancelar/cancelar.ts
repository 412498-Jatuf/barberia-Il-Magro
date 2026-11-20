import { Component, DestroyRef, OnInit, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { fechaLarga } from '../../core/formato';
import { ApiError, TurnoACancelar } from '../../core/models';

/**
 * Pagina del link de cancelacion que va en el WhatsApp del turno: muestra el turno y, con un toque,
 * lo cancela y libera el horario para que otro lo pueda reservar.
 */
@Component({
  selector: 'app-cancelar',
  imports: [RouterLink],
  templateUrl: './cancelar.html',
  styleUrl: './cancelar.scss',
})
export class Cancelar implements OnInit {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);

  /** :codigo de la ruta (withComponentInputBinding). */
  readonly codigo = input.required<string>();

  /** "Miércoles, 25 de noviembre": mayúscula solo al principio. */
  protected dia(iso: string): string {
    const f = fechaLarga(iso);
    return f.charAt(0).toUpperCase() + f.slice(1);
  }
  protected readonly turno = signal<TurnoACancelar | null>(null);
  protected readonly cargando = signal(true);
  protected readonly noExiste = signal(false);
  protected readonly cancelando = signal(false);
  protected readonly recienCancelado = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api
      .turnoACancelar(this.codigo())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (t) => {
          this.turno.set(t);
          this.cargando.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.cargando.set(false);
          if (err.status === 404) this.noExiste.set(true);
          else this.error.set('No pudimos cargar el turno. Probá de nuevo en unos minutos.');
        },
      });
  }

  protected cancelar(): void {
    if (this.cancelando()) return;
    this.cancelando.set(true);
    this.error.set(null);
    this.api
      .cancelarTurno(this.codigo())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (t) => {
          this.cancelando.set(false);
          this.turno.set(t);
          this.recienCancelado.set(true);
        },
        error: (err: HttpErrorResponse) => {
          this.cancelando.set(false);
          const api = err.error as ApiError | null;
          this.error.set(api?.mensaje ?? 'No pudimos cancelar el turno. Probá de nuevo en unos minutos.');
        },
      });
  }
}
