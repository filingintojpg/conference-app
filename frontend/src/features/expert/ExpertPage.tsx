import { useState } from 'react';
import { directionsApi, expertApplicationsApi } from '../../api';
import ApplicationList from '../../components/ApplicationList/ApplicationList';
import Notice from '../../components/Notice/Notice';
import { useRequiredUser } from '../../context/AuthContext';
import { useAsync } from '../../hooks/useAsync';
import type { FlashMessage } from '../../types';
import { errorMessage } from '../../utils/format';
import './ExpertPage.css';

export default function ExpertPage() {
  const currentUser = useRequiredUser();

  const directions = useAsync(() => directionsApi.list(), []);
  const direction = directions.data?.find((d) => d.expertId === currentUser.id);

  const applications = useAsync(
    () => expertApplicationsApi.list(),
    [currentUser.id],
  );

  const [message, setMessage] = useState<FlashMessage | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleDeleteAll(count: number) {
    const target = direction ? ` по направлению «${direction.name}»` : '';
    if (!window.confirm(`Удалить все заявки (${count})${target}? Это действие необратимо.`)) return;

    setDeleting(true);
    setMessage(null);
    try {
      const { deleted } = await expertApplicationsApi.deleteAll();
      setMessage({ type: 'success', text: `Удалено заявок: ${deleted}` });
    } catch (error) {
      setMessage({ type: 'error', text: errorMessage(error) });
    } finally {
      setDeleting(false);
      applications.reload();
    }
  }

  const count = applications.data?.length ?? 0;

  return (
    <div className="expert-page">
      <h2>Подтверждённые заявки</h2>
      {direction && (
        <p>
          Направление: <strong>{direction.name}</strong>
        </p>
      )}

      {count > 0 && (
        <div className="expert-page__actions">
          <button
            type="button"
            className="danger"
            disabled={deleting}
            onClick={() => void handleDeleteAll(count)}
          >
            Удалить все заявки
          </button>
        </div>
      )}

      {message && <Notice type={message.type}>{message.text}</Notice>}
      {applications.error && <Notice type="error">{applications.error.message}</Notice>}
      {applications.loading && !applications.data && <p>Загрузка...</p>}
      {applications.data && (
        <ApplicationList
          applications={applications.data}
          emptyText="Подтверждённых заявок нет"
        />
      )}
    </div>
  );
}
