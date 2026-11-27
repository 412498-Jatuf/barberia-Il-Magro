import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminService } from '../../core/admin.service';
import { ApiService } from '../../core/api.service';
import { DatosService } from '../../core/datos.service';
import { formatearPrecio } from '../../core/formato';
import { ApiError, Servicio } from '../../core/models';

/** Lo que entra en la base: hasta 8 cifras enteras y 2 decimales. */
const PRECIO_MAXIMO = 99_999_999.99;

/**
 * Precios de los servicios: Luca escribe el precio nuevo de cada uno y lo guarda. El cambio vale para las
 * reservas nuevas y se ve enseguida en el sitio. Cada servicio se guarda por separado.
 */
@Component({
  selector: 'app-admin-precios',
  templateUrl: './admin-precios.html',
})
export class AdminPrecios implements OnInit {
  private readonly api = inject(ApiService);
  private readonly admin = inject(AdminService);
  private readonly datos = inject(DatosService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly precio = formatearPrecio;
  protected readonly servicios = signal<Servicio[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  /** Lo que Luca viene escribiendo en cada campo, por id (lo que no se tocó no está acá). */
  protected readonly borradores = signal<Readonly<Record<number, string>>>({});
  protected readonly guardando = signal<number | null>(null);
  /** El servicio recién guardado (para decir "Listo") y el que falló con su motivo. */
  protected readonly guardado = signal<number | null>(null);
  protected readonly falla = signal<{ id: number; mensaje: string } | null>(null);

  ngOnInit(): void {
    this.api
      .servicios()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (lista) => {
          this.servicios.set(lista);
          this.cargando.set(false);
        },
        error: () => {
          this.cargando.set(false);
          this.error.set('No pudimos cargar los servicios. Probá de nuevo.');
        },
      });
  }

  protected valor(s: Servicio): string {
    return this.borradores()[s.id] ?? String(s.precio);
  }

  protected editar(s: Servicio, texto: string): void {
    this.borradores.update((b) => ({ ...b, [s.id]: texto }));
    if (this.guardado() === s.id) this.guardado.set(null);
    if (this.falla()?.id === s.id) this.falla.set(null);
  }

  /** El precio escrito, o null si está vacío o no sirve (cero, negativo, muy grande o con más de 2 decimales). */
  protected nuevoPrecio(s: Servicio): number | null {
    const texto = this.valor(s).trim();
    const n = Number(texto);
    if (texto === '' || !Number.isFinite(n) || n <= 0 || n > PRECIO_MAXIMO) return null;
    return Math.abs(n * 100 - Math.round(n * 100)) < 1e-6 ? n : null;
  }

  protected cambio(s: Servicio): boolean {
    const n = this.nuevoPrecio(s);
    return n !== null && n !== s.precio;
  }

  protected invalido(s: Servicio): boolean {
    return s.id in this.borradores() && this.nuevoPrecio(s) === null;
  }

  protected guardar(s: Servicio): void {
    const n = this.nuevoPrecio(s);
    if (n === null || !this.cambio(s) || this.guardando() !== null) return;
    this.guardando.set(s.id);
    this.guardado.set(null);
    this.falla.set(null);
    this.admin
      .cambiarPrecio(s.id, n)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (actualizado) => {
          this.guardando.set(null);
          this.servicios.update((lista) => lista.map((x) => (x.id === actualizado.id ? actualizado : x)));
          this.datos.reemplazarServicio(actualizado);
          this.borradores.update((b) => {
            const resto = { ...b };
            delete resto[s.id];
            return resto;
          });
          this.guardado.set(actualizado.id);
        },
        error: (err: HttpErrorResponse) => {
          this.guardando.set(null);
          this.falla.set({
            id: s.id,
            mensaje: (err.error as ApiError | null)?.mensaje ?? 'No pudimos guardar el precio. Probá de nuevo.',
          });
        },
      });
  }

  protected escrito(evento: Event): string {
    return (evento.target as HTMLInputElement).value;
  }
}
