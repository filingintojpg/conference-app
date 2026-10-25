// Адрес бэкенда. Меняется через frontend/.env (VITE_API_URL).
export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Заголовок, по которому бэкенд определяет, от чьего лица выполняется действие.
export const USER_HEADER = 'X-User-Id';

export const STORAGE_KEYS = {
  currentUserId: 'conference.currentUserId',
};

// Коды ролей и статусов - часть контракта с бэкендом.
// Сами списки (пользователи, направления, статусы) приходят с бэкенда.
export const ROLES = {
  PARTICIPANT: 'PARTICIPANT',
  ORGANIZER: 'ORGANIZER',
};

export const APPLICATION_STATUS = {
  SUBMITTED: 'SUBMITTED',
  EDITED: 'EDITED',
  WITHDRAWN: 'WITHDRAWN',
};

// Подписи для отображения. Для неизвестного кода показываем сам код.
export const ROLE_LABELS = {
  PARTICIPANT: 'участник',
  ORGANIZER: 'организатор',
};

export const STATUS_LABELS = {
  SUBMITTED: 'Подана',
  EDITED: 'Изменена',
  WITHDRAWN: 'Отозвана',
};

export const labelOf = (dictionary, code) => dictionary[code] ?? code;
