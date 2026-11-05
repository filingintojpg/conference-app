import './Select.css';

export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

interface SelectProps {
  label?: string;
  value: string;
  onChange: (value: string) => void;
  options: SelectOption[];
  placeholder?: string;
  disabled?: boolean;
  required?: boolean;
  error?: string;
}

/**
 * Выпадающий список.
 * options: [{ value, label, disabled? }]; onChange получает выбранное значение (строку).
 */
export default function Select({
  label,
  value,
  onChange,
  options,
  placeholder,
  disabled = false,
  required = false,
  error,
}: SelectProps) {
  return (
    <label className="select">
      {label && <span className="select__label">{label}</span>}
      <select
        className="select__control"
        value={value}
        required={required}
        disabled={disabled}
        onChange={(event) => onChange(event.target.value)}
      >
        {placeholder !== undefined && (
          <option value="" disabled={required}>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value} disabled={option.disabled}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <span className="select__error">{error}</span>}
    </label>
  );
}
