import { request } from './client';

export const statusesApi = {
  list: () => request('/application-statuses'),
};
