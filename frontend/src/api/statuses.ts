import type { ApplicationStatus } from '../types';
import { request } from './client';

export const statusesApi = {
  list: () => request<ApplicationStatus[]>('/application-statuses'),
};
