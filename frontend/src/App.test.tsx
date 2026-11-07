import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { User } from './types';
import App from './App';

interface AuthState {
  user: User | null;
  initializing: boolean;
}

const auth = vi.hoisted(() => ({ state: { user: null, initializing: false } as AuthState }));

vi.mock('./context/AuthContext', () => ({
  AuthProvider: ({ children }: { children: unknown }) => children,
  useAuth: () => ({
    ...auth.state,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
  }),
}));

// Настоящие экраны ролей здесь не нужны: проверяем только, какой из них выбирает App.
vi.mock('./features/roleViews', () => ({
  roleViews: {
    PARTICIPANT: () => 'Экран участника',
    ORGANIZER: () => 'Экран организатора',
    EXPERT: () => 'Экран эксперта',
  },
}));

function openAt(path: string) {
  window.history.pushState({}, '', path);
  return render(<App />);
}

const organizer: User = { id: 2, fullName: 'Сидорова Мария', email: 'maria@example.com', role: 'ORGANIZER' };

describe('App: маршрутизация', () => {
  beforeEach(() => {
    auth.state = { user: null, initializing: false };
  });

  it('пока проверяется сессия, показывает загрузку', () => {
    auth.state = { user: null, initializing: true };

    openAt('/');

    expect(screen.getByText('Загрузка...')).toBeInTheDocument();
  });

  it('гостя с главной отправляет на вход', () => {
    openAt('/');

    expect(screen.getByRole('heading', { name: 'Вход' })).toBeInTheDocument();
  });

  it('гость может открыть регистрацию', () => {
    openAt('/register');

    expect(screen.getByRole('heading', { name: 'Регистрация' })).toBeInTheDocument();
  });

  it('участнику показывается экран участника', () => {
    auth.state = { user: { id: 1, fullName: 'Иванов Иван', email: 'ivan@example.com', role: 'PARTICIPANT' }, initializing: false };

    openAt('/');

    expect(screen.getByText('Экран участника')).toBeInTheDocument();
  });

  it('организатору показывается экран организатора', () => {
    auth.state = { user: organizer, initializing: false };

    openAt('/');

    expect(screen.getByText('Экран организатора')).toBeInTheDocument();
  });

  it('эксперту показывается экран эксперта', () => {
    auth.state = { user: { ...organizer, role: 'EXPERT' }, initializing: false };

    openAt('/');

    expect(screen.getByText('Экран эксперта')).toBeInTheDocument();
  });

  it('вошедшего со страницы входа отправляет на главную', () => {
    auth.state = { user: organizer, initializing: false };

    openAt('/login');

    expect(screen.getByText('Экран организатора')).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Вход' })).not.toBeInTheDocument();
  });

  it('неизвестный адрес ведёт на главную', () => {
    auth.state = { user: organizer, initializing: false };

    openAt('/no-such-page');

    expect(screen.getByText('Экран организатора')).toBeInTheDocument();
  });

  it('шапка с названием приложения есть на любой странице', () => {
    openAt('/login');

    expect(screen.getByRole('heading', { name: 'Conference App' })).toBeInTheDocument();
  });
});
