import type { ExpertApplication } from '../types';
import { request } from './client';

export const expertApplicationsApi = {

  list: () => request<ExpertApplication[]>('/expert/applications'),
};
