import { STATUS_LABELS, labelOf } from '../../config';
import { formatDateTime } from '../../utils/format';
import './ApplicationCard.css';

/** Карточка заявки. actions - произвольные кнопки (зависят от роли). */
export default function ApplicationCard({ application, showParticipant = false, actions }) {
  return (
    <article className="application-card">
      <header className="application-card__header">
        <h3 className="application-card__title">{application.title}</h3>
        <span className={`application-card__status application-card__status--${application.status}`}>
          {labelOf(STATUS_LABELS, application.status)}
        </span>
      </header>

      <div className="application-card__meta">
        <div>Направление: {application.directionName}</div>
        {showParticipant && (
          <div>
            Участник: {application.participantFullName} ({application.participantEmail})
          </div>
        )}
        <div>
          Создана: {formatDateTime(application.createdAt)}, обновлена:{' '}
          {formatDateTime(application.updatedAt)}
        </div>
      </div>

      <p className="application-card__abstract">{application.abstractText}</p>

      <details className="application-card__content">
        <summary>Содержание</summary>
        <p>{application.content}</p>
      </details>

      {actions && <div className="application-card__actions">{actions}</div>}
    </article>
  );
}
