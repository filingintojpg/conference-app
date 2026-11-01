import { request } from './client';

export const expertApplicationsApi = {
  // Эксперт привязан к одному направлению на бэкенде, фильтр по направлению не нужен.
  list: (userId) => request('/expert/applications', { userId }),
};
