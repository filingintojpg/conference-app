import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { directionsApi, expertApplicationsApi } from '../../api';
import { makeDirection, makeExpertApplication } from '../../test/fixtures';
import ExpertPage from './ExpertPage';

vi.mock('../../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../api')>()),
  directionsApi: { list: vi.fn() },
  expertApplicationsApi: { list: vi.fn() },
}));

vi.mock('../../context/AuthContext', () => ({
  useRequiredUser: () => ({ id: 300, fullName: 'Кузнецов Алексей', email: 'alexey@example.com', role: 'EXPERT' }),
}));

describe('ExpertPage', () => {
  beforeEach(() => {
    vi.mocked(directionsApi.list).mockResolvedValue([
      makeDirection({ id: 10, name: 'Программирование', expertId: 300 }),
      makeDirection({ id: 20, name: 'Дизайн', expertId: 301 }),
    ]);
    vi.mocked(expertApplicationsApi.list).mockResolvedValue([makeExpertApplication()]);
  });

  it('показывает направление эксперта и подтверждённые заявки', async () => {
    render(<ExpertPage />);

    expect(screen.getByRole('heading', { name: 'Подтверждённые заявки' })).toBeInTheDocument();
    expect(await screen.findByText('Подтверждённый доклад')).toBeInTheDocument();
    expect(await screen.findByText('Программирование', { selector: 'strong' })).toBeInTheDocument();
  });

  it('не показывает данные об авторах заявок', async () => {
    render(<ExpertPage />);

    await screen.findByText('Подтверждённый доклад');
    expect(screen.queryByText(/Участник:/)).not.toBeInTheDocument();
  });

  it('без заявок показывает пустое состояние', async () => {
    vi.mocked(expertApplicationsApi.list).mockResolvedValue([]);

    render(<ExpertPage />);

    expect(await screen.findByText('Подтверждённых заявок нет')).toBeInTheDocument();
  });

  it('ошибка загрузки заявок показывается пользователю', async () => {
    vi.mocked(expertApplicationsApi.list).mockRejectedValue(new Error('Your role has no access to this resource'));

    render(<ExpertPage />);

    expect(await screen.findByText('Your role has no access to this resource')).toBeInTheDocument();
  });
});
