import type { ExpertApplication } from '../types';
import { request } from './client';

export interface DeletedApplications {
  deleted: number;
}

export const expertApplicationsApi = {

  list: () => request<ExpertApplication[]>('/expert/applications'),
  deleteAll: () => request<DeletedApplications>('/expert/applications', { method: 'DELETE' }),
};
