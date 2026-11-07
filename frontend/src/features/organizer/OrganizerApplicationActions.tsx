import type { Application, Direction } from '../../types';
import { canReviewApplication, isIncomingTransfer, isWithdrawn } from './applicationRules';
import TransferAction from './TransferAction';

interface OrganizerApplicationActionsProps {
  application: Application;
  directions: Direction[];
  selectedDirectionId: string;
  busy: boolean;
  onApprove: (application: Application) => void;
  onReject: (application: Application) => void;
  onAcceptTransfer: (application: Application) => void;
  onRejectTransfer: (application: Application) => void;
  onTransfer: (application: Application, targetDirectionId: string) => void;
}

export default function OrganizerApplicationActions({
  application,
  directions,
  selectedDirectionId,
  busy,
  onApprove,
  onReject,
  onAcceptTransfer,
  onRejectTransfer,
  onTransfer,
}: OrganizerApplicationActionsProps) {
  if (isIncomingTransfer(application, selectedDirectionId)) {
    return (
      <>
        {!isWithdrawn(application) && (
          <button
            type="button"
            className="primary"
            disabled={busy}
            onClick={() => onAcceptTransfer(application)}
          >
            Принять на данное направление
          </button>
        )}
        <button
          type="button"
          className="danger"
          disabled={busy}
          onClick={() => onRejectTransfer(application)}
        >
          Отвергнуть
        </button>
      </>
    );
  }

  if (!canReviewApplication(application)) return null;

  return (
    <>
      <button
        type="button"
        className="primary"
        disabled={busy}
        onClick={() => onApprove(application)}
      >
        Подтвердить
      </button>
      <button
        type="button"
        className="danger"
        disabled={busy}
        onClick={() => onReject(application)}
      >
        Отклонить
      </button>
      <TransferAction
        directions={directions.filter((direction) => direction.id !== application.directionId)}
        disabled={busy}
        onTransfer={(targetId) => onTransfer(application, targetId)}
      />
    </>
  );
}
