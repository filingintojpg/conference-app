import type { ReactNode } from 'react';
import type { CardApplication } from '../../types';
import ApplicationCard from '../ApplicationCard/ApplicationCard';
import './ApplicationList.css';

interface ApplicationListProps<T extends CardApplication> {
  applications: T[];
  showParticipant?: boolean;
  renderActions?: (application: T) => ReactNode;
  emptyText?: string;
}

/**
 * Список заявок.
 * renderActions(application) - необязательная функция, возвращающая кнопки для карточки.
 */
export default function ApplicationList<T extends CardApplication>({
  applications,
  showParticipant = false,
  renderActions,
  emptyText = 'Заявок нет',
}: ApplicationListProps<T>) {
  if (applications.length === 0) {
    return <p className="application-list__empty">{emptyText}</p>;
  }

  return (
    <div className="application-list">
      {applications.map((application) => (
        <ApplicationCard
          key={application.id}
          application={application}
          showParticipant={showParticipant}
          actions={renderActions ? renderActions(application) : null}
        />
      ))}
    </div>
  );
}
