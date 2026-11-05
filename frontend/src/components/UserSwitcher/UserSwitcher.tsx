import Select from '../Select/Select';
import { ROLE_LABELS, labelOf } from '../../config';
import { useCurrentUser } from '../../context/CurrentUserContext';
import './UserSwitcher.css';

/** Выбор пользователя, от лица которого выполняются действия. */
export default function UserSwitcher() {
  const { users, currentUser, setCurrentUserId } = useCurrentUser();

  const options = users.map((user) => ({
    value: String(user.id),
    label: `${user.fullName} (${labelOf(ROLE_LABELS, user.role)})`,
  }));

  return (
    <div className="user-switcher">
      <Select
        label="Действовать от лица"
        value={currentUser ? String(currentUser.id) : ''}
        onChange={setCurrentUserId}
        options={options}
      />
    </div>
  );
}
