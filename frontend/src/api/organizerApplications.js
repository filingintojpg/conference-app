import { request } from './client';

export const organizerApplicationsApi = {
  list: (userId, directionId, status) =>
    request('/organizer/applications', { userId, params: { directionId, status } }),
  get: (userId, id) => request(`/organizer/applications/${id}`, { userId }),
};
