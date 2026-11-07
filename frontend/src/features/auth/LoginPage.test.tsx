import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../api';
import LoginPage from './LoginPage';

const loginMock = vi.hoisted(() => vi.fn());

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ login: loginMock }),
}));

function renderPage() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    loginMock.mockReset();
  });

  it('отправляет email и пароль', async () => {
    loginMock.mockResolvedValue(undefined);
    renderPage();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText('Email'), 'ivan@example.com');
    await user.type(screen.getByLabelText('Пароль'), 'secret-pass');
    await user.click(screen.getByRole('button', { name: 'Войти' }));

    expect(loginMock).toHaveBeenCalledWith({ email: 'ivan@example.com', password: 'secret-pass' });
  });

  it('показывает ошибку сервера и снова разрешает отправку', async () => {
    loginMock.mockRejectedValue(new ApiError(401, 'Invalid email or password'));
    renderPage();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText('Email'), 'ivan@example.com');
    await user.type(screen.getByLabelText('Пароль'), 'wrong-pass');
    await user.click(screen.getByRole('button', { name: 'Войти' }));

    expect(await screen.findByText('Invalid email or password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Войти' })).toBeEnabled();
  });

  it('есть ссылка на регистрацию', () => {
    renderPage();

    expect(screen.getByRole('link', { name: 'Зарегистрироваться' })).toHaveAttribute('href', '/register');
  });
});
