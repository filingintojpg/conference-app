import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { errorMessage, formatDateTime, isPast } from './format';

describe('formatDateTime', () => {
  it('возвращает пустую строку для пустых значений', () => {
    expect(formatDateTime(null)).toBe('');
    expect(formatDateTime(undefined)).toBe('');
    expect(formatDateTime('')).toBe('');
  });

  it('форматирует корректную дату', () => {
    expect(formatDateTime('2026-10-10T12:00:00Z')).toContain('2026');
  });
});

describe('isPast', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-06-01T00:00:00Z'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('true для даты в прошлом', () => {
    expect(isPast('2026-01-01T00:00:00Z')).toBe(true);
  });

  it('false для даты в будущем', () => {
    expect(isPast('2026-12-01T00:00:00Z')).toBe(false);
  });

  it('false для пустого значения', () => {
    expect(isPast(null)).toBe(false);
    expect(isPast(undefined)).toBe(false);
  });
});

describe('errorMessage', () => {
  it('берёт message у Error', () => {
    expect(errorMessage(new Error('boom'))).toBe('boom');
  });

  it('приводит остальные значения к строке', () => {
    expect(errorMessage('просто текст')).toBe('просто текст');
    expect(errorMessage(404)).toBe('404');
  });
});
