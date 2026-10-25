import { request } from './client';

export const participantApplicationsApi = {
  list: (userId) => request('/applications', { userId }),
  create: (userId, data) => request('/applications', { method: 'POST', userId, body: data }),
  update: (userId, id, data) =>
    request(`/applications/${id}`, { method: 'PUT', userId, body: data }),
  withdraw: (userId, id) => request(`/applications/${id}/withdraw`, { method: 'POST', userId }),
};
