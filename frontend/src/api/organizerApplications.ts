import type { Application } from '../types';
import { request } from './client';

export const organizerApplicationsApi = {
  // status и reviewStatus необязательны: пустое значение означает "любой".
  list: (userId: number, directionId: number, status: string, reviewStatus: string) =>
    request<Application[]>('/organizer/applications', {
      userId,
      params: { directionId, status, reviewStatus },
    }),
  get: (userId: number, id: number) =>
    request<Application>(`/organizer/applications/${id}`, { userId }),
  approve: (userId: number, id: number) =>
    request<Application>(`/organizer/applications/${id}/approve`, { method: 'POST', userId }),
  reject: (userId: number, id: number) =>
    request<Application>(`/organizer/applications/${id}/reject`, { method: 'POST', userId }),
  // Передача заявки на другое направление и ответ организатора-получателя.
  transfer: (userId: number, id: number, targetDirectionId: number) =>
    request<Application>(`/organizer/applications/${id}/transfer`, {
      method: 'POST',
      userId,
      body: { targetDirectionId },
    }),
  acceptTransfer: (userId: number, id: number) =>
    request<Application>(`/organizer/applications/${id}/transfer/accept`, {
      method: 'POST',
      userId,
    }),
  rejectTransfer: (userId: number, id: number) =>
    request<Application>(`/organizer/applications/${id}/transfer/reject`, {
      method: 'POST',
      userId,
    }),
};
