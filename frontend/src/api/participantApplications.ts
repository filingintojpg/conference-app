import type { Application, CreateApplicationPayload, UpdateApplicationPayload } from '../types';
import { request } from './client';

export const participantApplicationsApi = {
  list: (userId: number) => request<Application[]>('/applications', { userId }),
  create: (userId: number, data: CreateApplicationPayload) =>
    request<Application>('/applications', { method: 'POST', userId, body: data }),
  update: (userId: number, id: number, data: UpdateApplicationPayload) =>
    request<Application>(`/applications/${id}`, { method: 'PUT', userId, body: data }),
  withdraw: (userId: number, id: number) =>
    request<Application>(`/applications/${id}/withdraw`, { method: 'POST', userId }),
};
