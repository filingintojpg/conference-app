import { useMemo, useState } from 'react';
import { directionsApi, participantApplicationsApi } from '../../api';
import ApplicationList from '../../components/ApplicationList/ApplicationList';
import Notice from '../../components/Notice/Notice';
import { REVIEW_STATUS } from '../../config';
import { useRequiredUser } from '../../context/AuthContext';
import { useAsync } from '../../hooks/useAsync';
import type { Application, Direction, FlashMessage } from '../../types';
import { errorMessage } from '../../utils/format';
import { getApplicationPermissions } from './applicationPermissions';
import ApplicationForm, { type ApplicationFormValues } from './ApplicationForm';
import './ParticipantPage.css';

type Tab = 'form' | 'list';

export default function ParticipantPage() {
  const currentUser = useRequiredUser();
  const directions = useAsync(() => directionsApi.list(), []);
  const applications = useAsync(
      () => participantApplicationsApi.list(),
      [currentUser.id],
  );

  const [tab, setTab] = useState<Tab>('form');
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
    setTab('list');
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
    setTab('form');
  };

  const handleCancel = () => {
    const wasEditing = editing !== null;
    resetForm();
    if (wasEditing) setTab('list');
  };

  const selectTab = (next: Tab) => {
    setMessage(null);
    setTab(next);
  };

  if (directions.loading && !directions.data) return <p>Загрузка...</p>;
  if (directions.error) return <Notice type="error">{directions.error.message}</Notice>;

  return (
      <div className="participant-page">
        <div className="participant-page__tabs" role="tablist" aria-label="Разделы участника">
          <button
              type="button"
              role="tab"
              id="participant-tab-form"
              aria-selected={tab === 'form'}
              aria-controls="participant-panel"
              className={`participant-page__tab${tab === 'form' ? ' participant-page__tab--active' : ''}`}
              onClick={() => selectTab('form')}
          >
            {editing ? 'Редактирование заявки' : 'Подать заявку'}
          </button>
          <button
              type="button"
              role="tab"
              id="participant-tab-list"
              aria-selected={tab === 'list'}
              aria-controls="participant-panel"
              className={`participant-page__tab${tab === 'list' ? ' participant-page__tab--active' : ''}`}
              onClick={() => selectTab('list')}
          >
            Мои заявки
          </button>
        </div>

        <div
            className="participant-page__content"
            role="tabpanel"
            id="participant-panel"
            aria-labelledby={tab === 'form' ? 'participant-tab-form' : 'participant-tab-list'}
        >
          {tab === 'form' && (
              <section className="participant-page__form">
                <ApplicationForm
                    key={`${editing?.id ?? 'new'}-${formKey}`}
                    directions={directions.data ?? []}
                    initial={editing}
                    onSubmit={handleSubmit}
                    onCancel={handleCancel}
                />
              </section>
          )}

          {tab === 'list' && (
              <section className="participant-page__list">
                {message && <Notice type={message.type}>{message.text}</Notice>}
                {applications.error && <Notice type="error">{applications.error.message}</Notice>}
                {applications.loading && !applications.data && <p>Загрузка...</p>}
                {applications.data && (
                    <ApplicationList
                        applications={applications.data}
                        emptyText="У вас пока нет заявок"
                        renderActions={(application) => {
                          const permissions = getApplicationPermissions(
                              application,
                              directionsById[application.directionId],
                          );

                          return (
                              <>
                                <button
                                    type="button"
                                    disabled={!permissions.canEdit}
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
                                    disabled={!permissions.canWithdraw}
                                    onClick={() => handleWithdraw(application)}
                                >
                                  Отозвать
                                </button>
                              </>
                          );
                        }}
                    />
                )}
              </section>
          )}
        </div>
      </div>
  );
}
