import { request } from './client';

export const usersApi = {
  // role необязателен: без него вернутся все пользователи.
  list: (role) => request('/users', { params: { role } }),
};
