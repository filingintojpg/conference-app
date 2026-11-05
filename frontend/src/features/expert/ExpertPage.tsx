import { directionsApi, expertApplicationsApi } from '../../api';
import ApplicationList from '../../components/ApplicationList/ApplicationList';
import Notice from '../../components/Notice/Notice';
import { useRequiredUser } from '../../context/CurrentUserContext';
import { useAsync } from '../../hooks/useAsync';
import './ExpertPage.css';

/** Экран члена экспертной комиссии: подтверждённые заявки своего направления без данных об авторах. */
export default function ExpertPage() {
  const currentUser = useRequiredUser();

  const directions = useAsync(() => directionsApi.list(), []);
  const direction = directions.data?.find((d) => d.expertId === currentUser.id);

  const applications = useAsync(
    () => expertApplicationsApi.list(currentUser.id),
    [currentUser.id],
  );

  return (
    <div className="expert-page">
      <h2>Подтверждённые заявки</h2>
      {direction && (
        <p>
          Направление: <strong>{direction.name}</strong>
        </p>
      )}

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
