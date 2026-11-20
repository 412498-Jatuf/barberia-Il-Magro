import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Barbero, Corte, Horario, ReservarTurnoRequest, Servicio, TurnoACancelar, TurnoReservado } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  cortes(): Observable<Corte[]> {
    return this.http.get<Corte[]>(`${this.base}/cortes`);
  }

  servicios(): Observable<Servicio[]> {
    return this.http.get<Servicio[]>(`${this.base}/servicios`);
  }

  barberos(): Observable<Barbero[]> {
    return this.http.get<Barbero[]>(`${this.base}/barberos`);
  }

  horario(): Observable<Horario> {
    return this.http.get<Horario>(`${this.base}/horario`);
  }

  horariosDisponibles(fecha: string, barberoId: number, servicioId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/turnos/disponibles`, {
      params: { fecha, barberoId, servicioId },
    });
  }

  reservar(turno: ReservarTurnoRequest): Observable<TurnoReservado> {
    return this.http.post<TurnoReservado>(`${this.base}/turnos`, turno);
  }

  turnoACancelar(codigo: string): Observable<TurnoACancelar> {
    return this.http.get<TurnoACancelar>(`${this.base}/turnos/cancelacion/${encodeURIComponent(codigo)}`);
  }

  cancelarTurno(codigo: string): Observable<TurnoACancelar> {
    return this.http.post<TurnoACancelar>(`${this.base}/turnos/cancelacion/${encodeURIComponent(codigo)}`, null);
  }
}
