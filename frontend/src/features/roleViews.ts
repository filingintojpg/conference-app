import type { ComponentType } from 'react';
import { ROLES } from '../config';
import type { Role } from '../types';
import ExpertPage from './expert/ExpertPage';
import OrganizerPage from './organizer/OrganizerPage';
import ParticipantPage from './participant/ParticipantPage';

// Какой экран показывать для какой роли.
// Новая роль или экран = новая папка в features/ + строка здесь.
export const roleViews: Record<Role, ComponentType> = {
  [ROLES.PARTICIPANT]: ParticipantPage,
  [ROLES.ORGANIZER]: OrganizerPage,
  [ROLES.EXPERT]: ExpertPage,
};
