import { API_URL, USER_HEADER } from '../config';

export class ApiError extends Error {
  constructor(status, message, fields = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fields = fields;
  }
}

function buildQuery(params) {
  if (!params) return '';
  const entries = Object.entries(params).filter(
    ([, value]) => value !== undefined && value !== null && value !== '',
  );
  const query = new URLSearchParams(entries).toString();
  return query ? `?${query}` : '';
}

/**
 * Единая точка общения с бэкендом.
 * userId - от чьего лица выполняется запрос (заголовок X-User-Id).
 */
export async function request(path, { method = 'GET', userId, params, body } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (userId !== undefined && userId !== null) headers[USER_HEADER] = String(userId);

  let response;
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
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    throw new ApiError(
      response.status,
      data?.error || `Ошибка ${response.status}`,
      data?.fields || {},
    );
  }
  return data;
}
