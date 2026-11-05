import { API_URL, USER_HEADER } from '../config';

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

type QueryValue = string | number | null | undefined;

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  userId?: number | null;
  params?: Record<string, QueryValue>;
  body?: unknown;
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
  { method = 'GET', userId, params, body }: RequestOptions = {},
): Promise<T> {
  const headers: Record<string, string> = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (userId !== undefined && userId !== null) headers[USER_HEADER] = String(userId);

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
    const errorBody = (data ?? {}) as ErrorBody;
    throw new ApiError(
      response.status,
      errorBody.error || `Ошибка ${response.status}`,
      errorBody.fields || {},
    );
  }
  return data as T;
}
