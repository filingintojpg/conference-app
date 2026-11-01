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
import TransferAction from './TransferAction';
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

/**
 * Экран организатора: заявки своих направлений, фильтры, проверка заявок
 * и передача заявок между направлениями.
 */
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

  // Выполняет действие над заявкой, показывает результат и перезагружает список.
  async function run(application, call, successText) {
    setReviewingId(application.id);
    setMessage(null);
    try {
      await call();
      setMessage({ type: 'success', text: successText });
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    } finally {
      setReviewingId(null);
      applications.reload();
    }
  }

  const review = (application, action, successText) =>
    run(application, () => organizerApplicationsApi[action](currentUser.id, application.id), successText);

  const transfer = (application, targetDirectionId) => {
    const target = (directions.data ?? []).find((d) => String(d.id) === String(targetDirectionId));
    return run(
      application,
      () => organizerApplicationsApi.transfer(currentUser.id, application.id, Number(targetDirectionId)),
      `Заявка передана на направление «${target?.name ?? targetDirectionId}», ожидает решения организатора`,
    );
  };

  const hasPendingTransfer = (application) => Boolean(application.transferToDirectionId);

  // Заявка передана на выбранное направление и ждёт ответа его организатора.
  const isIncomingTransfer = (application) =>
    hasPendingTransfer(application) &&
    String(application.transferToDirectionId) === selectedDirectionId;

  const isWithdrawn = (application) => application.status === APPLICATION_STATUS.WITHDRAWN;

  // Проверить или передать можно только неотозванную заявку без результата проверки
  // и без ожидающей передачи.
  const canReview = (application) =>
    !isWithdrawn(application) &&
    application.reviewStatus === REVIEW_STATUS.PENDING &&
    !hasPendingTransfer(application);

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
          renderActions={(application) => {
            const busy = reviewingId === application.id;

            if (isIncomingTransfer(application)) {
              return (
                <>
                  {!isWithdrawn(application) && (
                    <button
                      type="button"
                      className="primary"
                      disabled={busy}
                      onClick={() =>
                        run(
                          application,
                          () => organizerApplicationsApi.acceptTransfer(currentUser.id, application.id),
                          'Заявка принята на данное направление',
                        )
                      }
                    >
                      Принять на данное направление
                    </button>
                  )}
                  <button
                    type="button"
                    className="danger"
                    disabled={busy}
                    onClick={() =>
                      run(
                        application,
                        () => organizerApplicationsApi.rejectTransfer(currentUser.id, application.id),
                        'Передача заявки отвергнута, заявка осталась на прежнем направлении',
                      )
                    }
                  >
                    Отвергнуть
                  </button>
                </>
              );
            }

            if (!canReview(application)) return null;

            return (
              <>
                <button
                  type="button"
                  className="primary"
                  disabled={busy}
                  onClick={() => review(application, 'approve', 'Заявка прошла проверку')}
                >
                  Подтвердить
                </button>
                <button
                  type="button"
                  className="danger"
                  disabled={busy}
                  onClick={() => review(application, 'reject', 'Заявка не прошла проверку')}
                >
                  Отклонить
                </button>
                <TransferAction
                  directions={directions.data.filter((d) => d.id !== application.directionId)}
                  disabled={busy}
                  onTransfer={(targetId) => transfer(application, targetId)}
                />
              </>
            );
          }}
        />
      )}
    </div>
  );
}
