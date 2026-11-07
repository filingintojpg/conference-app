import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../api';
import { makeApplication, makeDirection } from '../../test/fixtures';
import ApplicationForm from './ApplicationForm';

const open = makeDirection({ id: 10, name: 'Программирование' });
const closed = makeDirection({ id: 11, name: 'Старое направление', deadline: '2000-01-01T00:00:00Z' });

async function fillAndSubmit(user: ReturnType<typeof userEvent.setup>) {
  await user.selectOptions(screen.getByLabelText('Направление'), '10');
  await user.type(screen.getByLabelText('Название'), 'Мой доклад');
  await user.type(screen.getByLabelText('Аннотация'), 'Коротко');
  await user.type(screen.getByLabelText('Содержание'), 'Подробно');
  await user.click(screen.getByRole('button', { name: 'Подать заявку' }));
}

describe('ApplicationForm: создание', () => {
  it('показывает форму новой заявки', () => {
    render(<ApplicationForm directions={[open]} initial={null} onSubmit={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByRole('heading', { name: 'Новая заявка' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Отмена' })).not.toBeInTheDocument();
  });

  it('направление с истёкшим сроком недоступно для выбора', () => {
    render(<ApplicationForm directions={[open, closed]} initial={null} onSubmit={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByRole('option', { name: /приём закрыт/ })).toBeDisabled();
    expect(screen.getByRole('option', { name: /Программирование/ })).toBeEnabled();
  });

  it('отправляет введённые значения', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<ApplicationForm directions={[open]} initial={null} onSubmit={onSubmit} onCancel={vi.fn()} />);

    await fillAndSubmit(userEvent.setup());

    expect(onSubmit).toHaveBeenCalledWith({
      directionId: '10',
      title: 'Мой доклад',
      abstractText: 'Коротко',
      content: 'Подробно',
    });
  });

  it('показывает ошибку сервера и ошибки полей', async () => {
    const onSubmit = vi
      .fn()
      .mockRejectedValue(new ApiError(400, 'Validation failed', { title: 'title must not be blank' }));
    render(<ApplicationForm directions={[open]} initial={null} onSubmit={onSubmit} onCancel={vi.fn()} />);

    await fillAndSubmit(userEvent.setup());

    expect(await screen.findByText('Validation failed')).toBeInTheDocument();
    expect(screen.getByText('title must not be blank')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Подать заявку' })).toBeEnabled();
  });
});

describe('ApplicationForm: редактирование', () => {
  const initial = makeApplication({ title: 'Старое название', abstractText: 'Старая аннотация' });

  it('подставляет значения заявки и блокирует смену направления', () => {
    render(<ApplicationForm directions={[open]} initial={initial} onSubmit={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByRole('heading', { name: 'Редактирование заявки' })).toBeInTheDocument();
    expect(screen.getByLabelText('Название')).toHaveValue('Старое название');
    expect(screen.getByLabelText('Аннотация')).toHaveValue('Старая аннотация');
    expect(screen.getByLabelText('Направление')).toBeDisabled();
  });

  it('кнопка «Отмена» вызывает onCancel', async () => {
    const onCancel = vi.fn();
    render(<ApplicationForm directions={[open]} initial={initial} onSubmit={vi.fn()} onCancel={onCancel} />);

    await userEvent.setup().click(screen.getByRole('button', { name: 'Отмена' }));

    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('«Сохранить» отправляет изменённые значения', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<ApplicationForm directions={[open]} initial={initial} onSubmit={onSubmit} onCancel={vi.fn()} />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText('Название'), ' (новое)');
    await user.click(screen.getByRole('button', { name: 'Сохранить' }));

    expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ title: 'Старое название (новое)' }));
  });
});
