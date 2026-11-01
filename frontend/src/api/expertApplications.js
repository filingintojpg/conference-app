import { request } from './client';

export const expertApplicationsApi = {
  // directionId необязателен: без него вернутся заявки всех направлений.
  list: (userId, directionId) =>
    request('/expert/applications', { userId, params: { directionId } }),
};
