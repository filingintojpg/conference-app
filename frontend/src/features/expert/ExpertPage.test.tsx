import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { directionsApi, expertApplicationsApi } from '../../api';
import { makeDirection, makeExpertApplication } from '../../test/fixtures';
import ExpertPage from './ExpertPage';

vi.mock('../../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../api')>()),
  directionsApi: { list: vi.fn() },
  expertApplicationsApi: { list: vi.fn(), deleteAll: vi.fn() },
}));

vi.mock('../../context/AuthContext', () => ({
  useRequiredUser: () => ({ id: 300, fullName: 'Кузнецов Алексей', email: 'alexey@example.com', role: 'EXPERT' }),
}));

describe('ExpertPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
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

  describe('удаление всех заявок', () => {
    const deleteButton = () => screen.getByRole('button', { name: 'Удалить все заявки' });

    it('кнопка есть, если эксперт видит заявки', async () => {
      render(<ExpertPage />);

      await screen.findByText('Подтверждённый доклад');
      expect(deleteButton()).toBeInTheDocument();
    });

    it('кнопки нет, если заявок нет', async () => {
      vi.mocked(expertApplicationsApi.list).mockResolvedValue([]);

      render(<ExpertPage />);

      await screen.findByText('Подтверждённых заявок нет');
      expect(screen.queryByRole('button', { name: 'Удалить все заявки' })).not.toBeInTheDocument();
    });

    it('без подтверждения ничего не удаляется', async () => {
      vi.spyOn(window, 'confirm').mockReturnValue(false);
      render(<ExpertPage />);
      await screen.findByText('Подтверждённый доклад');

      await userEvent.click(deleteButton());

      expect(expertApplicationsApi.deleteAll).not.toHaveBeenCalled();
      expect(screen.getByText('Подтверждённый доклад')).toBeInTheDocument();
    });

    it('после подтверждения удаляет заявки, показывает результат и перезагружает список', async () => {
      const confirm = vi.spyOn(window, 'confirm').mockReturnValue(true);
      vi.mocked(expertApplicationsApi.deleteAll).mockResolvedValue({ deleted: 1 });
      render(<ExpertPage />);
      await screen.findByText('Подтверждённый доклад');
      vi.mocked(expertApplicationsApi.list).mockResolvedValue([]);

      await userEvent.click(deleteButton());

      expect(confirm).toHaveBeenCalledWith(expect.stringContaining('Программирование'));
      expect(expertApplicationsApi.deleteAll).toHaveBeenCalledTimes(1);
      expect(await screen.findByText('Удалено заявок: 1')).toBeInTheDocument();
      expect(await screen.findByText('Подтверждённых заявок нет')).toBeInTheDocument();
      await waitFor(() =>
        expect(screen.queryByRole('button', { name: 'Удалить все заявки' })).not.toBeInTheDocument(),
      );
    });

    it('ошибка удаления показывается пользователю, заявки остаются', async () => {
      vi.spyOn(window, 'confirm').mockReturnValue(true);
      vi.mocked(expertApplicationsApi.deleteAll).mockRejectedValue(new Error('Expert is not assigned to a direction'));
      render(<ExpertPage />);
      await screen.findByText('Подтверждённый доклад');

      await userEvent.click(deleteButton());

      expect(await screen.findByText('Expert is not assigned to a direction')).toBeInTheDocument();
      expect(await screen.findByText('Подтверждённый доклад')).toBeInTheDocument();
    });
  });
});
