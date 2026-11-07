import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Application } from '../../types';
import ApplicationCard from './ApplicationCard';

const application: Application = {
  id: 1,
  directionId: 10,
  directionName: 'Программирование',
  title: 'Spring Boot',
  abstractText: 'Краткое описание',
  content: 'Полный текст',
  createdAt: '2026-10-10T12:00:00Z',
  updatedAt: '2026-10-11T12:00:00Z',
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

describe('ApplicationCard', () => {
  it('показывает название, направление и статусы', () => {
    render(<ApplicationCard application={application} />);

    expect(screen.getByRole('heading', { name: 'Spring Boot' })).toBeInTheDocument();
    expect(screen.getByText(/Направление: Программирование/)).toBeInTheDocument();
    expect(screen.getByText('Подана')).toBeInTheDocument();
    expect(screen.getByText('Ожидает проверки')).toBeInTheDocument();
  });

  it('данные участника показываются только по флагу', () => {
    const { rerender } = render(<ApplicationCard application={application} />);
    expect(screen.queryByText(/Участник:/)).not.toBeInTheDocument();

    rerender(<ApplicationCard application={application} showParticipant />);
    expect(screen.getByText(/Участник: Иванов Иван \(ivan@example.com\)/)).toBeInTheDocument();
  });

  it('у отозванной непроверенной заявки статус проверки скрыт', () => {
    render(<ApplicationCard application={{ ...application, status: 'WITHDRAWN' }} />);

    expect(screen.getByText('Отозвана')).toBeInTheDocument();
    expect(screen.queryByText('Ожидает проверки')).not.toBeInTheDocument();
  });

  it('показывает ожидающую передачу на другое направление', () => {
    render(
      <ApplicationCard
        application={{
          ...application,
          transferredFromDirectionName: 'Дизайн',
          transferToDirectionId: 20,
          transferToDirectionName: 'Данные',
        }}
      />,
    );

    expect(screen.getByText('Передача ожидает решения')).toBeInTheDocument();
  });

  it('выводит переданные кнопки действий', () => {
    render(<ApplicationCard application={application} actions={<button type="button">Отозвать</button>} />);

    expect(screen.getByRole('button', { name: 'Отозвать' })).toBeInTheDocument();
  });
});
