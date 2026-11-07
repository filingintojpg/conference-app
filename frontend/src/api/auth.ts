import type { LoginPayload, RegisterPayload, User } from '../types';
import { request } from './client';

export const authApi = {
  login: (data: LoginPayload) =>
    request<User>('/auth/login', { method: 'POST', body: data, auth: false }),
  register: (data: RegisterPayload) =>
    request<User>('/auth/register', { method: 'POST', body: data, auth: false }),

  logout: () => request<void>('/auth/logout', { method: 'POST', auth: false }),

  me: () => request<User>('/auth/me'),
};
