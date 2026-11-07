import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError, request, setUnauthorizedHandler } from './client';

const fetchMock = vi.fn();

function jsonResponse(status: number, body?: unknown): Response {
  return new Response(body === undefined ? null : JSON.stringify(body), { status });
}

function lastCall(): { url: string; init: RequestInit } {
  const [url, init] = fetchMock.mock.calls[fetchMock.mock.calls.length - 1] as [string, RequestInit];
  return { url, init };
}

function setCsrfCookie(value: string) {
  document.cookie = `XSRF-TOKEN=${value}; path=/`;
}

describe('api client: авторизация через httpOnly-cookie и CSRF', () => {
  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    setUnauthorizedHandler(null);
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('GET отправляет cookie (credentials: include) без CSRF-заголовка и без Authorization', async () => {
    setCsrfCookie('abc123');
    fetchMock.mockResolvedValue(jsonResponse(200, { ok: true }));

    await request('/directions');

    const { url, init } = lastCall();
    expect(url).toContain('/directions');
    expect(init.credentials).toBe('include');
    expect(init.headers).not.toHaveProperty('X-XSRF-TOKEN');
    expect(init.headers).not.toHaveProperty('Authorization');
  });

  it('POST добавляет значение cookie XSRF-TOKEN в заголовок X-XSRF-TOKEN', async () => {
    setCsrfCookie('abc123');
    fetchMock.mockResolvedValue(jsonResponse(200, { id: 1 }));

    await request('/applications', { method: 'POST', body: { title: 'T' } });

    const { init } = lastCall();
    expect(init.headers).toMatchObject({
      'X-XSRF-TOKEN': 'abc123',
      'Content-Type': 'application/json',
    });
    expect(init.body).toBe(JSON.stringify({ title: 'T' }));
  });

  it('POST без cookie XSRF-TOKEN уходит без заголовка', async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, {}));

    await request('/applications/1/withdraw', { method: 'POST' });

    expect(lastCall().init.headers).not.toHaveProperty('X-XSRF-TOKEN');
  });

  it('не использует localStorage для токена', async () => {
    const setItem = vi.spyOn(Storage.prototype, 'setItem');
    const getItem = vi.spyOn(Storage.prototype, 'getItem');
    fetchMock.mockResolvedValue(jsonResponse(200, {}));

    await request('/auth/me');

    expect(setItem).not.toHaveBeenCalled();
    expect(getItem).not.toHaveBeenCalled();
  });
});

describe('api client: запросы и ошибки', () => {
  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    setUnauthorizedHandler(null);
  });

  it('собирает query-параметры и пропускает пустые', async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, []));

    await request('/organizer/applications', {
      params: { directionId: 1, status: '', reviewStatus: null, other: undefined },
    });

    expect(lastCall().url.endsWith('/organizer/applications?directionId=1')).toBe(true);
  });

  it('пустое тело ответа (204) даёт null', async () => {
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }));

    await expect(request('/auth/logout', { method: 'POST' })).resolves.toBeNull();
  });

  it('ошибка бэкенда превращается в ApiError со статусом, текстом и ошибками полей', async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(400, { error: 'Validation failed', fields: { email: 'email must be a valid address' } }),
    );

    const error = await request('/auth/register', { method: 'POST', body: {}, auth: false }).catch(
      (e: unknown) => e,
    );

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 400,
      message: 'Validation failed',
      fields: { email: 'email must be a valid address' },
    });
  });

  it('недоступный бэкенд даёт ApiError со статусом 0', async () => {
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'));

    await expect(request('/directions')).rejects.toMatchObject({ status: 0 });
  });

  it('401 на защищённый запрос вызывает обработчик «сессия истекла»', async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    fetchMock.mockResolvedValue(jsonResponse(401, { error: 'Authentication required' }));

    await expect(request('/applications')).rejects.toMatchObject({ status: 401 });

    expect(handler).toHaveBeenCalledTimes(1);
  });

  it('401 при входе (auth: false) обработчик не вызывает', async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    fetchMock.mockResolvedValue(jsonResponse(401, { error: 'Invalid email or password' }));

    await expect(request('/auth/login', { method: 'POST', body: {}, auth: false })).rejects.toMatchObject({
      message: 'Invalid email or password',
    });

    expect(handler).not.toHaveBeenCalled();
  });
});
