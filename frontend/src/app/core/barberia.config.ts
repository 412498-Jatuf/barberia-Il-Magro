/**
 * Datos de la barberia que se muestran en el sitio.
 *
 * Los campos en null todavia no los paso la barberia: la pagina los oculta hasta que se completen.
 * Servicios, precios, barberos, horarios y fotos de cortes vienen del backend
 * (backend/.../infrastructure/seed/DatosIniciales.java y application.properties).
 */
export interface DatosBarberia {
  nombre: string;
  /** El barbero a cargo. */
  barbero: string;
  /** Calle y altura (ej. "Av. Corrientes 1234"). */
  calle: string | null;
  /** Barrio y ciudad (ej. "Almagro, Buenos Aires"). */
  barrio: string | null;
  /** WhatsApp en formato internacional sin "+" ni espacios (ej. "5491122334455"). */
  whatsapp: string | null;
  /** Usuario de Instagram sin "@". */
  instagram: string | null;
  nosotros: string[];
}

export const BARBERIA: DatosBarberia = {
  nombre: 'Il Magro',
  barbero: 'Luca Fracassi',
  calle: 'Av. Pueyrredón 2581',
  barrio: 'Córdoba capital',
  whatsapp: '5493516598837',
  instagram: null,
  nosotros: [
    'Il Magro es la barbería de Luca Fracassi. Tiene 21 años y cortar el pelo es lo que más le apasiona: fades, flequillos, mullets y cortes a tijera.',
    'Atiende con turno, de a un cliente por vez, en su local de Av. Pueyrredón, en Córdoba.',
  ],
};

/** Link a Google Maps, solo cuando hay calle (sin calle no se muestra). */
export function linkMapa(b: DatosBarberia): string | null {
  if (!b.calle) return null;
  const q = [b.calle, b.barrio].filter(Boolean).join(', ');
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(q)}`;
}

/** Link a WhatsApp con un mensaje inicial, o null si no hay numero. */
export function linkWhatsapp(b: DatosBarberia, mensaje = 'Hola, quería consultar por un turno'): string | null {
  if (!b.whatsapp) return null;
  return `https://wa.me/${b.whatsapp}?text=${encodeURIComponent(mensaje)}`;
}
