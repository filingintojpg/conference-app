import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { User } from '../../types';
import Header from './Header';

const auth = vi.hoisted(() => ({ user: null as User | null, logout: vi.fn() }));

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ user: auth.user, logout: auth.logout }),
}));

describe('Header', () => {
  beforeEach(() => {
    auth.user = null;
    auth.logout.mockReset();
  });

  it('всегда показывает название приложения', () => {
    render(<Header />);

    expect(screen.getByRole('heading', { name: 'Conference App' })).toBeInTheDocument();
  });

  it('гостю не показывает кнопку выхода', () => {
    render(<Header />);

    expect(screen.queryByRole('button', { name: 'Выйти' })).not.toBeInTheDocument();
  });

  it('вошедшему показывает имя, роль и кнопку выхода', () => {
    auth.user = { id: 2, fullName: 'Сидорова Мария', email: 'maria@example.com', role: 'ORGANIZER' };

    render(<Header />);

    expect(screen.getByText('Сидорова Мария (организатор)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Выйти' })).toBeInTheDocument();
  });

  it('кнопка «Выйти» вызывает logout', async () => {
    auth.user = { id: 1, fullName: 'Иванов Иван', email: 'ivan@example.com', role: 'PARTICIPANT' };
    render(<Header />);

    await userEvent.setup().click(screen.getByRole('button', { name: 'Выйти' }));

    expect(auth.logout).toHaveBeenCalledTimes(1);
  });
});
