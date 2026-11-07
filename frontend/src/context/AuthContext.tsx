import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authApi, setUnauthorizedHandler } from '../api';
import type { LoginPayload, RegisterPayload, User } from '../types';

interface AuthContextValue {
  /** Вошедший пользователь или null. */
  user: User | null;
  /** true, пока при запуске выясняется, есть ли действующая сессия (cookie). */
  initializing: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  // Cookie httpOnly, из JS её не видно - поэтому при каждом запуске спрашиваем сервер, кто мы.
  const [initializing, setInitializing] = useState(true);

  // Сервер ответил 401 на защищённый запрос: cookie истекла или недействительна.
  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
    return () => setUnauthorizedHandler(null);
  }, []);

  // При запуске восстанавливаем сессию по cookie. Заодно этот GET выдаёт браузеру cookie XSRF-TOKEN.
  useEffect(() => {
    let cancelled = false;
    authApi
      .me()
      .then((me) => {
        if (!cancelled) setUser(me);
      })
      .catch(() => {
        // 401 - сессии нет, показываем вход. Сетевая ошибка - то же самое.
      })
      .finally(() => {
        if (!cancelled) setInitializing(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (payload: LoginPayload) => {
    setUser(await authApi.login(payload));
  }, []);

  const register = useCallback(async (payload: RegisterPayload) => {
    setUser(await authApi.register(payload));
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // Даже если сервер недоступен, локально считаем пользователя вышедшим.
    }
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ user, initializing, login, register, logout }),
    [user, initializing, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}

/** Для экранов, которые рендерятся только после входа. */
export function useRequiredUser(): User {
  const { user } = useAuth();
  if (!user) throw new Error('Пользователь не авторизован');
  return user;
}
