import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../../api';
import Field from '../../components/Field/Field';
import Notice from '../../components/Notice/Notice';
import { useAuth } from '../../context/AuthContext';
import { errorMessage } from '../../utils/format';
import './AuthPage.css';

/** Вход по email и паролю. После успешного входа App сам перенаправит на главную. */
export default function LoginPage() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});
    try {
      await login({ email: email.trim(), password });
    } catch (err) {
      setError(errorMessage(err));
      setFieldErrors(err instanceof ApiError ? err.fields : {});
      setSubmitting(false);
    }
  }

  return (
    <form className="auth-page" onSubmit={handleSubmit}>
      <h2>Вход</h2>
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
        autoComplete="current-password"
        value={password}
        onChange={setPassword}
        required
        error={fieldErrors.password}
      />
      {error && <Notice type="error">{error}</Notice>}
      <button type="submit" className="primary" disabled={submitting}>
        Войти
      </button>
      <p className="auth-page__switch">
        Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
      </p>
    </form>
  );
}
