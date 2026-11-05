import type { Role, User } from '../types';
import { request } from './client';

export const usersApi = {
  // role необязателен: без него вернутся все пользователи.
  list: (role?: Role) => request<User[]>('/users', { params: { role } }),
};
