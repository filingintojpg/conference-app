import { useMemo, useState } from 'react';
import { directionsApi, organizerApplicationsApi, statusesApi } from '../../api';
import ApplicationList from '../../components/ApplicationList/ApplicationList';
import Notice from '../../components/Notice/Notice';
import Select from '../../components/Select/Select';
import { STATUS_LABELS, labelOf } from '../../config';
import { useCurrentUser } from '../../context/CurrentUserContext';
import { useAsync } from '../../hooks/useAsync';
import './OrganizerPage.css';

/** Экран организатора: заявки своих направлений с фильтром по статусу. */
export default function OrganizerPage() {
  const { currentUser } = useCurrentUser();
  const directions = useAsync(() => directionsApi.list(), []);
  const statuses = useAsync(() => statusesApi.list(), []);

  const [directionId, setDirectionId] = useState('');
  const [status, setStatus] = useState('');

  // Организатор видит только свои направления.
  const myDirections = useMemo(
    () => (directions.data ?? []).filter((d) => d.organizerId === currentUser.id),
    [directions.data, currentUser.id],
  );

  // Пока пользователь ничего не выбрал - берём первый вариант из списка.
  const selectedDirectionId =
    directionId && myDirections.some((d) => String(d.id) === directionId)
      ? directionId
      : myDirections[0]
        ? String(myDirections[0].id)
        : '';
  const selectedStatus = status || statuses.data?.[0] || '';

  const applications = useAsync(
    () => organizerApplicationsApi.list(currentUser.id, Number(selectedDirectionId), selectedStatus),
    [currentUser.id, selectedDirectionId, selectedStatus],
    { enabled: Boolean(selectedDirectionId && selectedStatus) },
  );

  const loadError = directions.error || statuses.error;
  if (loadError) return <Notice type="error">{loadError.message}</Notice>;
  if (!directions.data || !statuses.data) return <p>Загрузка...</p>;
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
          label="Статус"
          value={selectedStatus}
          onChange={setStatus}
          options={statuses.data.map((s) => ({ value: s, label: labelOf(STATUS_LABELS, s) }))}
        />
      </div>

      {applications.error && <Notice type="error">{applications.error.message}</Notice>}
      {applications.loading && <p>Загрузка...</p>}
      {!applications.loading && applications.data && (
        <ApplicationList
          applications={applications.data}
          showParticipant
          emptyText="Заявок с таким статусом нет"
        />
      )}
    </div>
  );
}
