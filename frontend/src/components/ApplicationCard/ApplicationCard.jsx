import { APPLICATION_STATUS, REVIEW_STATUS, REVIEW_STATUS_LABELS, STATUS_LABELS, labelOf } from '../../config';
import { formatDateTime } from '../../utils/format';
import './ApplicationCard.css';

/** Карточка заявки. actions - произвольные кнопки (зависят от роли). */
export default function ApplicationCard({ application, showParticipant = false, actions }) {
  // У отозванной заявки, которую так и не проверили, статус проверки не показываем.
  const showReview =
    Boolean(application.reviewStatus) &&
    (application.status !== APPLICATION_STATUS.WITHDRAWN ||
      application.reviewStatus !== REVIEW_STATUS.PENDING);

  const transferPending = Boolean(application.transferToDirectionId);

  return (
    <article className="application-card">
      <header className="application-card__header">
        <h3 className="application-card__title">{application.title}</h3>
        <div className="application-card__badges">
          {application.status && (
            <span
              className={`application-card__status application-card__status--${application.status}`}
            >
              {labelOf(STATUS_LABELS, application.status)}
            </span>
          )}
          {transferPending && (
            <span className="application-card__status application-card__transfer-badge">
              Передача ожидает решения
            </span>
          )}
          {showReview && (
            <span
              className={`application-card__status application-card__review--${application.reviewStatus}`}
            >
              {labelOf(REVIEW_STATUS_LABELS, application.reviewStatus)}
            </span>
          )}
        </div>
      </header>

      <div className="application-card__meta">
        <div>Направление: {application.directionName}</div>
        {transferPending && (
          <div className="application-card__transfer">
            Передана с направления «{application.transferredFromDirectionName}» на направление «
            {application.transferToDirectionName}», ожидает решения
          </div>
        )}
        {!transferPending && application.transferredFromDirectionName && (
          <div className="application-card__transfer">
            Передана с направления «{application.transferredFromDirectionName}»
          </div>
        )}
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
