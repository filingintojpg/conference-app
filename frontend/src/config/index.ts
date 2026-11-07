import type { ApplicationStatus, ReviewStatus, Role } from '../types';

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const CSRF_COOKIE = 'XSRF-TOKEN';
export const CSRF_HEADER = 'X-XSRF-TOKEN';

export const ROLES = {
  PARTICIPANT: 'PARTICIPANT',
  ORGANIZER: 'ORGANIZER',
  EXPERT: 'EXPERT',
} as const satisfies Record<Role, Role>;

export const APPLICATION_STATUS = {
  SUBMITTED: 'SUBMITTED',
  EDITED: 'EDITED',
  WITHDRAWN: 'WITHDRAWN',
} as const satisfies Record<ApplicationStatus, ApplicationStatus>;

export const REVIEW_STATUS = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const satisfies Record<ReviewStatus, ReviewStatus>;

export const ROLE_LABELS: Record<Role, string> = {
  PARTICIPANT: 'участник',
  ORGANIZER: 'организатор',
  EXPERT: 'член экспертной комиссии',
};

export const STATUS_LABELS: Record<ApplicationStatus, string> = {
  SUBMITTED: 'Подана',
  EDITED: 'Изменена',
  WITHDRAWN: 'Отозвана',
};

export const REVIEW_STATUS_LABELS: Record<ReviewStatus, string> = {
  PENDING: 'Ожидает проверки',
  APPROVED: 'Прошла проверку',
  REJECTED: 'Не прошла проверку',
};

export const labelOf = (dictionary: Record<string, string>, code: string): string =>
  dictionary[code] ?? code;
