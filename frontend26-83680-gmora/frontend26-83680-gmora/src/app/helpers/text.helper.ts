/**
 * Normaliza un texto para las búsquedas: minúsculas y sin acentos.
 *
 * @param texto Texto a normalizar.
 * @returns El texto en minúsculas y sin diacríticos.
 */
export function normalizarTexto(texto: string): string {
    return texto.toLocaleLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}