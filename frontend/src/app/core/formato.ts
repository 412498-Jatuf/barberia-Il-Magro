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

/** "Martes a sábado" a partir de [2,3,4,5,6]. */
export function describirDias(dias: number[]): string {
  if (!dias.length) return '';
  const ordenados = [...dias].sort((a, b) => a - b);
  const consecutivos = ordenados.every((d, i) => i === 0 || d === ordenados[i - 1] + 1);
  if (consecutivos && ordenados.length > 2) {
    return `${NOMBRES_DIAS[ordenados[0]]} a ${NOMBRES_DIAS[ordenados.at(-1)!].toLowerCase()}`;
  }
  return ordenados.map((d) => NOMBRES_DIAS[d]).join(', ');
}
