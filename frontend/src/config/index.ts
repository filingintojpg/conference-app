import type { ApplicationStatus, ReviewStatus, Role } from '../types';

// Адрес бэкенда. Меняется через frontend/.env (VITE_API_URL).
export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Имена CSRF-cookie и заголовка совпадают с настройками Spring Security (CookieCsrfTokenRepository).
export const CSRF_COOKIE = 'XSRF-TOKEN';
export const CSRF_HEADER = 'X-XSRF-TOKEN';

// Коды ролей и статусов - часть контракта с бэкендом.
// Сами списки (пользователи, направления, статусы) приходят с бэкенда.
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

// Подписи для отображения. Для неизвестного кода показываем сам код.
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

// Результат проверки заявки организатором.
export const REVIEW_STATUS_LABELS: Record<ReviewStatus, string> = {
  PENDING: 'Ожидает проверки',
  APPROVED: 'Прошла проверку',
  REJECTED: 'Не прошла проверку',
};

export const labelOf = (dictionary: Record<string, string>, code: string): string =>
  dictionary[code] ?? code;
