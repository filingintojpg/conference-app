import type { ChangeEvent } from 'react';
import './Field.css';

interface FieldProps {
  label?: string;
  value: string;
  onChange: (value: string) => void;
  multiline?: boolean;
  rows?: number;
  maxLength?: number;
  required?: boolean;
  disabled?: boolean;
  error?: string;
  type?: 'text' | 'email' | 'password';
  autoComplete?: string;
}

export default function Field({
  label,
  value,
  onChange,
  multiline = false,
  rows = 4,
  maxLength,
  required = false,
  disabled = false,
  error,
  type = 'text',
  autoComplete,
}: FieldProps) {
  const props = {
    className: 'field__control',
    value,
    maxLength,
    required,
    disabled,
    onChange: (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
      onChange(event.target.value),
  };

  return (
    <label className="field">
      {label && <span className="field__label">{label}</span>}
      {multiline ? <textarea rows={rows} {...props} /> : <input type={type} autoComplete={autoComplete} {...props} />}
      {error && <span className="field__error">{error}</span>}
    </label>
  );
}
