import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { directionsApi, participantApplicationsApi } from '../../api';
import type { Application, Direction } from '../../types';
import ParticipantPage from './ParticipantPage';

vi.mock('../../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../api')>()),
  directionsApi: { list: vi.fn() },
  participantApplicationsApi: { list: vi.fn(), create: vi.fn(), update: vi.fn(), withdraw: vi.fn() },
}));

vi.mock('../../context/AuthContext', () => ({
  useRequiredUser: () => ({ id: 1, fullName: 'Иванов Иван', email: 'ivan@example.com', role: 'PARTICIPANT' }),
}));

const direction: Direction = {
  id: 10,
  name: 'Программирование',
  deadline: '2099-01-01T00:00:00Z',
  organizerId: 100,
  organizerFullName: 'Сидорова Мария',
  expertId: null,
};

const application: Application = {
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
};

describe('ParticipantPage', () => {
  beforeEach(() => {
    vi.mocked(directionsApi.list).mockResolvedValue([direction]);
    vi.mocked(participantApplicationsApi.list).mockReset();
    vi.mocked(participantApplicationsApi.withdraw).mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('без заявок показывает пустой список', async () => {
    vi.mocked(participantApplicationsApi.list).mockResolvedValue([]);

    render(<ParticipantPage />);

    expect(await screen.findByText('У вас пока нет заявок')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Мои заявки' })).toBeInTheDocument();
  });

  it('отзыв заявки: после подтверждения вызывает API и показывает сообщение', async () => {
    vi.mocked(participantApplicationsApi.list).mockResolvedValue([application]);
    vi.mocked(participantApplicationsApi.withdraw).mockResolvedValue({ ...application, status: 'WITHDRAWN' });
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    render(<ParticipantPage />);

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Отозвать' }));

    expect(participantApplicationsApi.withdraw).toHaveBeenCalledWith(5);
    expect(await screen.findByText('Заявка отозвана')).toBeInTheDocument();
  });

  it('если участник не подтвердил отзыв, запрос не отправляется', async () => {
    vi.mocked(participantApplicationsApi.list).mockResolvedValue([application]);
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    render(<ParticipantPage />);

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Отозвать' }));

    expect(participantApplicationsApi.withdraw).not.toHaveBeenCalled();
  });

  it('проверенную заявку нельзя редактировать, только отозвать', async () => {
    vi.mocked(participantApplicationsApi.list).mockResolvedValue([{ ...application, reviewStatus: 'APPROVED' }]);

    render(<ParticipantPage />);

    expect(await screen.findByRole('button', { name: 'Редактировать' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Отозвать' })).toBeEnabled();
  });

  it('отозванную заявку нельзя ни редактировать, ни отозвать повторно', async () => {
    vi.mocked(participantApplicationsApi.list).mockResolvedValue([{ ...application, status: 'WITHDRAWN' }]);

    render(<ParticipantPage />);

    expect(await screen.findByRole('button', { name: 'Редактировать' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Отозвать' })).toBeDisabled();
  });
});
