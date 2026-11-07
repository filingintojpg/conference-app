import type { Page, Route } from '@playwright/test';

export interface MockUser {
  id: number;
  fullName: string;
  email: string;
  role: 'PARTICIPANT' | 'ORGANIZER' | 'EXPERT';
}

export const IVAN: MockUser = {
  id: 1,
  fullName: 'Иванов Иван',
  email: 'ivan@example.com',
  role: 'PARTICIPANT',
};

export const VALID_PASSWORD = 'secret-pass';
export const CSRF_VALUE = 'test-csrf-token';

const API = 'http://localhost:8080';
const ORIGIN = 'http://localhost:5173';

export interface RecordedRequest {
  method: string;
  path: string;
  csrfHeader: string | undefined;
}

export interface MockApi {
  /** Все запросы, дошедшие до «бэкенда». */
  requests: RecordedRequest[];
  /** Вошедший пользователь (имитация httpOnly-cookie на сервере). null - сессии нет. */
  setSession: (user: MockUser | null) => void;
}

const corsHeaders = {
  'Access-Control-Allow-Origin': ORIGIN,
  'Access-Control-Allow-Credentials': 'true',
  'Access-Control-Allow-Headers': 'Content-Type, X-XSRF-TOKEN, Authorization',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
};

function reply(route: Route, status: number, body?: unknown) {
  return route.fulfill({
    status,
    headers: corsHeaders,
    contentType: 'application/json',
    body: body === undefined ? '' : JSON.stringify(body),
  });
}

/**
 * Подменяет бэкенд (http://localhost:8080) в браузере: логин, регистрация, /auth/me, выход и списки.
 * Сессию имитируем переменной: настоящая cookie тут не нужна, важно, что делает фронтенд.
 * Cookie XSRF-TOKEN кладём в браузер сами, как это сделал бы сервер.
 */
export async function mockApi(page: Page, initialUser: MockUser | null = null): Promise<MockApi> {
  let session = initialUser;
  const requests: RecordedRequest[] = [];

  await page.context().addCookies([{ name: 'XSRF-TOKEN', value: CSRF_VALUE, url: ORIGIN }]);

  await page.route(`${API}/**`, async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const method = request.method();

    if (method === 'OPTIONS') {
      return route.fulfill({ status: 204, headers: corsHeaders });
    }
    requests.push({ method, path, csrfHeader: request.headers()['x-xsrf-token'] });

    if (path === '/auth/me' && method === 'GET') {
      return session ? reply(route, 200, session) : reply(route, 401, { error: 'Authentication required' });
    }
    if (path === '/auth/login' && method === 'POST') {
      const { email, password } = request.postDataJSON() as { email: string; password: string };
      if (email === IVAN.email && password === VALID_PASSWORD) {
        session = IVAN;
        return reply(route, 200, IVAN);
      }
      return reply(route, 401, { error: 'Invalid email or password' });
    }
    if (path === '/auth/register' && method === 'POST') {
      session = IVAN;
      return reply(route, 201, IVAN);
    }
    if (path === '/auth/logout' && method === 'POST') {
      session = null;
      return reply(route, 204);
    }
    if (path === '/directions' || path === '/applications') {
      return session ? reply(route, 200, []) : reply(route, 401, { error: 'Authentication required' });
    }
    return reply(route, 404, { error: `Нет подмены для ${method} ${path}` });
  });

  return {
    requests,
    setSession: (user) => {
      session = user;
    },
  };
}
