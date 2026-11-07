import { useMemo, useState } from 'react';
import { directionsApi, participantApplicationsApi } from '../../api';
import ApplicationList from '../../components/ApplicationList/ApplicationList';
import Notice from '../../components/Notice/Notice';
import { APPLICATION_STATUS, REVIEW_STATUS } from '../../config';
import { useRequiredUser } from '../../context/AuthContext';
import { useAsync } from '../../hooks/useAsync';
import type { Application, Direction, FlashMessage } from '../../types';
import { errorMessage, isPast } from '../../utils/format';
import ApplicationForm, { type ApplicationFormValues } from './ApplicationForm';
import './ParticipantPage.css';

/** Экран участника: подать заявку, посмотреть свои, изменить или отозвать. */
export default function ParticipantPage() {
  const currentUser = useRequiredUser();
  const directions = useAsync(() => directionsApi.list(), []);
  const applications = useAsync(
    () => participantApplicationsApi.list(),
    [currentUser.id],
  );

  const [editing, setEditing] = useState<Application | null>(null);
  const [formKey, setFormKey] = useState(0);
  const [message, setMessage] = useState<FlashMessage | null>(null);

  const directionsById = useMemo(
    () =>
      Object.fromEntries((directions.data ?? []).map((d) => [d.id, d])) as Record<
        number,
        Direction | undefined
      >,
    [directions.data],
  );

  const canWithdraw = (application: Application) =>
    application.status !== APPLICATION_STATUS.WITHDRAWN &&
    !isPast(directionsById[application.directionId]?.deadline);

  // После проверки организатором заявку можно только отозвать.
  const canEdit = (application: Application) =>
    canWithdraw(application) && application.reviewStatus === REVIEW_STATUS.PENDING;

  const resetForm = () => {
    setEditing(null);
    setFormKey((key) => key + 1);
  };

  async function handleSubmit(values: ApplicationFormValues) {
    if (editing) {
      await participantApplicationsApi.update(editing.id, {
        title: values.title,
        abstractText: values.abstractText,
        content: values.content,
      });
      setMessage({ type: 'success', text: 'Заявка обновлена' });
    } else {
      await participantApplicationsApi.create({
        directionId: Number(values.directionId),
        title: values.title,
        abstractText: values.abstractText,
        content: values.content,
      });
      setMessage({ type: 'success', text: 'Заявка подана' });
    }
    resetForm();
    applications.reload();
  }

  async function handleWithdraw(application: Application) {
    if (!window.confirm(`Отозвать заявку «${application.title}»?`)) return;
    try {
      await participantApplicationsApi.withdraw(application.id);
      setMessage({ type: 'success', text: 'Заявка отозвана' });
      if (editing?.id === application.id) resetForm();
    } catch (err) {
      setMessage({ type: 'error', text: errorMessage(err) });
    }
    applications.reload();
  }

  const startEditing = (application: Application) => {
    setMessage(null);
    setEditing(application);
  };

  if (directions.loading && !directions.data) return <p>Загрузка...</p>;
  if (directions.error) return <Notice type="error">{directions.error.message}</Notice>;

  return (
    <div className="participant-page">
      <section className="participant-page__form">
        <ApplicationForm
          key={`${editing?.id ?? 'new'}-${formKey}`}
          directions={directions.data ?? []}
          initial={editing}
          onSubmit={handleSubmit}
          onCancel={resetForm}
        />
      </section>

      <section className="participant-page__list">
        <h2>Мои заявки</h2>
        {message && <Notice type={message.type}>{message.text}</Notice>}
        {applications.error && <Notice type="error">{applications.error.message}</Notice>}
        {applications.loading && !applications.data && <p>Загрузка...</p>}
        {applications.data && (
          <ApplicationList
            applications={applications.data}
            emptyText="У вас пока нет заявок"
            renderActions={(application) => (
              <>
                <button
                  type="button"
                  disabled={!canEdit(application)}
                  title={
                    application.reviewStatus !== REVIEW_STATUS.PENDING
                      ? 'Заявка уже проверена - её можно только отозвать'
                      : undefined
                  }
                  onClick={() => startEditing(application)}
                >
                  Редактировать
                </button>
                <button
                  type="button"
                  className="danger"
                  disabled={!canWithdraw(application)}
                  onClick={() => handleWithdraw(application)}
                >
                  Отозвать
                </button>
              </>
            )}
          />
        )}
      </section>
    </div>
  );
}
