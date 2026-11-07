import type { Application } from '../types';
import { request } from './client';

export const organizerApplicationsApi = {
  // status и reviewStatus необязательны: пустое значение означает "любой".
  list: (directionId: number, status: string, reviewStatus: string) =>
    request<Application[]>('/organizer/applications', {
      params: { directionId, status, reviewStatus },
    }),
  get: (id: number) => request<Application>(`/organizer/applications/${id}`),
  approve: (id: number) =>
    request<Application>(`/organizer/applications/${id}/approve`, { method: 'POST' }),
  reject: (id: number) =>
    request<Application>(`/organizer/applications/${id}/reject`, { method: 'POST' }),
  // Передача заявки на другое направление и ответ организатора-получателя.
  transfer: (id: number, targetDirectionId: number) =>
    request<Application>(`/organizer/applications/${id}/transfer`, {
      method: 'POST',
      body: { targetDirectionId },
    }),
  acceptTransfer: (id: number) =>
    request<Application>(`/organizer/applications/${id}/transfer/accept`, { method: 'POST' }),
  rejectTransfer: (id: number) =>
    request<Application>(`/organizer/applications/${id}/transfer/reject`, { method: 'POST' }),
};
