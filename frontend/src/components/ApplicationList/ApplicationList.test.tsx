import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { makeApplication } from '../../test/fixtures';
import ApplicationList from './ApplicationList';

describe('ApplicationList', () => {
  it('для пустого списка показывает текст по умолчанию', () => {
    render(<ApplicationList applications={[]} />);

    expect(screen.getByText('Заявок нет')).toBeInTheDocument();
  });

  it('для пустого списка показывает свой текст', () => {
    render(<ApplicationList applications={[]} emptyText="У вас пока нет заявок" />);

    expect(screen.getByText('У вас пока нет заявок')).toBeInTheDocument();
  });

  it('рисует карточку на каждую заявку', () => {
    render(
      <ApplicationList
        applications={[makeApplication({ id: 1, title: 'Первая' }), makeApplication({ id: 2, title: 'Вторая' })]}
      />,
    );

    expect(screen.getAllByRole('article')).toHaveLength(2);
    expect(screen.getByRole('heading', { name: 'Первая' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Вторая' })).toBeInTheDocument();
  });

  it('вызывает renderActions для каждой заявки', () => {
    render(
      <ApplicationList
        applications={[makeApplication({ id: 1 }), makeApplication({ id: 2 })]}
        renderActions={(application) => <button type="button">Действие {application.id}</button>}
      />,
    );

    expect(screen.getByRole('button', { name: 'Действие 1' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Действие 2' })).toBeInTheDocument();
  });

  it('showParticipant показывает автора заявки', () => {
    render(<ApplicationList applications={[makeApplication()]} showParticipant />);

    expect(screen.getByText(/Участник: Иванов Иван/)).toBeInTheDocument();
  });
});
