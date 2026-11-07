import { API_URL, STORAGE_KEYS } from '../config';

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

// ---- Токен доступа (JWT). Хранится в localStorage, чтобы вход переживал перезагрузку страницы.

export function getAccessToken(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEYS.accessToken);
  } catch {
    return null;
  }
}

export function setAccessToken(token: string): void {
  try {
    localStorage.setItem(STORAGE_KEYS.accessToken, token);
  } catch {
    // localStorage недоступен - вход не переживёт перезагрузку.
  }
}

export function clearAccessToken(): void {
  try {
    localStorage.removeItem(STORAGE_KEYS.accessToken);
  } catch {
    // ничего не делаем
  }
}

// Вызывается, когда сервер ответил 401 на запрос с токеном (токен истёк или недействителен).
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
  /** false - не отправлять токен и не считать 401 «сессия истекла» (вход и регистрация). */
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
  const token = auth ? getAccessToken() : null;
  if (token) headers.Authorization = `Bearer ${token}`;

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}${buildQuery(params)}`, {
      method,
      headers,
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
    if (response.status === 401 && auth && token) unauthorizedHandler?.();
    const errorBody = (data ?? {}) as ErrorBody;
    throw new ApiError(
      response.status,
      errorBody.error || `Ошибка ${response.status}`,
      errorBody.fields || {},
    );
  }
  return data as T;
}
