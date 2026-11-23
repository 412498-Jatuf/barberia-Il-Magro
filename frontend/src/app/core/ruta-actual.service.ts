import { DOCUMENT } from '@angular/common';
import { Injectable, computed, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map } from 'rxjs';

/** La seccion de la ruta: la primera parte de la url ("turnos", "admin"...) o "inicio" en la raiz. */
function seccionDe(url: string): string {
  return new URL(url, location.origin).pathname.split('/')[1] || 'inicio';
}

/**
 * Dice en que seccion del sitio esta el visitante y la deja en body[data-ruta].
 * El encabezado y el pie se montan fuera del router (main.ts): con esa marca los estilos pueden
 * esconder o mostrar partes por pagina sin que cada componente tenga que saber la ruta.
 */
@Injectable({ providedIn: 'root' })
export class RutaActual {
  private readonly router = inject(Router);
  private readonly documento = inject(DOCUMENT);

  readonly seccion = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => seccionDe(e.urlAfterRedirects)),
    ),
    { initialValue: seccionDe(location.pathname) },
  );

  readonly esInicio = computed(() => this.seccion() === 'inicio');

  constructor() {
    effect(() => (this.documento.body.dataset['ruta'] = this.seccion()));
  }
}
