import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { BloqueoAgenda, BloqueoCreado, DiaHorario, NuevoBloqueo, SemanaGuardada, TurnoAdmin } from './models';

interface Sesion {
  usuario: string;
}

/**
 * Acceso al panel de administracion: la sesion (cookie del servidor) y las acciones de Luca.
 * Si el servidor responde 401 en cualquier pedido (la sesion vencio), el panel vuelve al ingreso.
 */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/admin';

  /** undefined: todavia no se sabe; null: sin sesion; texto: el usuario que entro. */
  readonly usuario = signal<string | null | undefined>(undefined);

  comprobarSesion(): void {
    this.http.get<Sesion>(`${this.base}/yo`).subscribe({
      next: (s) => this.usuario.set(s.usuario),
      error: () => this.usuario.set(null),
    });
  }

  entrar(usuario: string, clave: string): Observable<void> {
    return this.http.post<Sesion>(`${this.base}/login`, { usuario, clave }).pipe(
      tap((s) => this.usuario.set(s.usuario)),
      map(() => undefined),
    );
  }

  salir(): Observable<void> {
    return this.http.post<void>(`${this.base}/logout`, null).pipe(tap(() => this.usuario.set(null)));
  }

  turnos(desde: string, hasta: string): Observable<TurnoAdmin[]> {
    return this.protegido(this.http.get<TurnoAdmin[]>(`${this.base}/turnos`, { params: { desde, hasta } }));
  }

  cancelarTurno(id: number): Observable<TurnoAdmin> {
    return this.protegido(this.http.post<TurnoAdmin>(`${this.base}/turnos/${id}/cancelar`, null));
  }

  semana(): Observable<DiaHorario[]> {
    return this.protegido(this.http.get<DiaHorario[]>(`${this.base}/semana`));
  }

  guardarSemana(dias: DiaHorario[]): Observable<SemanaGuardada> {
    return this.protegido(this.http.put<SemanaGuardada>(`${this.base}/semana`, { dias }));
  }

  bloqueos(): Observable<BloqueoAgenda[]> {
    return this.protegido(this.http.get<BloqueoAgenda[]>(`${this.base}/bloqueos`));
  }

  bloquear(bloqueo: NuevoBloqueo): Observable<BloqueoCreado> {
    return this.protegido(this.http.post<BloqueoCreado>(`${this.base}/bloqueos`, bloqueo));
  }

  quitarBloqueo(id: number): Observable<void> {
    return this.protegido(this.http.delete<void>(`${this.base}/bloqueos/${id}`));
  }

  private protegido<T>(pedido: Observable<T>): Observable<T> {
    return pedido.pipe(
      catchError((err) => {
        if (err?.status === 401) this.usuario.set(null);
        return throwError(() => err);
      }),
    );
  }
}
