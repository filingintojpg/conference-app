import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { directionsApi, organizerApplicationsApi, reviewStatusesApi, statusesApi } from '../../api';
import { makeApplication, makeDirection } from '../../test/fixtures';
import OrganizerPage from './OrganizerPage';

const currentUser = vi.hoisted(() => ({ id: 100 }));

vi.mock('../../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../api')>()),
  directionsApi: { list: vi.fn() },
  statusesApi: { list: vi.fn() },
  reviewStatusesApi: { list: vi.fn() },
  organizerApplicationsApi: {
    list: vi.fn(),
    approve: vi.fn(),
    reject: vi.fn(),
    transfer: vi.fn(),
    acceptTransfer: vi.fn(),
    rejectTransfer: vi.fn(),
  },
}));

vi.mock('../../context/AuthContext', () => ({
  useRequiredUser: () => ({
    id: currentUser.id,
    fullName: 'Сидорова Мария',
    email: 'maria@example.com',
    role: 'ORGANIZER',
  }),
}));

const mine = makeDirection({ id: 10, name: 'Программирование', organizerId: 100 });
const alsoMine = makeDirection({ id: 30, name: 'Данные', organizerId: 100 });
const foreign = makeDirection({ id: 20, name: 'Дизайн', organizerId: 200, organizerFullName: 'Петров Пётр' });

const api = vi.mocked(organizerApplicationsApi);

describe('OrganizerPage', () => {
  beforeEach(() => {
    currentUser.id = 100;
    for (const fn of Object.values(api)) fn.mockReset();
    vi.mocked(directionsApi.list).mockResolvedValue([mine, foreign, alsoMine]);
    vi.mocked(statusesApi.list).mockResolvedValue(['SUBMITTED', 'EDITED', 'WITHDRAWN']);
    vi.mocked(reviewStatusesApi.list).mockResolvedValue(['PENDING', 'APPROVED', 'REJECTED']);
    api.list.mockResolvedValue([makeApplication()]);
    api.approve.mockResolvedValue(makeApplication({ reviewStatus: 'APPROVED' }));
    api.reject.mockResolvedValue(makeApplication({ reviewStatus: 'REJECTED' }));
    api.transfer.mockResolvedValue(makeApplication());
    api.acceptTransfer.mockResolvedValue(makeApplication());
    api.rejectTransfer.mockResolvedValue(makeApplication());
  });

  it('если у организатора нет направлений, сообщает об этом', async () => {
    currentUser.id = 999;

    render(<OrganizerPage />);

    expect(await screen.findByText('У этого организатора нет направлений')).toBeInTheDocument();
    expect(api.list).not.toHaveBeenCalled();
  });

  it('в фильтре только свои направления', async () => {
    render(<OrganizerPage />);

    const select = await screen.findByLabelText('Направление');

    const names = within(select)
      .getAllByRole('option')
      .map((option) => option.textContent);
    expect(names).toEqual(['Программирование', 'Данные']);
  });

  it('по умолчанию грузит непроверенные заявки первого направления', async () => {
    render(<OrganizerPage />);

    expect(await screen.findByRole('heading', { name: 'Spring Boot' })).toBeInTheDocument();
    expect(api.list).toHaveBeenCalledWith(10, '', 'PENDING');
  });

  it('смена фильтров перезапрашивает список с новыми параметрами', async () => {
    render(<OrganizerPage />);
    const user = userEvent.setup();
    await screen.findByRole('heading', { name: 'Spring Boot' });

    await user.selectOptions(screen.getByLabelText('Проверка'), 'APPROVED');
    await user.selectOptions(screen.getByLabelText('Статус заявки'), 'EDITED');
    await user.selectOptions(screen.getByLabelText('Направление'), '30');

    expect(api.list).toHaveBeenLastCalledWith(30, 'EDITED', 'APPROVED');
  });

  it('пустой список показывает подсказку', async () => {
    api.list.mockResolvedValue([]);

    render(<OrganizerPage />);

    expect(await screen.findByText('Заявок с такими параметрами нет')).toBeInTheDocument();
  });

  it('«Подтвердить» вызывает approve и показывает сообщение', async () => {
    render(<OrganizerPage />);

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Подтвердить' }));

    expect(api.approve).toHaveBeenCalledWith(5);
    expect(await screen.findByText('Заявка прошла проверку')).toBeInTheDocument();
  });

  it('«Отклонить» вызывает reject и показывает сообщение', async () => {
    render(<OrganizerPage />);

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Отклонить' }));

    expect(api.reject).toHaveBeenCalledWith(5);
    expect(await screen.findByText('Заявка не прошла проверку')).toBeInTheDocument();
  });

  it('ошибка сервера при проверке показывается пользователю', async () => {
    api.approve.mockRejectedValue(new Error('Application already reviewed'));
    render(<OrganizerPage />);

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Подтвердить' }));

    expect(await screen.findByText('Application already reviewed')).toBeInTheDocument();
  });

  it('у проверенной заявки нет кнопок проверки', async () => {
    api.list.mockResolvedValue([makeApplication({ reviewStatus: 'APPROVED' })]);

    render(<OrganizerPage />);

    await screen.findByRole('heading', { name: 'Spring Boot' });
    expect(screen.queryByRole('button', { name: 'Подтвердить' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Отклонить' })).not.toBeInTheDocument();
  });

  it('у отозванной заявки нет кнопок проверки', async () => {
    api.list.mockResolvedValue([makeApplication({ status: 'WITHDRAWN' })]);

    render(<OrganizerPage />);

    await screen.findByRole('heading', { name: 'Spring Boot' });
    expect(screen.queryByRole('button', { name: 'Подтвердить' })).not.toBeInTheDocument();
  });

  it('передача заявки на другое направление', async () => {
    render(<OrganizerPage />);
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: 'Передать на другое направление' }));
    await user.selectOptions(screen.getByLabelText('Передать на направление'), '20');
    await user.click(screen.getByRole('button', { name: 'Передать' }));

    expect(api.transfer).toHaveBeenCalledWith(5, 20);
    expect(await screen.findByText(/Заявка передана на направление «Дизайн»/)).toBeInTheDocument();
  });

  it('в списке для передачи нет текущего направления заявки', async () => {
    render(<OrganizerPage />);
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: 'Передать на другое направление' }));

    const target = screen.getByLabelText('Передать на направление');
    const values = within(target)
      .getAllByRole('option')
      .map((option) => option.getAttribute('value'));
    expect(values).not.toContain('10');
    expect(values).toContain('20');
  });

  describe('входящая передача с другого направления', () => {
    const incoming = makeApplication({
      directionId: 20,
      directionName: 'Дизайн',
      transferredFromDirectionId: 20,
      transferredFromDirectionName: 'Дизайн',
      transferToDirectionId: 10,
      transferToDirectionName: 'Программирование',
    });

    beforeEach(() => {
      api.list.mockResolvedValue([incoming]);
    });

    it('показывает кнопки «принять» и «отвергнуть» вместо обычных', async () => {
      render(<OrganizerPage />);

      expect(await screen.findByRole('button', { name: 'Принять на данное направление' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Отвергнуть' })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Подтвердить' })).not.toBeInTheDocument();
    });

    it('принять передачу', async () => {
      render(<OrganizerPage />);

      await userEvent.setup().click(await screen.findByRole('button', { name: 'Принять на данное направление' }));

      expect(api.acceptTransfer).toHaveBeenCalledWith(5);
      expect(await screen.findByText('Заявка принята на данное направление')).toBeInTheDocument();
    });

    it('отвергнуть передачу', async () => {
      render(<OrganizerPage />);

      await userEvent.setup().click(await screen.findByRole('button', { name: 'Отвергнуть' }));

      expect(api.rejectTransfer).toHaveBeenCalledWith(5);
      expect(
        await screen.findByText('Передача заявки отвергнута, заявка осталась на прежнем направлении'),
      ).toBeInTheDocument();
    });

    it('отозванную заявку можно только отвергнуть', async () => {
      api.list.mockResolvedValue([{ ...incoming, status: 'WITHDRAWN' }]);

      render(<OrganizerPage />);

      expect(await screen.findByRole('button', { name: 'Отвергнуть' })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Принять на данное направление' })).not.toBeInTheDocument();
    });
  });

  it('исходящая передача (ждёт чужого решения): кнопок действий нет', async () => {
    api.list.mockResolvedValue([
      makeApplication({ transferToDirectionId: 20, transferToDirectionName: 'Дизайн' }),
    ]);

    render(<OrganizerPage />);

    expect(await screen.findByText('Передача ожидает решения')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Подтвердить' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Принять на данное направление' })).not.toBeInTheDocument();
  });

  it('ошибка загрузки справочников показывается вместо страницы', async () => {
    vi.mocked(statusesApi.list).mockRejectedValue(new Error('Бэкенд недоступен'));

    render(<OrganizerPage />);

    expect(await screen.findByText('Бэкенд недоступен')).toBeInTheDocument();
  });
});
