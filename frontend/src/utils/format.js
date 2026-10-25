export function formatDateTime(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('ru-RU');
}

export function isPast(iso) {
  return Boolean(iso) && new Date(iso).getTime() <= Date.now();
}
