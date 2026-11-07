import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RegisterPage from './RegisterPage';

const registerMock = vi.hoisted(() => vi.fn());

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ register: registerMock }),
}));

async function fillForm(password: string, repeat: string) {
  const user = userEvent.setup();
  await user.type(screen.getByLabelText('Фамилия'), 'Иванов');
  await user.type(screen.getByLabelText('Имя'), 'Иван');
  await user.type(screen.getByLabelText('Email'), 'ivan@example.com');
  await user.type(screen.getByLabelText('Пароль'), password);
  await user.type(screen.getByLabelText('Повторите пароль'), repeat);
  await user.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));
}

describe('RegisterPage', () => {
  beforeEach(() => {
    registerMock.mockReset();
    render(
      <MemoryRouter>
        <RegisterPage />
      </MemoryRouter>,
    );
  });

  it('не отправляет форму, если пароли не совпадают', async () => {
    await fillForm('secret-pass', 'other-pass');

    expect(await screen.findByText('Пароли не совпадают')).toBeInTheDocument();
    expect(registerMock).not.toHaveBeenCalled();
  });

  it('не отправляет форму с коротким паролем', async () => {
    await fillForm('short', 'short');

    expect(await screen.findByText('Пароль должен быть не короче 8 символов')).toBeInTheDocument();
    expect(registerMock).not.toHaveBeenCalled();
  });

  it('отправляет данные при корректной форме (отчество необязательно)', async () => {
    registerMock.mockResolvedValue(undefined);

    await fillForm('secret-pass', 'secret-pass');

    expect(registerMock).toHaveBeenCalledWith({
      lastName: 'Иванов',
      firstName: 'Иван',
      middleName: undefined,
      email: 'ivan@example.com',
      password: 'secret-pass',
    });
  });
});
