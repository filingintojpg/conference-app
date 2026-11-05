import type { ReviewStatus } from '../types';
import { request } from './client';

export const reviewStatusesApi = {
  list: () => request<ReviewStatus[]>('/review-statuses'),
};
