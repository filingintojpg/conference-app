import { useState } from 'react';
import { directionsApi, expertApplicationsApi } from '../../api';
import ApplicationList from '../../components/ApplicationList/ApplicationList';
import Notice from '../../components/Notice/Notice';
import Select from '../../components/Select/Select';
import { useCurrentUser } from '../../context/CurrentUserContext';
import { useAsync } from '../../hooks/useAsync';
import './ExpertPage.css';

/** Экран члена экспертной комиссии: подтверждённые заявки без данных об авторах. */
export default function ExpertPage() {
  const { currentUser } = useCurrentUser();
  const directions = useAsync(() => directionsApi.list(), []);

  // Пустая строка - "все направления".
  const [directionId, setDirectionId] = useState('');

  const applications = useAsync(
    () => expertApplicationsApi.list(currentUser.id, directionId),
    [currentUser.id, directionId],
  );

  if (directions.error) return <Notice type="error">{directions.error.message}</Notice>;
  if (!directions.data) return <p>Загрузка...</p>;

  return (
    <div className="expert-page">
      <h2>Подтверждённые заявки</h2>

      <Select
        label="Направление"
        value={directionId}
        onChange={setDirectionId}
        placeholder="Все"
        options={directions.data.map((d) => ({ value: String(d.id), label: d.name }))}
      />

      {applications.error && <Notice type="error">{applications.error.message}</Notice>}
      {applications.loading && <p>Загрузка...</p>}
      {!applications.loading && applications.data && (
        <ApplicationList
          applications={applications.data}
          emptyText="Подтверждённых заявок нет"
        />
      )}
    </div>
  );
}
