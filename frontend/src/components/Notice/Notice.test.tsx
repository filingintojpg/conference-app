import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import Notice from './Notice';

describe('Notice', () => {
  it('по умолчанию информационное', () => {
    render(<Notice>Привет</Notice>);

    expect(screen.getByText('Привет')).toHaveClass('notice', 'notice--info');
  });

  it.each(['error', 'success', 'info'] as const)('тип %s задаёт свой класс', (type) => {
    render(<Notice type={type}>Текст</Notice>);

    expect(screen.getByText('Текст')).toHaveClass(`notice--${type}`);
  });
});
