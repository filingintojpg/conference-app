import Notice from './components/Notice/Notice';
import UserSwitcher from './components/UserSwitcher/UserSwitcher';
import { CurrentUserProvider, useCurrentUser } from './context/CurrentUserContext';
import { roleViews } from './features/roleViews';
import './App.css';

function Content() {
  const { currentUser, loading, error, reload } = useCurrentUser();

  if (loading) return <p>Загрузка...</p>;
  if (error) {
    return (
      <>
        <Notice type="error">{error.message}</Notice>
        <p>
          <button type="button" onClick={reload}>
            Повторить
          </button>
        </p>
      </>
    );
  }
  if (!currentUser) return <Notice type="info">На бэкенде нет пользователей</Notice>;

  const View = roleViews[currentUser.role];
  if (!View) return <Notice type="error">Нет экрана для роли {currentUser.role}</Notice>;

  return <View key={currentUser.id} />;
}

export default function App() {
  return (
    <CurrentUserProvider>
      <div className="app">
        <header className="app__header">
          <h1 className="app__title">Conference App</h1>
          <UserSwitcher />
        </header>
        <main className="app__main">
          <Content />
        </main>
      </div>
    </CurrentUserProvider>
  );
}
