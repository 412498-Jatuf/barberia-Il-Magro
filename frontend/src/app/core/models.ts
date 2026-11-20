export interface Corte {
  id: number;
  nombre: string;
  descripcion: string | null;
  categoria: string | null;
  imagenUrl: string;
}

export interface Servicio {
  id: number;
  nombre: string;
  descripcion: string | null;
  duracionMinutos: number;
  precio: number;
}

export interface Barbero {
  id: number;
  nombre: string;
  especialidad: string | null;
  fotoUrl: string | null;
}

export interface Horario {
  apertura: string;
  cierre: string;
  intervaloMinutos: number;
  /** ISO-8601: 1 = lunes ... 7 = domingo */
  diasAtencion: number[];
  diasAnticipacion: number;
}

export interface ReservarTurnoRequest {
  clienteNombre: string;
  clienteTelefono: string;
  clienteEmail: string | null;
  servicioId: number;
  barberoId: number;
  /** yyyy-MM-dd */
  fecha: string;
  /** HH:mm */
  hora: string;
  notas: string | null;
}

export interface TurnoReservado {
  id: number;
  clienteNombre: string;
  servicio: string;
  precio: number;
  barbero: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: string;
  /** Secreto del link de cancelacion (/cancelar/:codigo). */
  codigoCancelacion: string;
}

/** El turno visto desde su link de cancelacion. */
export interface TurnoACancelar {
  clienteNombre: string;
  servicio: string;
  barbero: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: 'CONFIRMADO' | 'CANCELADO';
  cancelable: boolean;
}

export interface ApiError {
  mensaje: string;
  campos?: Record<string, string>;
}
