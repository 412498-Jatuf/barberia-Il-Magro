import { Component, ElementRef, HostListener, computed, inject, signal } from '@angular/core';
import { DESARROLLADOR, waMe } from '../core/barberia.config';

interface Via {
  texto: string;
  href: string;
  /** Lleva el iconito de WhatsApp. */
  wa?: boolean;
}

/**
 * "Sitio por <nombre>" del pie. Si hay mas de una via de contacto, el nombre abre un menu chico
 * (LinkedIn, mail, WhatsApp); si hay una sola, es un link directo. Las vias en null no aparecen.
 */
@Component({
  selector: 'app-credito',
  template: `
    <span class="credito">
      Sitio por
      @if (vias().length > 1) {
        <button type="button" class="credito__nombre" aria-haspopup="true" [attr.aria-expanded]="abierto()"
                aria-controls="credito-menu" (click)="abierto.set(!abierto())">{{ dev.nombre }}</button>
        @if (abierto()) {
          <ul class="credito__menu" id="credito-menu" role="list">
            @for (v of vias(); track v.texto) {
              <li><a [href]="v.href" [class.con-wa]="v.wa" [target]="v.href.startsWith('mailto:') ? null : '_blank'" rel="noopener" (click)="abierto.set(false)">{{ v.texto }}</a></li>
            }
          </ul>
        }
      } @else if (vias()[0]; as unica) {
        <a class="credito__nombre" [href]="unica.href" target="_blank" rel="noopener">{{ dev.nombre }}</a>
      } @else {
        {{ dev.nombre }}
      }
    </span>
  `,
})
export class Credito {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly dev = DESARROLLADOR;
  protected readonly abierto = signal(false);

  protected readonly vias = computed<Via[]>(() => {
    const d = this.dev;
    const mensaje = 'Hola Thiago, vi tu trabajo en el sitio de Il Magro y quería consultarte por un proyecto.';
    return [
      d.linkedin ? { texto: 'LinkedIn', href: d.linkedin } : null,
      d.email ? { texto: 'Mail', href: `mailto:${d.email}` } : null,
      d.whatsapp ? { texto: 'WhatsApp', href: waMe(d.whatsapp, mensaje), wa: true } : null,
    ].filter((v): v is Via => v !== null);
  });

  /** Se cierra al tocar afuera o con Escape. */
  @HostListener('document:click', ['$event'])
  protected alTocarAfuera(e: Event): void {
    if (this.abierto() && !this.host.nativeElement.contains(e.target as Node)) this.abierto.set(false);
  }

  @HostListener('document:keydown.escape')
  protected alEscape(): void {
    this.abierto.set(false);
  }
}
