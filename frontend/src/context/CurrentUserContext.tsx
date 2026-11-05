import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { usersApi } from '../api';
import { STORAGE_KEYS } from '../config';
import { useAsync } from '../hooks/useAsync';
import type { User } from '../types';

interface CurrentUserContextValue {
  users: User[];
  currentUser: User | null;
  setCurrentUserId: (id: number | string) => void;
  loading: boolean;
  error: Error | null;
  reload: () => void;
}

const CurrentUserContext = createContext<CurrentUserContextValue | null>(null);

function readStoredId(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEYS.currentUserId);
  } catch {
    return null;
  }
}

export function CurrentUserProvider({ children }: { children: ReactNode }) {
  const { data: users, loading, error, reload } = useAsync(() => usersApi.list(), []);
  const [selectedId, setSelectedId] = useState<string | null>(readStoredId);

  const currentUser = useMemo<User | null>(() => {
    if (!users || users.length === 0) return null;
    return users.find((user) => String(user.id) === String(selectedId)) ?? users[0];
  }, [users, selectedId]);

  const setCurrentUserId = useCallback((id: number | string) => {
    setSelectedId(String(id));
    try {
      localStorage.setItem(STORAGE_KEYS.currentUserId, String(id));
    } catch {
      // localStorage может быть недоступен - выбор просто не сохранится.
    }
  }, []);

  const value = useMemo<CurrentUserContextValue>(
    () => ({ users: users ?? [], currentUser, setCurrentUserId, loading, error, reload }),
    [users, currentUser, setCurrentUserId, loading, error, reload],
  );

  return <CurrentUserContext.Provider value={value}>{children}</CurrentUserContext.Provider>;
}

export function useCurrentUser(): CurrentUserContextValue {
  const context = useContext(CurrentUserContext);
  if (!context) throw new Error('useCurrentUser must be used inside CurrentUserProvider');
  return context;
}

/** Для экранов, которые рендерятся только при выбранном пользователе. */
export function useRequiredUser(): User {
  const { currentUser } = useCurrentUser();
  if (!currentUser) throw new Error('Текущий пользователь не выбран');
  return currentUser;
}
