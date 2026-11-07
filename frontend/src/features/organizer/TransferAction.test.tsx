import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { makeDirection } from '../../test/fixtures';
import TransferAction from './TransferAction';

const directions = [
  makeDirection({ id: 20, name: 'Дизайн', organizerFullName: 'Петров Пётр' }),
  makeDirection({ id: 30, name: 'Данные', organizerFullName: 'Орлова Ольга' }),
];

const OPEN_BUTTON = 'Передать на другое направление';

describe('TransferAction', () => {
  it('сначала показывает только кнопку', () => {
    render(<TransferAction directions={directions} onTransfer={vi.fn()} />);

    expect(screen.getByRole('button', { name: OPEN_BUTTON })).toBeEnabled();
    expect(screen.queryByLabelText('Передать на направление')).not.toBeInTheDocument();
  });

  it('кнопка отключается, если disabled', () => {
    render(<TransferAction directions={directions} disabled onTransfer={vi.fn()} />);

    expect(screen.getByRole('button', { name: OPEN_BUTTON })).toBeDisabled();
  });

  it('по клику показывает выбор направления с именем организатора', async () => {
    render(<TransferAction directions={directions} onTransfer={vi.fn()} />);

    await userEvent.setup().click(screen.getByRole('button', { name: OPEN_BUTTON }));

    expect(screen.getByRole('option', { name: 'Дизайн (Петров Пётр)' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Данные (Орлова Ольга)' })).toBeInTheDocument();
  });

  it('«Передать» неактивна, пока направление не выбрано', async () => {
    render(<TransferAction directions={directions} onTransfer={vi.fn()} />);

    await userEvent.setup().click(screen.getByRole('button', { name: OPEN_BUTTON }));

    expect(screen.getByRole('button', { name: 'Передать' })).toBeDisabled();
  });

  it('после выбора вызывает onTransfer и снова сворачивается в кнопку', async () => {
    const onTransfer = vi.fn().mockResolvedValue(undefined);
    render(<TransferAction directions={directions} onTransfer={onTransfer} />);
    const user = userEvent.setup();

    await user.click(screen.getByRole('button', { name: OPEN_BUTTON }));
    await user.selectOptions(screen.getByLabelText('Передать на направление'), '20');
    await user.click(screen.getByRole('button', { name: 'Передать' }));

    expect(onTransfer).toHaveBeenCalledWith('20');
    expect(await screen.findByRole('button', { name: OPEN_BUTTON })).toBeInTheDocument();
  });

  it('«Отмена» сворачивает выбор без вызова onTransfer', async () => {
    const onTransfer = vi.fn();
    render(<TransferAction directions={directions} onTransfer={onTransfer} />);
    const user = userEvent.setup();

    await user.click(screen.getByRole('button', { name: OPEN_BUTTON }));
    await user.click(screen.getByRole('button', { name: 'Отмена' }));

    expect(onTransfer).not.toHaveBeenCalled();
    expect(screen.getByRole('button', { name: OPEN_BUTTON })).toBeInTheDocument();
  });
});
