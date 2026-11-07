import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { authApi, setUnauthorizedHandler } from '../api';
import type { User } from '../types';
import { AuthProvider, useAuth } from './AuthContext';

vi.mock('../api', () => ({
  authApi: { me: vi.fn(), login: vi.fn(), register: vi.fn(), logout: vi.fn() },
  setUnauthorizedHandler: vi.fn(),
}));

const ivan: User = { id: 1, fullName: 'Иванов Иван', email: 'ivan@example.com', role: 'PARTICIPANT' };

function Probe() {
  const { user, initializing, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="state">{initializing ? 'loading' : (user?.fullName ?? 'guest')}</span>
      <button type="button" onClick={() => void login({ email: 'ivan@example.com', password: 'secret-pass' })}>
        login
      </button>
      <button type="button" onClick={() => void logout()}>
        logout
      </button>
    </div>
  );
}

function renderProvider() {
  return render(
    <AuthProvider>
      <Probe />
    </AuthProvider>,
  );
}

describe('AuthProvider', () => {
  beforeEach(() => {
    vi.mocked(authApi.me).mockReset();
    vi.mocked(authApi.login).mockReset();
    vi.mocked(authApi.logout).mockReset();
    vi.mocked(setUnauthorizedHandler).mockReset();
  });

  it('при запуске восстанавливает сессию по cookie (GET /auth/me)', async () => {
    vi.mocked(authApi.me).mockResolvedValue(ivan);

    renderProvider();

    expect(screen.getByTestId('state')).toHaveTextContent('loading');
    expect(await screen.findByText('Иванов Иван')).toBeInTheDocument();
  });

  it('без сессии (me вернул ошибку) показывает гостя', async () => {
    vi.mocked(authApi.me).mockRejectedValue(new Error('Authentication required'));

    renderProvider();

    expect(await screen.findByText('guest')).toBeInTheDocument();
  });

  it('login сохраняет пользователя из ответа сервера', async () => {
    vi.mocked(authApi.me).mockRejectedValue(new Error('401'));
    vi.mocked(authApi.login).mockResolvedValue(ivan);
    renderProvider();
    await screen.findByText('guest');

    await userEvent.setup().click(screen.getByText('login'));

    expect(await screen.findByText('Иванов Иван')).toBeInTheDocument();
    expect(authApi.login).toHaveBeenCalledWith({ email: 'ivan@example.com', password: 'secret-pass' });
  });

  it('logout просит сервер удалить cookie и сбрасывает пользователя', async () => {
    vi.mocked(authApi.me).mockResolvedValue(ivan);
    vi.mocked(authApi.logout).mockResolvedValue(null as never);
    renderProvider();
    await screen.findByText('Иванов Иван');

    await userEvent.setup().click(screen.getByText('logout'));

    expect(await screen.findByText('guest')).toBeInTheDocument();
    expect(authApi.logout).toHaveBeenCalledTimes(1);
  });

  it('401 от сервера (cookie истекла) разлогинивает пользователя', async () => {
    vi.mocked(authApi.me).mockResolvedValue(ivan);
    renderProvider();
    await screen.findByText('Иванов Иван');

    const handler = vi
      .mocked(setUnauthorizedHandler)
      .mock.calls.map(([fn]) => fn)
      .find((fn) => typeof fn === 'function');
    act(() => handler?.());

    expect(await screen.findByText('guest')).toBeInTheDocument();
  });
});
