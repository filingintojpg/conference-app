import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { usersApi } from '../api';
import { STORAGE_KEYS } from '../config';
import { useAsync } from '../hooks/useAsync';

const CurrentUserContext = createContext(null);

function readStoredId() {
  try {
    return localStorage.getItem(STORAGE_KEYS.currentUserId);
  } catch {
    return null;
  }
}

/**
 * Хранит список пользователей (с бэкенда) и того, от чьего лица сейчас работаем.
 * Выбор запоминается в localStorage.
 */
export function CurrentUserProvider({ children }) {
  const { data: users, loading, error, reload } = useAsync(() => usersApi.list(), []);
  const [selectedId, setSelectedId] = useState(readStoredId);

  const currentUser = useMemo(() => {
    if (!users || users.length === 0) return null;
    return users.find((user) => String(user.id) === String(selectedId)) ?? users[0];
  }, [users, selectedId]);

  const setCurrentUserId = useCallback((id) => {
    setSelectedId(String(id));
    try {
      localStorage.setItem(STORAGE_KEYS.currentUserId, String(id));
    } catch {
      // localStorage недоступен - выбор просто не запомнится
    }
  }, []);

  const value = useMemo(
    () => ({ users: users ?? [], currentUser, setCurrentUserId, loading, error, reload }),
    [users, currentUser, setCurrentUserId, loading, error, reload],
  );

  return <CurrentUserContext.Provider value={value}>{children}</CurrentUserContext.Provider>;
}

export function useCurrentUser() {
  const context = useContext(CurrentUserContext);
  if (!context) throw new Error('useCurrentUser must be used inside CurrentUserProvider');
  return context;
}
