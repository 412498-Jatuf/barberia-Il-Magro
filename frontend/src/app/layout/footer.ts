import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BARBERIA, linkMapa, linkWhatsapp } from '../core/barberia.config';
import { DatosService } from '../core/datos.service';
import { Credito } from './credito';

/** footer.closing del kit: fila 1 el cierre (close-word sin el nombre), fila 2 footer-visit. */
@Component({
  selector: 'app-footer',
  imports: [RouterLink, Credito],
  template: `
    <div class="wrap">
      <div class="closing__cta close--word">
        <div class="section-head--split">
          <div>
            <h2 id="close-h">Tu turno en un minuto</h2>
            <p class="t-lead">Elegís servicio, día y horario.</p>
          </div>
          <a class="btn btn--primary" routerLink="/turnos">Reservar turno<svg class="m-arrow" viewBox="0 0 12 12" aria-hidden="true"><path class="m-arrow__stem" d="M1 6h8"/><path class="m-arrow__head" d="M4.5 2.5 8 6l-3.5 3.5"/></svg></a>
        </div>
        <p class="close__word" aria-hidden="true">{{ b.nombre }}</p>
      </div>
      <div class="closing__links">
        <div class="grid">
          @if (b.calle || b.instagram || whatsapp) {
          <div class="span-6 stack-4">
            @if (b.calle) {
              <address class="footer__address">{{ b.calle }}@if (b.barrio) {, {{ b.barrio }}}</address>
              <p><a class="link con-ubicacion" [href]="mapa" target="_blank" rel="noopener">Cómo llegar</a></p>
            }
            @if (whatsapp) {
              <p><a class="link con-wa" [href]="whatsapp" target="_blank" rel="noopener">Escribinos por WhatsApp</a></p>
            }
            @if (b.instagram) {
              <p><a class="link" [href]="'https://instagram.com/' + b.instagram" target="_blank" rel="noopener">Instagram</a></p>
            }
          </div>
          }
          <div class="span-6 stack-4" [class.start-7]="b.calle || b.instagram || whatsapp">
            <h3>Horarios</h3>
            @if (datos.filasHorario().length) {
              <dl class="hours">
                @for (f of datos.filasHorario(); track f.dias) {
                  <dt>{{ f.dias }}</dt><dd>{{ f.horas }}</dd>
                }
              </dl>
            }
          </div>
        </div>
        <nav class="footer__nav" aria-label="Pie de página">
          <a routerLink="/" fragment="visit">Nosotros</a>
          <a routerLink="/" fragment="servicios">Precios</a>
          <a routerLink="/" fragment="cortes">Cortes</a>
        </nav>
        <div class="footer__legal">
          <span>&copy; {{ anio }} {{ b.nombre }} · {{ b.barbero }}</span>
          <app-credito class="footer__credito" />
        </div>
      </div>
    </div>
  `,
})
export class Footer {
  protected readonly datos = inject(DatosService);
  protected readonly b = BARBERIA;
  protected readonly mapa = linkMapa(BARBERIA);
  protected readonly whatsapp = linkWhatsapp(BARBERIA);
  protected readonly anio = new Date().getFullYear();
}
