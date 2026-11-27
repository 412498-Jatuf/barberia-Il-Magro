import { Component, DestroyRef, OnInit, ViewEncapsulation, inject, signal } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { AdminService } from '../../core/admin.service';
import { AdminCierres } from './admin-cierres';
import { AdminHorario } from './admin-horario';
import { AdminLogin } from './admin-login';
import { AdminPrecios } from './admin-precios';
import { AdminTurnos } from './admin-turnos';

type Pestana = 'turnos' | 'horario' | 'cierres' | 'precios';

/**
 * Panel de Luca (/admin): ingreso y, adentro, cuatro pestanas: los turnos (ver y cancelar), el horario de
 * la semana, los cierres de dias u horas y los precios de los servicios. La pagina no se linkea desde el sitio ni la indexan los buscadores.
 */
@Component({
  selector: 'app-admin',
  imports: [AdminLogin, AdminTurnos, AdminHorario, AdminCierres, AdminPrecios],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
  // los estilos del panel cubren tambien a sus pestanas (cuelgan de .admin)
  encapsulation: ViewEncapsulation.None,
})
export class Admin implements OnInit {
  protected readonly admin = inject(AdminService);
  private readonly meta = inject(Meta);

  protected readonly pestana = signal<Pestana>('turnos');
  protected readonly pestanas: { id: Pestana; nombre: string }[] = [
    { id: 'turnos', nombre: 'Turnos' },
    { id: 'horario', nombre: 'Horario' },
    { id: 'cierres', nombre: 'Cierres' },
    { id: 'precios', nombre: 'Precios' },
  ];

  constructor() {
    const robots = this.meta.addTag({ name: 'robots', content: 'noindex, nofollow' });
    inject(DestroyRef).onDestroy(() => robots && this.meta.removeTagElement(robots));
  }

  ngOnInit(): void {
    this.admin.comprobarSesion();
  }

  protected salir(): void {
    this.admin.salir().subscribe({ error: () => this.admin.usuario.set(null) });
  }
}
