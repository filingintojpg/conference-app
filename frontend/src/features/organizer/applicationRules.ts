import { APPLICATION_STATUS, REVIEW_STATUS } from '../../config';
import type { Application } from '../../types';

export function hasPendingTransfer(application: Application): boolean {
  return Boolean(application.transferToDirectionId);
}

export function isWithdrawn(application: Application): boolean {
  return application.status === APPLICATION_STATUS.WITHDRAWN;
}

export function isIncomingTransfer(
  application: Application,
  selectedDirectionId: string,
): boolean {
  return (
    hasPendingTransfer(application) &&
    String(application.transferToDirectionId) === selectedDirectionId
  );
}

export function canReviewApplication(application: Application): boolean {
  return (
    !isWithdrawn(application) &&
    application.reviewStatus === REVIEW_STATUS.PENDING &&
    !hasPendingTransfer(application)
  );
}
