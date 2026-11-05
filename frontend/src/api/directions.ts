import type { Direction } from '../types';
import { request } from './client';

export const directionsApi = {
  list: () => request<Direction[]>('/directions'),
};
