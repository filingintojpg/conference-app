import { APPLICATION_STATUS, REVIEW_STATUS } from '../../config';
import type { Application, Direction } from '../../types';
import { isPast } from '../../utils/format';

export interface ApplicationPermissions {
  canEdit: boolean;
  canWithdraw: boolean;
}

export function getApplicationPermissions(
  application: Application,
  direction?: Direction,
): ApplicationPermissions {
  const canWithdraw =
    application.status !== APPLICATION_STATUS.WITHDRAWN && !isPast(direction?.deadline);

  return {
    canWithdraw,
    canEdit: canWithdraw && application.reviewStatus === REVIEW_STATUS.PENDING,
  };
}
