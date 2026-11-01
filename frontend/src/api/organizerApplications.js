import { request } from './client';

export const organizerApplicationsApi = {
  // status и reviewStatus необязательны: пустое значение означает "любой".
  list: (userId, directionId, status, reviewStatus) =>
    request('/organizer/applications', { userId, params: { directionId, status, reviewStatus } }),
  get: (userId, id) => request(`/organizer/applications/${id}`, { userId }),
  approve: (userId, id) =>
    request(`/organizer/applications/${id}/approve`, { method: 'POST', userId }),
  reject: (userId, id) =>
    request(`/organizer/applications/${id}/reject`, { method: 'POST', userId }),
  // Передача заявки на другое направление и ответ организатора-получателя.
  transfer: (userId, id, targetDirectionId) =>
    request(`/organizer/applications/${id}/transfer`, {
      method: 'POST',
      userId,
      body: { targetDirectionId },
    }),
  acceptTransfer: (userId, id) =>
    request(`/organizer/applications/${id}/transfer/accept`, { method: 'POST', userId }),
  rejectTransfer: (userId, id) =>
    request(`/organizer/applications/${id}/transfer/reject`, { method: 'POST', userId }),
};
