import { act, renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { useAsync } from './useAsync';

describe('useAsync', () => {
  it('сначала loading, потом данные', async () => {
    const fn = vi.fn().mockResolvedValue(['a', 'b']);

    const { result } = renderHook(() => useAsync(fn, []));

    expect(result.current.loading).toBe(true);
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toEqual(['a', 'b']);
    expect(result.current.error).toBeNull();
  });

  it('ошибка попадает в error', async () => {
    const fn = vi.fn().mockRejectedValue(new Error('Бэкенд недоступен'));

    const { result } = renderHook(() => useAsync(fn, []));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toBeNull();
    expect(result.current.error?.message).toBe('Бэкенд недоступен');
  });

  it('при enabled = false запрос не выполняется', () => {
    const fn = vi.fn().mockResolvedValue('x');

    const { result } = renderHook(() => useAsync(fn, [], { enabled: false }));

    expect(result.current.loading).toBe(false);
    expect(fn).not.toHaveBeenCalled();
  });

  it('reload повторяет запрос', async () => {
    const fn = vi.fn().mockResolvedValueOnce('first').mockResolvedValueOnce('second');

    const { result } = renderHook(() => useAsync(fn, []));
    await waitFor(() => expect(result.current.data).toBe('first'));

    act(() => result.current.reload());

    await waitFor(() => expect(result.current.data).toBe('second'));
    expect(fn).toHaveBeenCalledTimes(2);
  });
});
