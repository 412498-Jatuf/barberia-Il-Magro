import { fechaLarga } from './formato';
import { TurnoAdmin } from './models';
import { telefonoWhatsapp } from './telefono';

/** Mensaje de Luca al cliente cuando le cancela un turno, con el link para reservar otro horario. */
export function mensajeCancelacionAlCliente(t: TurnoAdmin, linkReservar: string): string {
  const dia = fechaLarga(t.fecha);
  return [
    `Hola ${t.clienteNombre.split(' ')[0]}! Te escribo de Il Magro.`,
    '',
    `Tuve que cancelar tu turno del ${dia} a las ${t.horaInicio} hs. Disculpá el inconveniente.`,
    '',
    `Si querés, podés reservar otro horario acá: ${linkReservar}`,
  ].join('\n');
}

/** Link wa.me al chat con el cliente y el mensaje ya escrito. */
export function linkWhatsappCliente(t: TurnoAdmin, texto: string): string {
  return `https://wa.me/${telefonoWhatsapp(t.clienteTelefono)}?text=${encodeURIComponent(texto)}`;
}
