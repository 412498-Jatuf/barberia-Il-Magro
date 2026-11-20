import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BARBERIA } from '../core/barberia.config';

/** header.nav del kit (nav-split): las palabras a la izquierda, el nombre al centro, el boton a la derecha. */
@Component({
  selector: 'app-header',
  imports: [RouterLink],
  template: `
    <div class="wrap nav__bar">
      <a class="skip-link" href="#top">Ir al contenido</a>
      <nav class="nav__links" aria-label="Principal">
        <div class="nav__item"><a class="nav__link" routerLink="/" fragment="visit">Nosotros</a></div>
        <div class="nav__item"><a class="nav__link" routerLink="/" fragment="servicios">Precios</a></div>
        <div class="nav__item"><a class="nav__link" routerLink="/" fragment="cortes">Cortes</a></div>
      </nav>
      <a class="nav__logo" routerLink="/"><img class="logo" src="assets/img/logo.png" width="151" height="154" alt="" aria-hidden="true">{{ nombre }}</a>
      <div class="nav__actions">
        <a class="btn btn--primary" routerLink="/turnos">Reservar turno</a>
      </div>
      <button class="nav__toggle" type="button" aria-expanded="false" aria-label="Menú">
        <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden="true"><path d="M2 5h14M2 13h14" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
      </button>
    </div>
  `,
})
export class Header {
  protected readonly nombre = BARBERIA.nombre;
}
