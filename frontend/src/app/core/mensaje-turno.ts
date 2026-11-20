import { fechaLarga, formatearPrecio } from './formato';
import { TurnoReservado } from './models';

/** Lo que el cliente cargo en el formulario y no vuelve en la respuesta del backend. */
export interface DatosCliente {
  telefono: string;
  email: string | null;
  notas: string | null;
}

/**
 * Mensaje de WhatsApp del cliente al barbero con el turno recien reservado. Estructurado en
 * renglones con *negrita* de WhatsApp para que Luca lo lea de un vistazo.
 */
export function mensajeTurno(
  t: TurnoReservado,
  cliente: DatosCliente,
  duracionMinutos: number | null,
  barberia: string,
): string {
  const dia = fechaLarga(t.fecha);
  const servicio = duracionMinutos
    ? `${t.servicio} (${duracionMinutos} min · ${formatearPrecio(t.precio)})`
    : `${t.servicio} (${formatearPrecio(t.precio)})`;

  const renglones = [
    `Hola ${t.barbero.split(' ')[0]}! Reservé un turno en ${barberia} 💈`,
    '',
    `*Nombre:* ${t.clienteNombre}`,
    `*Teléfono:* ${cliente.telefono}`,
    ...(cliente.email ? [`*Email:* ${cliente.email}`] : []),
    `*Servicio:* ${servicio}`,
    `*Día:* ${dia.charAt(0).toUpperCase()}${dia.slice(1)}`,
    `*Hora:* ${t.horaInicio} a ${t.horaFin} hs`,
    ...(cliente.notas ? [`*Comentarios:* ${cliente.notas}`] : []),
    '',
    `Si se cancela, el horario se libera acá: ${linkCancelacion(t.codigoCancelacion)}`,
  ];
  return renglones.join('\n');
}

/** Link a la pagina que cancela el turno y libera el horario. */
export function linkCancelacion(codigo: string): string {
  return `${location.origin}/cancelar/${codigo}`;
}

/** Link wa.me que abre el chat con el barbero y el mensaje ya escrito. */
export function linkWhatsapp(numero: string, texto: string): string {
  return `https://wa.me/${numero}?text=${encodeURIComponent(texto)}`;
}
