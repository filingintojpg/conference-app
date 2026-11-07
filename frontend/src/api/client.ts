import { API_URL, CSRF_COOKIE, CSRF_HEADER } from '../config';

export type FieldErrors = Record<string, string>;

export class ApiError extends Error {
  readonly status: number;
  readonly fields: FieldErrors;

  constructor(status: number, message: string, fields: FieldErrors = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fields = fields;
  }
}

// ---- Авторизация: JWT лежит в httpOnly-cookie, JavaScript его не видит и не хранит.
// Браузер сам прикладывает cookie к каждому запросу (credentials: 'include').
// От CSRF защищает заголовок X-XSRF-TOKEN: его значение берётся из cookie XSRF-TOKEN,
// которую читает только наш сайт; чужой сайт прочитать её и подставить заголовок не может.

function readCookie(name: string): string | null {
  const prefix = `${encodeURIComponent(name)}=`;
  for (const part of document.cookie.split('; ')) {
    if (part.startsWith(prefix)) return decodeURIComponent(part.slice(prefix.length));
  }
  return null;
}

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

// Вызывается, когда сервер ответил 401 на защищённый запрос (cookie истекла или недействительна).
let unauthorizedHandler: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

// ---- Запросы

type QueryValue = string | number | null | undefined;

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  params?: Record<string, QueryValue>;
  body?: unknown;
  /** false - 401 не считается «сессия истекла» (вход и регистрация). */
  auth?: boolean;
}

interface ErrorBody {
  error?: string;
  fields?: FieldErrors;
}

function buildQuery(params?: Record<string, QueryValue>): string {
  if (!params) return '';
  const entries = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => [key, String(value)] as [string, string]);
  const query = new URLSearchParams(entries).toString();
  return query ? `?${query}` : '';
}

export async function request<T>(
  path: string,
  { method = 'GET', params, body, auth = true }: RequestOptions = {},
): Promise<T> {
  const headers: Record<string, string> = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (!SAFE_METHODS.has(method)) {
    const csrfToken = readCookie(CSRF_COOKIE);
    if (csrfToken) headers[CSRF_HEADER] = csrfToken;
  }

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}${buildQuery(params)}`, {
      method,
      headers,
      credentials: 'include',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Бэкенд недоступен. Проверьте, что он запущен.');
  }

  const text = await response.text();
  let data: unknown = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    if (response.status === 401 && auth) unauthorizedHandler?.();
    const errorBody = (data ?? {}) as ErrorBody;
    throw new ApiError(
      response.status,
      errorBody.error || `Ошибка ${response.status}`,
      errorBody.fields || {},
    );
  }
  return data as T;
}
