import { Component, Injector, afterNextRender, computed, effect, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { BARBERIA, linkMapa, linkWhatsapp } from './core/barberia.config';
import { DatosService } from './core/datos.service';
import { formatearPrecio } from './core/formato';
import { KitService } from './core/kit.service';
import { RutaActual } from './core/ruta-actual.service';

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
  private readonly kit = inject(KitService);
  private readonly injector = inject(Injector);
  protected readonly datos = inject(DatosService);

  protected readonly b = BARBERIA;
  protected readonly mapa = linkMapa(BARBERIA);
  protected readonly whatsapp = linkWhatsapp(BARBERIA);
  protected readonly precio = formatearPrecio;

  /** En "/" se dibuja la landing; ademas deja la marca body[data-ruta] que usan los estilos. */
  protected readonly esLanding = inject(RutaActual).esInicio;

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
    return [...precios, ...horario, { dt: 'Turnos', dd: 'Online' }];
  });

  constructor() {
    this.datos.cargar();
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
