const moneda = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'ARS',
  maximumFractionDigits: 0,
});

export function formatearPrecio(valor: number): string {
  return moneda.format(valor);
}

/** yyyy-MM-dd en hora local (sin pasar por UTC). */
export function aIsoLocal(fecha: Date): string {
  const m = String(fecha.getMonth() + 1).padStart(2, '0');
  const d = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${m}-${d}`;
}

export function desdeIsoLocal(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d);
}

/** Dia de la semana ISO-8601: 1 = lunes ... 7 = domingo. */
export function diaIso(fecha: Date): number {
  const d = fecha.getDay();
  return d === 0 ? 7 : d;
}

export const NOMBRES_DIAS = ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

export function fechaLarga(iso: string): string {
  return desdeIsoLocal(iso).toLocaleDateString('es-AR', { weekday: 'long', day: 'numeric', month: 'long' });
}
