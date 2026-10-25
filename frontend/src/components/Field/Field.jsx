import './Field.css';

/** Текстовое поле. multiline - многострочное (textarea). onChange получает строку. */
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
}) {
  const props = {
    className: 'field__control',
    value,
    maxLength,
    required,
    disabled,
    onChange: (event) => onChange(event.target.value),
  };

  return (
    <label className="field">
      {label && <span className="field__label">{label}</span>}
      {multiline ? <textarea rows={rows} {...props} /> : <input type="text" {...props} />}
      {error && <span className="field__error">{error}</span>}
    </label>
  );
}
