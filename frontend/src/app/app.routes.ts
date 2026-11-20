import { Routes } from '@angular/router';

export const routes: Routes = [
  // La landing la dibuja el componente raiz (App) directamente dentro de <main>.
  { path: '', pathMatch: 'full', children: [], title: 'Il Magro: barbería de fades y cortes en Córdoba' },
  {
    path: 'turnos',
    loadComponent: () => import('./pages/turnos/turnos').then((m) => m.Turnos),
    title: 'Reservar turno · Il Magro',
  },
  {
    path: 'cancelar/:codigo',
    loadComponent: () => import('./pages/cancelar/cancelar').then((m) => m.Cancelar),
    title: 'Cancelar turno · Il Magro',
  },
  { path: '**', redirectTo: '' },
];
