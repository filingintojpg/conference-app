import { request } from './client';

export const reviewStatusesApi = {
  list: () => request('/review-statuses'),
};
