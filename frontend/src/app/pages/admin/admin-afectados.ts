import { Component, input } from '@angular/core';
import { fechaLarga } from '../../core/formato';
import { TurnoAdmin } from '../../core/models';

/**
 * Aviso de los turnos ya reservados que un cambio dejo afuera (otro horario, un cierre). No se cancelan
 * solos: Luca los ve aca y decide, desde la pestana Turnos.
 */
@Component({
  selector: 'app-admin-afectados',
  template: `
    @if (turnos().length) {
      <div class="aviso" role="status">
        <p>
          <strong>{{ turnos().length === 1 ? 'Hay 1 turno reservado' : 'Hay ' + turnos().length + ' turnos reservados' }}
            fuera de este horario.</strong>
          No se cancelaron: andá a la pestaña Turnos para cancelarlos o avisarle al cliente.
        </p>
        <ul>
          @for (t of turnos(); track t.id) {
            <li>
              {{ dia(t.fecha) }} · {{ t.horaInicio }} hs · {{ t.clienteNombre }} ·
              <a [href]="'tel:' + t.clienteTelefono">{{ t.clienteTelefono }}</a>
            </li>
          }
        </ul>
      </div>
    }
  `,
})
export class AdminAfectados {
  readonly turnos = input.required<TurnoAdmin[]>();

  protected dia(iso: string): string {
    const f = fechaLarga(iso);
    return f.charAt(0).toUpperCase() + f.slice(1);
  }
}
