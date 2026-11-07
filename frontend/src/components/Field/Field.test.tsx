import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import Field from './Field';

describe('Field', () => {
  it('показывает подпись и значение', () => {
    render(<Field label="Имя" value="Иван" onChange={() => {}} />);

    expect(screen.getByLabelText('Имя')).toHaveValue('Иван');
  });

  it('передаёт в onChange строку, а не событие', async () => {
    const onChange = vi.fn();
    render(<Field label="Имя" value="" onChange={onChange} />);

    await userEvent.setup().type(screen.getByLabelText('Имя'), 'a');

    expect(onChange).toHaveBeenCalledWith('a');
  });

  it('показывает текст ошибки', () => {
    render(<Field label="Email" value="" onChange={() => {}} error="email must be a valid address" />);

    expect(screen.getByText('email must be a valid address')).toBeInTheDocument();
  });

  it('multiline рисует textarea, по умолчанию - input', () => {
    const { rerender } = render(<Field label="Текст" value="" onChange={() => {}} />);
    expect(screen.getByLabelText('Текст').tagName).toBe('INPUT');

    rerender(<Field label="Текст" value="" onChange={() => {}} multiline />);
    expect(screen.getByLabelText('Текст').tagName).toBe('TEXTAREA');
  });

  it('поле пароля скрывает ввод', () => {
    render(<Field label="Пароль" type="password" value="" onChange={() => {}} />);

    expect(screen.getByLabelText('Пароль')).toHaveAttribute('type', 'password');
  });
});
