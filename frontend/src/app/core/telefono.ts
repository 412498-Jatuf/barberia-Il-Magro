/**
 * Numero para wa.me a partir de lo que escribio el cliente ("351 555 0101", "0351 15 555 0101",
 * "+54 9 351 555 0101"): solo digitos y con el codigo de pais (54 9 para un celular argentino).
 */
export function telefonoWhatsapp(telefono: string): string {
  let d = telefono.replace(/\D/g, '').replace(/^0+/, '');
  if (d.startsWith('54')) return d;
  // formato viejo "area 15 numero" (12 digitos): se saca el 15
  if (d.length === 12) {
    const area = [3, 2, 4].find((a) => d.slice(a, a + 2) === '15');
    if (area) d = d.slice(0, area) + d.slice(area + 2);
  }
  return `549${d}`;
}
