import type { Application, Direction, ExpertApplication, User } from '../types';

export const ivan: User = { id: 1, fullName: 'Иванов Иван', email: 'ivan@example.com', role: 'PARTICIPANT' };

export function makeDirection(overrides: Partial<Direction> = {}): Direction {
  return {
    id: 10,
    name: 'Программирование',
    deadline: '2099-01-01T00:00:00Z',
    organizerId: 100,
    organizerFullName: 'Сидорова Мария',
    expertId: null,
    ...overrides,
  };
}

export function makeApplication(overrides: Partial<Application> = {}): Application {
  return {
    id: 5,
    directionId: 10,
    directionName: 'Программирование',
    title: 'Spring Boot',
    abstractText: 'Краткое описание',
    content: 'Полный текст',
    createdAt: '2026-10-10T12:00:00Z',
    updatedAt: '2026-10-10T12:00:00Z',
    participantId: 1,
    participantFullName: 'Иванов Иван',
    participantEmail: 'ivan@example.com',
    status: 'SUBMITTED',
    reviewStatus: 'PENDING',
    transferredFromDirectionId: null,
    transferredFromDirectionName: null,
    transferToDirectionId: null,
    transferToDirectionName: null,
    ...overrides,
  };
}

export function makeExpertApplication(overrides: Partial<ExpertApplication> = {}): ExpertApplication {
  return {
    id: 7,
    directionId: 10,
    directionName: 'Программирование',
    title: 'Подтверждённый доклад',
    abstractText: 'Аннотация',
    content: 'Содержание',
    createdAt: '2026-10-10T12:00:00Z',
    updatedAt: '2026-10-10T12:00:00Z',
    ...overrides,
  };
}
