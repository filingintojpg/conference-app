import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import Select from './Select';

const options = [
  { value: '1', label: 'Программирование' },
  { value: '2', label: 'Дизайн' },
  { value: '3', label: 'Закрыто', disabled: true },
];

describe('Select', () => {
  it('показывает варианты и placeholder', () => {
    render(<Select label="Направление" value="" onChange={() => {}} options={options} placeholder="Выберите" />);

    expect(screen.getByRole('option', { name: 'Выберите' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Программирование' })).toBeInTheDocument();
    expect(screen.getAllByRole('option')).toHaveLength(4);
  });

  it('передаёт в onChange выбранное значение', async () => {
    const onChange = vi.fn();
    render(<Select label="Направление" value="" onChange={onChange} options={options} placeholder="Выберите" />);

    await userEvent.setup().selectOptions(screen.getByLabelText('Направление'), '2');

    expect(onChange).toHaveBeenCalledWith('2');
  });

  it('недоступный вариант отключён', () => {
    render(<Select label="Направление" value="" onChange={() => {}} options={options} />);

    expect(screen.getByRole('option', { name: 'Закрыто' })).toBeDisabled();
  });

  it('показывает ошибку', () => {
    render(<Select value="" onChange={() => {}} options={options} error="Выберите направление" />);

    expect(screen.getByText('Выберите направление')).toBeInTheDocument();
  });
});
