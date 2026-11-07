import { ROLE_LABELS, labelOf } from '../../config';
import { useAuth } from '../../context/AuthContext';
import './Header.css';

export default function Header() {
  const { user, logout } = useAuth();

  return (
    <header className="header">
      <h1 className="header__title">Conference App</h1>
      {user && (
        <div className="header__user">
          <span>
            {user.fullName} ({labelOf(ROLE_LABELS, user.role)})
          </span>
          <button type="button" onClick={() => void logout()}>
            Выйти
          </button>
        </div>
      )}
    </header>
  );
}
