import { request } from './client';

export const directionsApi = {
  list: () => request('/directions'),
};
