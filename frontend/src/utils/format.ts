export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '';
  return new Date(iso).toLocaleString('ru-RU');
}

export function isPast(iso: string | null | undefined): boolean {
  return Boolean(iso) && new Date(iso as string).getTime() <= Date.now();
}

export function errorMessage(err: unknown): string {
  return err instanceof Error ? err.message : String(err);
}
