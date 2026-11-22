import { DOCUMENT } from '@angular/common';
import { Component, Injector, afterNextRender, computed, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { BARBERIA, linkMapa, linkWhatsapp } from './core/barberia.config';
import { DatosService } from './core/datos.service';
import { formatearPrecio } from './core/formato';
import { KitService } from './core/kit.service';

/**
 * Componente raiz, montado sobre <main id="top">. En "/" dibuja la landing directamente
 * (el kit necesita main > section); el resto de las rutas van al router-outlet.
 */
@Component({
  selector: 'main[app-root]',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
})
export class App {
  private readonly router = inject(Router);
  private readonly kit = inject(KitService);
  private readonly injector = inject(Injector);
  protected readonly datos = inject(DatosService);

  protected readonly b = BARBERIA;
  protected readonly mapa = linkMapa(BARBERIA);
  protected readonly whatsapp = linkWhatsapp(BARBERIA);
  protected readonly precio = formatearPrecio;

  protected readonly esLanding = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => new URL(e.urlAfterRedirects, location.origin).pathname === '/'),
    ),
    { initialValue: location.pathname === '/' },
  );

  /** En /admin (panel privado de Luca) no va la navegacion ni el pie del sitio publico. */
  private readonly esAdmin = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => new URL(e.urlAfterRedirects, location.origin).pathname.startsWith('/admin')),
    ),
    { initialValue: location.pathname.startsWith('/admin') },
  );
  private readonly documento = inject(DOCUMENT);

  protected readonly nombresBarberos = computed(() => {
    const n = this.datos.barberos().map((b) => b.nombre);
    return n.length > 1 ? `${n.slice(0, -1).join(', ')} y ${n.at(-1)}` : (n[0] ?? '');
  });

  /** Los datos clave del hero: dos precios y el horario. */
  protected readonly hechos = computed(() => {
    const precios = this.datos
      .servicios()
      .filter((s) => s.nombre === 'Corte' || s.nombre === 'Corte y barba')
      .map((s) => ({ dt: s.nombre, dd: formatearPrecio(s.precio) }));
    const filas = this.datos.filasHorario();
    const horario = filas.length ? [{ dt: filas[0].dias, dd: filas[0].horas }] : [];
    return [...precios, ...horario, { dt: 'Turnos', dd: 'Online, en un minuto' }];
  });

  constructor() {
    this.datos.cargar();
    // body.es-admin lo usan los estilos para esconder el encabezado, el pie y la barra de reservar
    effect(() => this.documento.body.classList.toggle('es-admin', this.esAdmin()));
    // El kit lee el DOM al iniciar: se carga cuando la landing ya esta dibujada con los datos.
    effect(() => {
      if (this.datos.listo() && this.esLanding()) {
        afterNextRender(() => this.kit.iniciar(), { injector: this.injector });
      } else if (!this.esLanding()) {
        this.kit.desmontarMoments();
      }
    });
  }
}
