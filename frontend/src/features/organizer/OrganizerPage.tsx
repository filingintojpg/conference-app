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
  REVIEW_STATUS,
  REVIEW_STATUS_LABELS,
  STATUS_LABELS,
  labelOf,
} from '../../config';
import { useRequiredUser } from '../../context/AuthContext';
import { useAsync } from '../../hooks/useAsync';
import type { Application, FlashMessage } from '../../types';
import { errorMessage } from '../../utils/format';
import OrganizerApplicationActions from './OrganizerApplicationActions';
import './OrganizerPage.css';

export default function OrganizerPage() {
  const currentUser = useRequiredUser();
  const directions = useAsync(() => directionsApi.list(), []);
  const statuses = useAsync(() => statusesApi.list(), []);
  const reviewStatuses = useAsync(() => reviewStatusesApi.list(), []);

  const [directionId, setDirectionId] = useState('');
  const [status, setStatus] = useState('');
  const [reviewStatus, setReviewStatus] = useState<string>(REVIEW_STATUS.PENDING);
  const [message, setMessage] = useState<FlashMessage | null>(null);
  const [processingApplicationId, setProcessingApplicationId] = useState<number | null>(null);

  const myDirections = useMemo(
    () => (directions.data ?? []).filter(({ organizerId }) => organizerId === currentUser.id),
    [directions.data, currentUser.id],
  );

  const selectedDirectionId = useMemo(() => {
    const selectionIsValid = myDirections.some(({ id }) => String(id) === directionId);
    if (selectionIsValid) return directionId;
    return myDirections[0] ? String(myDirections[0].id) : '';
  }, [directionId, myDirections]);

  const applications = useAsync(
    () =>
      organizerApplicationsApi.list(
        Number(selectedDirectionId),
        status,
        reviewStatus,
      ),
    [currentUser.id, selectedDirectionId, status, reviewStatus],
    { enabled: Boolean(selectedDirectionId) },
  );

  async function executeApplicationAction(
    application: Application,
    action: () => Promise<unknown>,
    successText: string,
  ) {
    setProcessingApplicationId(application.id);
    setMessage(null);

    try {
      await action();
      setMessage({ type: 'success', text: successText });
    } catch (error) {
      setMessage({ type: 'error', text: errorMessage(error) });
    } finally {
      setProcessingApplicationId(null);
      applications.reload();
    }
  }

  function reviewApplication(application: Application, decision: 'approve' | 'reject') {
    const successText =
      decision === 'approve' ? 'Заявка прошла проверку' : 'Заявка не прошла проверку';

    return executeApplicationAction(
      application,
      () => organizerApplicationsApi[decision](application.id),
      successText,
    );
  }

  function transferApplication(application: Application, targetDirectionId: string) {
    const targetDirection = directions.data?.find(
      ({ id }) => String(id) === targetDirectionId,
    );
    const directionName = targetDirection?.name ?? targetDirectionId;

    return executeApplicationAction(
      application,
      () => organizerApplicationsApi.transfer(application.id, Number(targetDirectionId)),
      `Заявка передана на направление «${directionName}», ожидает решения организатора`,
    );
  }

  const loadError = directions.error || statuses.error || reviewStatuses.error;
  if (loadError) return <Notice type="error">{loadError.message}</Notice>;
  if (!directions.data || !statuses.data || !reviewStatuses.data) return <p>Загрузка...</p>;
  if (myDirections.length === 0) {
    return <Notice type="info">У этого организатора нет направлений</Notice>;
  }

  const directionOptions = myDirections.map(({ id, name }) => ({
    value: String(id),
    label: name,
  }));
  const reviewStatusOptions = reviewStatuses.data.map((value) => ({
    value,
    label: labelOf(REVIEW_STATUS_LABELS, value),
  }));
  const statusOptions = statuses.data.map((value) => ({
    value,
    label: labelOf(STATUS_LABELS, value),
  }));

  return (
    <div className="organizer-page">
      <h2>Заявки по направлению</h2>

      <div className="organizer-page__filters">
        <Select
          label="Направление"
          value={selectedDirectionId}
          onChange={setDirectionId}
          options={directionOptions}
        />
        <Select
          label="Проверка"
          value={reviewStatus}
          onChange={setReviewStatus}
          placeholder="Все"
          options={reviewStatusOptions}
        />
        <Select
          label="Статус заявки"
          value={status}
          onChange={setStatus}
          placeholder="Все"
          options={statusOptions}
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
          renderActions={(application) => (
            <OrganizerApplicationActions
              application={application}
              directions={directions.data ?? []}
              selectedDirectionId={selectedDirectionId}
              busy={processingApplicationId === application.id}
              onApprove={(item) => reviewApplication(item, 'approve')}
              onReject={(item) => reviewApplication(item, 'reject')}
              onAcceptTransfer={(item) =>
                executeApplicationAction(
                  item,
                  () => organizerApplicationsApi.acceptTransfer(item.id),
                  'Заявка принята на данное направление',
                )
              }
              onRejectTransfer={(item) =>
                executeApplicationAction(
                  item,
                  () => organizerApplicationsApi.rejectTransfer(item.id),
                  'Передача заявки отвергнута, заявка осталась на прежнем направлении',
                )
              }
              onTransfer={transferApplication}
            />
          )}
        />
      )}
    </div>
  );
}
