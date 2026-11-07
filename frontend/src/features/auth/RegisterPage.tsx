import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../../api';
import Field from '../../components/Field/Field';
import Notice from '../../components/Notice/Notice';
import { useAuth } from '../../context/AuthContext';
import { errorMessage } from '../../utils/format';
import './AuthPage.css';

const MIN_PASSWORD_LENGTH = 8;

export default function RegisterPage() {
  const { register } = useAuth();
  const [lastName, setLastName] = useState('');
  const [firstName, setFirstName] = useState('');
  const [middleName, setMiddleName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordRepeat, setPasswordRepeat] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setFieldErrors({});

    if (password.length < MIN_PASSWORD_LENGTH) {
      setFieldErrors({ password: `Пароль должен быть не короче ${MIN_PASSWORD_LENGTH} символов` });
      return;
    }
    if (password !== passwordRepeat) {
      setFieldErrors({ passwordRepeat: 'Пароли не совпадают' });
      return;
    }

    setSubmitting(true);
    try {
      await register({
        lastName: lastName.trim(),
        firstName: firstName.trim(),
        middleName: middleName.trim() || undefined,
        email: email.trim(),
        password,
      });
    } catch (err) {
      setError(errorMessage(err));
      setFieldErrors(err instanceof ApiError ? err.fields : {});
      setSubmitting(false);
    }
  }

  return (
    <form className="auth-page" onSubmit={handleSubmit}>
      <h2>Регистрация</h2>
      <Field
        label="Фамилия"
        value={lastName}
        onChange={setLastName}
        maxLength={100}
        required
        error={fieldErrors.lastName}
      />
      <Field
        label="Имя"
        value={firstName}
        onChange={setFirstName}
        maxLength={100}
        required
        error={fieldErrors.firstName}
      />
      <Field
        label="Отчество (необязательно)"
        value={middleName}
        onChange={setMiddleName}
        maxLength={100}
        error={fieldErrors.middleName}
      />
      <Field
        label="Email"
        type="email"
        autoComplete="username"
        value={email}
        onChange={setEmail}
        required
        error={fieldErrors.email}
      />
      <Field
        label="Пароль"
        type="password"
        autoComplete="new-password"
        value={password}
        onChange={setPassword}
        maxLength={72}
        required
        error={fieldErrors.password}
      />
      <Field
        label="Повторите пароль"
        type="password"
        autoComplete="new-password"
        value={passwordRepeat}
        onChange={setPasswordRepeat}
        maxLength={72}
        required
        error={fieldErrors.passwordRepeat}
      />
      {error && <Notice type="error">{error}</Notice>}
      <button type="submit" className="primary" disabled={submitting}>
        Зарегистрироваться
      </button>
      <p className="auth-page__switch">
        Уже есть аккаунт? <Link to="/login">Войти</Link>
      </p>
    </form>
  );
}
