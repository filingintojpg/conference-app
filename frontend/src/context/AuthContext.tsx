import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import {
  authApi,
  clearAccessToken,
  getAccessToken,
  setAccessToken,
  setUnauthorizedHandler,
} from '../api';
import type { AuthResponse, LoginPayload, RegisterPayload, User } from '../types';

interface AuthContextValue {
  /** Вошедший пользователь или null. */
  user: User | null;
  /** true, пока при запуске проверяется сохранённый токен. */
  initializing: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [initializing, setInitializing] = useState<boolean>(() => getAccessToken() !== null);

  const logout = useCallback(() => {
    clearAccessToken();
    setUser(null);
  }, []);

  // Сервер ответил 401 на запрос с токеном: токен истёк или недействителен.
  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  // При запуске восстанавливаем сессию по сохранённому токену.
  useEffect(() => {
    if (getAccessToken() === null) return undefined;
    let cancelled = false;
    authApi
      .me()
      .then((me) => {
        if (!cancelled) setUser(me);
      })
      .catch(() => {
        // 401 уже сбросил токен через unauthorizedHandler; при сетевой ошибке просто покажем вход.
      })
      .finally(() => {
        if (!cancelled) setInitializing(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const accept = useCallback((response: AuthResponse) => {
    setAccessToken(response.accessToken);
    setUser(response.user);
  }, []);

  const login = useCallback(
    async (payload: LoginPayload) => accept(await authApi.login(payload)),
    [accept],
  );

  const register = useCallback(
    async (payload: RegisterPayload) => accept(await authApi.register(payload)),
    [accept],
  );

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
