import { useMemo, useState } from 'react';
import {
  directionsApi,
  organizerApplicationsApi,
  reviewStatusesApi,
  statusesApi,
} from '../../api';
import ApplicationList from '../../components/ApplicationList/ApplicationList';
import Notice from '../../components/Notice/Notice';
import Select from '../../components/Select/Select';
import {
  APPLICATION_STATUS,
  REVIEW_STATUS,
  REVIEW_STATUS_LABELS,
  STATUS_LABELS,
  labelOf,
} from '../../config';
import { useCurrentUser } from '../../context/CurrentUserContext';
import { useAsync } from '../../hooks/useAsync';
import './OrganizerPage.css';

/** Экран организатора: заявки своих направлений, фильтры и проверка заявок. */
export default function OrganizerPage() {
  const { currentUser } = useCurrentUser();
  const directions = useAsync(() => directionsApi.list(), []);
  const statuses = useAsync(() => statusesApi.list(), []);
  const reviewStatuses = useAsync(() => reviewStatusesApi.list(), []);

  const [directionId, setDirectionId] = useState('');
  // Пустая строка - "любой".
  const [status, setStatus] = useState('');
  const [reviewStatus, setReviewStatus] = useState(REVIEW_STATUS.PENDING);
  const [message, setMessage] = useState(null);
  const [reviewingId, setReviewingId] = useState(null);

  // Организатор видит только свои направления.
  const myDirections = useMemo(
    () => (directions.data ?? []).filter((d) => d.organizerId === currentUser.id),
    [directions.data, currentUser.id],
  );

  // Пока пользователь ничего не выбрал - берём первое направление из списка.
  const selectedDirectionId =
    directionId && myDirections.some((d) => String(d.id) === directionId)
      ? directionId
      : myDirections[0]
        ? String(myDirections[0].id)
        : '';

  const applications = useAsync(
    () =>
      organizerApplicationsApi.list(
        currentUser.id,
        Number(selectedDirectionId),
        status,
        reviewStatus,
      ),
    [currentUser.id, selectedDirectionId, status, reviewStatus],
    { enabled: Boolean(selectedDirectionId) },
  );

  async function review(application, action, successText) {
    setReviewingId(application.id);
    setMessage(null);
    try {
      await organizerApplicationsApi[action](currentUser.id, application.id);
      setMessage({ type: 'success', text: successText });
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    } finally {
      setReviewingId(null);
      applications.reload();
    }
  }

  const canReview = (application) =>
    application.status !== APPLICATION_STATUS.WITHDRAWN &&
    application.reviewStatus === REVIEW_STATUS.PENDING;

  const loadError = directions.error || statuses.error || reviewStatuses.error;
  if (loadError) return <Notice type="error">{loadError.message}</Notice>;
  if (!directions.data || !statuses.data || !reviewStatuses.data) return <p>Загрузка...</p>;
  if (myDirections.length === 0) {
    return <Notice type="info">У этого организатора нет направлений</Notice>;
  }

  return (
    <div className="organizer-page">
      <h2>Заявки по направлению</h2>

      <div className="organizer-page__filters">
        <Select
          label="Направление"
          value={selectedDirectionId}
          onChange={setDirectionId}
          options={myDirections.map((d) => ({ value: String(d.id), label: d.name }))}
        />
        <Select
          label="Проверка"
          value={reviewStatus}
          onChange={setReviewStatus}
          placeholder="Все"
          options={reviewStatuses.data.map((s) => ({
            value: s,
            label: labelOf(REVIEW_STATUS_LABELS, s),
          }))}
        />
        <Select
          label="Статус заявки"
          value={status}
          onChange={setStatus}
          placeholder="Все"
          options={statuses.data.map((s) => ({ value: s, label: labelOf(STATUS_LABELS, s) }))}
        />
      </div>

      {message && <Notice type={message.type}>{message.text}</Notice>}
      {applications.error && <Notice type="error">{applications.error.message}</Notice>}
      {applications.loading && <p>Загрузка...</p>}
      {!applications.loading && applications.data && (
        <ApplicationList
          applications={applications.data}
          showParticipant
          emptyText="Заявок с такими параметрами нет"
          renderActions={(application) =>
            canReview(application) && (
              <>
                <button
                  type="button"
                  className="primary"
                  disabled={reviewingId === application.id}
                  onClick={() => review(application, 'approve', 'Заявка прошла проверку')}
                >
                  Подтвердить
                </button>
                <button
                  type="button"
                  className="danger"
                  disabled={reviewingId === application.id}
                  onClick={() => review(application, 'reject', 'Заявка не прошла проверку')}
                >
                  Отклонить
                </button>
              </>
            )
          }
        />
      )}
    </div>
  );
}
