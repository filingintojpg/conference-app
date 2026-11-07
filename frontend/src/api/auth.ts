import type { LoginPayload, RegisterPayload, User } from '../types';
import { request } from './client';

// Токен в теле ответов не приходит: сервер ставит его в httpOnly-cookie. Тело - только пользователь.
export const authApi = {
  login: (data: LoginPayload) =>
    request<User>('/auth/login', { method: 'POST', body: data, auth: false }),
  register: (data: RegisterPayload) =>
    request<User>('/auth/register', { method: 'POST', body: data, auth: false }),
  // Выход: сервер удаляет cookie (её нельзя удалить из JS, она httpOnly).
  logout: () => request<void>('/auth/logout', { method: 'POST', auth: false }),
  // Текущий пользователь по cookie.
  me: () => request<User>('/auth/me'),
};
