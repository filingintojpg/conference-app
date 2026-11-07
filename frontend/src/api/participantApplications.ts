import type { Application, CreateApplicationPayload, UpdateApplicationPayload } from '../types';
import { request } from './client';

export const participantApplicationsApi = {
  list: () => request<Application[]>('/applications'),
  create: (data: CreateApplicationPayload) =>
    request<Application>('/applications', { method: 'POST', body: data }),
  update: (id: number, data: UpdateApplicationPayload) =>
    request<Application>(`/applications/${id}`, { method: 'PUT', body: data }),
  withdraw: (id: number) =>
    request<Application>(`/applications/${id}/withdraw`, { method: 'POST' }),
};
