// Типы данных, приходящих с бэкенда (см. dto/*.java).

export type Role = 'PARTICIPANT' | 'ORGANIZER' | 'EXPERT';
export type ApplicationStatus = 'SUBMITTED' | 'EDITED' | 'WITHDRAWN';
export type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
}

export interface Direction {
  id: number;
  name: string;
  deadline: string;
  organizerId: number;
  organizerFullName: string;
  expertId: number | null;
}

/** Заявка в виде, который видит член экспертной комиссии (без данных об авторе). */
export interface ExpertApplication {
  id: number;
  directionId: number;
  directionName: string;
  title: string;
  abstractText: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface Application extends ExpertApplication {
  participantId: number;
  participantFullName: string;
  participantEmail: string;
  status: ApplicationStatus;
  reviewStatus: ReviewStatus;
  transferredFromDirectionId: number | null;
  transferredFromDirectionName: string | null;
  transferToDirectionId: number | null;
  transferToDirectionName: string | null;
}

/** То, что умеет показать карточка: заявка эксперта или полная заявка. */
export type CardApplication = ExpertApplication & Partial<Application>;

export interface CreateApplicationPayload {
  directionId: number;
  title: string;
  abstractText: string;
  content: string;
}

export type UpdateApplicationPayload = Omit<CreateApplicationPayload, 'directionId'>;

export type NoticeType = 'error' | 'success' | 'info';

export interface FlashMessage {
  type: NoticeType;
  text: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  lastName: string;
  firstName: string;
  middleName?: string;
}
