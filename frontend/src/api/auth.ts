import type { AuthResponse, LoginPayload, RegisterPayload, User } from '../types';
import { request } from './client';

export const authApi = {
  login: (data: LoginPayload) =>
    request<AuthResponse>('/auth/login', { method: 'POST', body: data, auth: false }),
  register: (data: RegisterPayload) =>
    request<AuthResponse>('/auth/register', { method: 'POST', body: data, auth: false }),
  // Текущий пользователь по токену.
  me: () => request<User>('/auth/me'),
};
