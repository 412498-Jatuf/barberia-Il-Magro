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

/** Un tramo de horas de un dia ("15:00" a "18:00"). */
export interface TramoHorario {
  desde: string;
  hasta: string;
}

/** Un dia de la semana con sus tramos; mas de uno significa que hay pausa. */
export interface DiaHorario {
  /** ISO-8601: 1 = lunes ... 7 = domingo */
  dia: number;
  tramos: TramoHorario[];
}

export interface Horario {
  intervaloMinutos: number;
  diasAnticipacion: number;
  /** Solo los dias que se atienden; los que no estan, estan cerrados. */
  semana: DiaHorario[];
  /** yyyy-MM-dd: fechas proximas que se cerraron enteras (feriado, viaje). */
  cerrados: string[];
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
  /** Como le avisa el cliente a Luca: por WhatsApp (se abre el chat) o por email (sale del servidor). */
  avisoPor: AvisoPor;
}

export type AvisoPor = 'whatsapp' | 'email';

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

// ---------- panel de administracion (/admin) ----------

/** Un turno tal como lo ve Luca: con el telefono del cliente para poder avisarle. */
export interface TurnoAdmin {
  id: number;
  clienteNombre: string;
  clienteTelefono: string;
  clienteEmail: string | null;
  servicio: string;
  duracionMinutos: number;
  barbero: string;
  /** yyyy-MM-dd */
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: 'CONFIRMADO' | 'CANCELADO';
  notas: string | null;
  cancelable: boolean;
}

/** Un cierre de la agenda: un dia entero (sin horas) o unas horas de un dia. */
export interface BloqueoAgenda {
  id: number;
  fecha: string;
  desde: string | null;
  hasta: string | null;
  motivo: string | null;
  diaCompleto: boolean;
}

export interface NuevoBloqueo {
  fecha: string;
  desde: string | null;
  hasta: string | null;
  motivo: string | null;
}

/** Tras cambiar el horario: lo guardado y los turnos que quedaron fuera (no se cancelan solos). */
export interface SemanaGuardada {
  semana: DiaHorario[];
  turnosAfectados: TurnoAdmin[];
}

export interface BloqueoCreado {
  bloqueo: BloqueoAgenda;
  turnosAfectados: TurnoAdmin[];
}
