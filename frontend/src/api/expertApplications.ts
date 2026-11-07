import type { ExpertApplication } from '../types';
import { request } from './client';

export const expertApplicationsApi = {
  // Эксперт привязан к одному направлению на бэкенде, фильтр по направлению не нужен.
  list: () => request<ExpertApplication[]>('/expert/applications'),
};
