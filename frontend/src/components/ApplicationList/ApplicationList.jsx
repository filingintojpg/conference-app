import ApplicationCard from '../ApplicationCard/ApplicationCard';
import './ApplicationList.css';

/**
 * Список заявок.
 * renderActions(application) - необязательная функция, возвращающая кнопки для карточки.
 */
export default function ApplicationList({
  applications,
  showParticipant = false,
  renderActions,
  emptyText = 'Заявок нет',
}) {
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
