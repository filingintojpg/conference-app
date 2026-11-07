import { expect, type Locator, type Page, type Route } from '@playwright/test';

export type MockRole = 'PARTICIPANT' | 'ORGANIZER' | 'EXPERT';

export interface MockUser {
  id: number;
  fullName: string;
  email: string;
  role: MockRole;
}

export const VALID_PASSWORD = 'secret-pass';
export const CSRF_VALUE = 'test-csrf-token';

export const IVAN: MockUser = { id: 1, fullName: 'Иванов Иван', email: 'ivan@example.com', role: 'PARTICIPANT' };
export const PETR: MockUser = { id: 2, fullName: 'Петров Пётр', email: 'petr@example.com', role: 'PARTICIPANT' };

export const ANNA: MockUser = { id: 10, fullName: 'Сидорова Анна', email: 'anna@example.com', role: 'ORGANIZER' };
export const OLEG: MockUser = { id: 11, fullName: 'Кузнецов Олег', email: 'oleg@example.com', role: 'ORGANIZER' };
export const ORGANIZER_WITHOUT_DIRECTIONS: MockUser = {
  id: 12,
  fullName: 'Новикова Елена',
  email: 'elena@example.com',
  role: 'ORGANIZER',
};

export const MARIA: MockUser = { id: 20, fullName: 'Смирнова Мария', email: 'maria@example.com', role: 'EXPERT' };
export const EXPERT_WITHOUT_DIRECTION: MockUser = {
  id: 21,
  fullName: 'Орлов Дмитрий',
  email: 'dmitry@example.com',
  role: 'EXPERT',
};

const ALL_USERS: MockUser[] = [IVAN, PETR, ANNA, OLEG, ORGANIZER_WITHOUT_DIRECTIONS, MARIA, EXPERT_WITHOUT_DIRECTION];

export const ROLE_SCREENS: { user: MockUser; roleLabel: string; heading: string }[] = [
  { user: IVAN, roleLabel: 'участник', heading: 'Мои заявки' },
  { user: ANNA, roleLabel: 'организатор', heading: 'Заявки по направлению' },
  { user: MARIA, roleLabel: 'член экспертной комиссии', heading: 'Подтверждённые заявки' },
];

interface MockDirection {
  id: number;
  name: string;
  deadline: string;
  organizerId: number;
  organizerFullName: string;
  expertId: number | null;
}

export const AI_DIRECTION = 'Искусственный интеллект';
export const CYBER_DIRECTION = 'Кибербезопасность';
export const ARCHIVE_DIRECTION = 'Архивное направление';

const FUTURE = '2099-01-01T12:00:00Z';
const PAST = '2020-01-01T12:00:00Z';

const DIRECTIONS: MockDirection[] = [
  { id: 1, name: AI_DIRECTION, deadline: FUTURE, organizerId: ANNA.id, organizerFullName: ANNA.fullName, expertId: MARIA.id },
  { id: 2, name: CYBER_DIRECTION, deadline: FUTURE, organizerId: OLEG.id, organizerFullName: OLEG.fullName, expertId: null },

  { id: 3, name: ARCHIVE_DIRECTION, deadline: PAST, organizerId: OLEG.id, organizerFullName: OLEG.fullName, expertId: null },
];

type ApplicationStatus = 'SUBMITTED' | 'EDITED' | 'WITHDRAWN';
type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface MockApplication {
  id: number;
  participantId: number;
  directionId: number;
  title: string;
  abstractText: string;
  content: string;
  status: ApplicationStatus;
  reviewStatus: ReviewStatus;
  transferredFromDirectionId: number | null;
  transferToDirectionId: number | null;
  createdAt: string;
  updatedAt: string;
}

let nextSeedId = 1000;

export function makeApplication(overrides: Partial<MockApplication> & { title: string }): MockApplication {
  return {
    id: nextSeedId++,
    participantId: IVAN.id,
    directionId: 1,
    abstractText: `Аннотация: ${overrides.title}`,
    content: `Содержание: ${overrides.title}`,
    status: 'SUBMITTED',
    reviewStatus: 'PENDING',
    transferredFromDirectionId: null,
    transferToDirectionId: null,
    createdAt: '2026-10-01T09:00:00Z',
    updatedAt: '2026-10-01T09:00:00Z',
    ...overrides,
  };
}

export function defaultApplications(): MockApplication[] {
  return [

    makeApplication({ id: 1, title: 'Нейросети в медицине' }),
    makeApplication({ id: 2, title: 'Генерация текстов', reviewStatus: 'APPROVED' }),
    makeApplication({ id: 3, title: 'Защита паролей', directionId: 2 }),
    makeApplication({ id: 9, title: 'Старая работа', directionId: 3 }),

    makeApplication({ id: 4, title: 'Распознавание речи', participantId: PETR.id }),
    makeApplication({ id: 5, title: 'Отозванная работа', participantId: PETR.id, status: 'WITHDRAWN' }),
    makeApplication({ id: 6, title: 'Анализ вредоносного кода', participantId: PETR.id, directionId: 2, reviewStatus: 'APPROVED' }),
    makeApplication({ id: 7, title: 'Этика искусственного интеллекта', participantId: PETR.id, reviewStatus: 'APPROVED' }),
    makeApplication({ id: 8, title: 'Снятая с конференции', participantId: PETR.id, reviewStatus: 'APPROVED', status: 'WITHDRAWN' }),
  ];
}

const API = 'http://localhost:8080';
const ORIGIN = 'http://localhost:5173';

export interface RecordedRequest {
  method: string;
  path: string;
  csrfHeader: string | undefined;
  body: unknown;
}

export interface MockApi {

  requests: RecordedRequest[];

  setSession: (user: MockUser | null) => void;

  applications: () => MockApplication[];

  failNext: (method: string, path: RegExp | string, status: number, message: string) => void;

  setOffline: (offline: boolean) => void;
}

export interface MockApiOptions {
  user?: MockUser | null;
  applications?: MockApplication[];
}

const corsHeaders = {
  'Access-Control-Allow-Origin': ORIGIN,
  'Access-Control-Allow-Credentials': 'true',
  'Access-Control-Allow-Headers': 'Content-Type, X-XSRF-TOKEN, Authorization',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
};

function reply(route: Route, status: number, body?: unknown) {
  return route.fulfill({
    status,
    headers: corsHeaders,
    contentType: 'application/json',
    body: body === undefined ? '' : JSON.stringify(body),
  });
}

class HttpError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

const directionById = (id: number) => DIRECTIONS.find((d) => d.id === id);

export async function mockApi(
  page: Page,
  userOrOptions: MockUser | null | MockApiOptions = null,
): Promise<MockApi> {
  const options: MockApiOptions =
    userOrOptions && 'role' in userOrOptions ? { user: userOrOptions } : (userOrOptions ?? {});

  let session: MockUser | null = options.user ?? null;
  let offline = false;
  const applications = (options.applications ?? defaultApplications()).map((a) => ({ ...a }));
  const requests: RecordedRequest[] = [];
  const failures: { method: string; path: RegExp | string; status: number; message: string }[] = [];
  const registeredUsers = [...ALL_USERS];

  await page.context().addCookies([{ name: 'XSRF-TOKEN', value: CSRF_VALUE, url: ORIGIN }]);

  const now = () => new Date().toISOString();
  const nextId = () => Math.max(0, ...applications.map((a) => a.id)) + 1;

  const requireRole = (role: MockRole): MockUser => {
    if (!session) throw new HttpError(401, 'Authentication required');
    if (session.role !== role) throw new HttpError(403, 'Access denied');
    return session;
  };

  const requireSession = () => {
    if (!session) throw new HttpError(401, 'Authentication required');
  };

  const findApplication = (id: number) => {
    const application = applications.find((a) => a.id === id);
    if (!application) throw new HttpError(404, `Application not found: ${id}`);
    return application;
  };

  const toResponse = (a: MockApplication) => {
    const participant = registeredUsers.find((u) => u.id === a.participantId);
    const from = a.transferredFromDirectionId ? directionById(a.transferredFromDirectionId) : null;
    const to = a.transferToDirectionId ? directionById(a.transferToDirectionId) : null;
    return {
      ...a,
      directionName: directionById(a.directionId)?.name ?? '',
      participantFullName: participant?.fullName ?? '',
      participantEmail: participant?.email ?? '',
      transferredFromDirectionName: from?.name ?? null,
      transferToDirectionName: to?.name ?? null,
    };
  };

  const toExpertResponse = (a: MockApplication) => ({
    id: a.id,
    directionId: a.directionId,
    directionName: directionById(a.directionId)?.name ?? '',
    title: a.title,
    abstractText: a.abstractText,
    content: a.content,
    createdAt: a.createdAt,
    updatedAt: a.updatedAt,
  });

  const ensureOwnedByOrganizer = (direction: MockDirection | undefined, organizer: MockUser) => {
    if (!direction) throw new HttpError(404, 'Direction not found');
    if (direction.organizerId !== organizer.id) throw new HttpError(403, 'Direction belongs to another organizer');
  };

  const ensureDeadlineNotPassed = (directionId: number) => {
    const direction = directionById(directionId);
    if (direction && new Date(direction.deadline).getTime() <= Date.now()) {
      throw new HttpError(409, 'Приём заявок на это направление закрыт');
    }
  };

  function handle(method: string, url: URL, body: any): [number, unknown?] {
    const path = url.pathname;
    let match: RegExpMatchArray | null;

    if (path === '/auth/me' && method === 'GET') {
      requireSession();
      return [200, session];
    }
    if (path === '/auth/login' && method === 'POST') {
      const user = registeredUsers.find((u) => u.email === body.email);
      if (!user || body.password !== VALID_PASSWORD) throw new HttpError(401, 'Invalid email or password');
      session = user;
      return [200, user];
    }
    if (path === '/auth/register' && method === 'POST') {
      const user: MockUser = {
        id: 100 + registeredUsers.length,
        fullName: [body.lastName, body.firstName, body.middleName].filter(Boolean).join(' '),
        email: body.email,
        role: 'PARTICIPANT',
      };
      registeredUsers.push(user);
      session = user;
      return [201, user];
    }
    if (path === '/auth/logout' && method === 'POST') {
      session = null;
      return [204];
    }

    if (path === '/directions' && method === 'GET') {
      requireSession();
      return [200, DIRECTIONS];
    }
    if (path === '/application-statuses' && method === 'GET') {
      requireSession();
      return [200, ['SUBMITTED', 'EDITED', 'WITHDRAWN']];
    }
    if (path === '/review-statuses' && method === 'GET') {
      requireSession();
      return [200, ['PENDING', 'APPROVED', 'REJECTED']];
    }

    if (path === '/applications' && method === 'GET') {
      const user = requireRole('PARTICIPANT');
      const own = applications.filter((a) => a.participantId === user.id).sort((a, b) => b.id - a.id);
      return [200, own.map(toResponse)];
    }
    if (path === '/applications' && method === 'POST') {
      const user = requireRole('PARTICIPANT');
      if (!directionById(body.directionId)) throw new HttpError(404, 'Direction not found');
      ensureDeadlineNotPassed(body.directionId);
      const created = makeApplication({
        id: nextId(),
        participantId: user.id,
        directionId: body.directionId,
        title: body.title,
        abstractText: body.abstractText,
        content: body.content,
        createdAt: now(),
        updatedAt: now(),
      });
      applications.push(created);
      return [201, toResponse(created)];
    }
    if ((match = path.match(/^\/applications\/(\d+)$/)) && method === 'PUT') {
      const user = requireRole('PARTICIPANT');
      const application = findApplication(Number(match[1]));
      if (application.participantId !== user.id) throw new HttpError(403, 'Application belongs to another participant');
      if (application.status === 'WITHDRAWN') throw new HttpError(409, 'Application is already withdrawn');
      if (application.reviewStatus !== 'PENDING') {
        throw new HttpError(409, 'Application has already been reviewed and cannot be edited');
      }
      ensureDeadlineNotPassed(application.directionId);
      Object.assign(application, {
        title: body.title,
        abstractText: body.abstractText,
        content: body.content,
        status: 'EDITED',
        updatedAt: now(),
      });
      return [200, toResponse(application)];
    }
    if ((match = path.match(/^\/applications\/(\d+)\/withdraw$/)) && method === 'POST') {
      const user = requireRole('PARTICIPANT');
      const application = findApplication(Number(match[1]));
      if (application.participantId !== user.id) throw new HttpError(403, 'Application belongs to another participant');
      if (application.status === 'WITHDRAWN') throw new HttpError(409, 'Application is already withdrawn');
      ensureDeadlineNotPassed(application.directionId);
      application.status = 'WITHDRAWN';
      application.updatedAt = now();
      return [200, toResponse(application)];
    }

    if (path === '/organizer/applications' && method === 'GET') {
      const organizer = requireRole('ORGANIZER');
      const directionId = Number(url.searchParams.get('directionId'));
      const status = url.searchParams.get('status');
      const reviewStatus = url.searchParams.get('reviewStatus');
      ensureOwnedByOrganizer(directionById(directionId), organizer);

      const matches = (a: MockApplication) =>
        (!status || a.status === status) && (!reviewStatus || a.reviewStatus === reviewStatus);

      const incoming = applications.filter((a) => a.transferToDirectionId === directionId && matches(a));
      const own = applications.filter((a) => a.directionId === directionId && matches(a));
      return [200, [...incoming, ...own].map(toResponse)];
    }
    if ((match = path.match(/^\/organizer\/applications\/(\d+)\/(approve|reject)$/)) && method === 'POST') {
      const organizer = requireRole('ORGANIZER');
      const application = findApplication(Number(match[1]));
      ensureOwnedByOrganizer(directionById(application.directionId), organizer);
      if (application.status === 'WITHDRAWN') throw new HttpError(409, 'Application is withdrawn and cannot be reviewed');
      if (application.reviewStatus !== 'PENDING') throw new HttpError(409, 'Application has already been reviewed');
      if (application.transferToDirectionId) {
        throw new HttpError(409, 'Application is waiting for a transfer decision and cannot be reviewed');
      }
      application.reviewStatus = match[2] === 'approve' ? 'APPROVED' : 'REJECTED';
      application.updatedAt = now();
      return [200, toResponse(application)];
    }
    if ((match = path.match(/^\/organizer\/applications\/(\d+)\/transfer$/)) && method === 'POST') {
      const organizer = requireRole('ORGANIZER');
      const application = findApplication(Number(match[1]));
      ensureOwnedByOrganizer(directionById(application.directionId), organizer);
      if (!directionById(body.targetDirectionId)) throw new HttpError(404, 'Direction not found');
      if (body.targetDirectionId === application.directionId) {
        throw new HttpError(409, 'Application already belongs to this direction');
      }
      if (application.status === 'WITHDRAWN') throw new HttpError(409, 'Application is withdrawn and cannot be transferred');
      if (application.reviewStatus !== 'PENDING') {
        throw new HttpError(409, 'Application has already been reviewed and cannot be transferred');
      }
      if (application.transferToDirectionId) {
        throw new HttpError(409, 'Application is already waiting for a transfer decision');
      }
      application.transferredFromDirectionId = application.directionId;
      application.transferToDirectionId = body.targetDirectionId;
      application.updatedAt = now();
      return [200, toResponse(application)];
    }
    if ((match = path.match(/^\/organizer\/applications\/(\d+)\/transfer\/(accept|reject)$/)) && method === 'POST') {
      const organizer = requireRole('ORGANIZER');
      const application = findApplication(Number(match[1]));
      if (!application.transferToDirectionId) throw new HttpError(409, 'Application has no pending transfer');
      ensureOwnedByOrganizer(directionById(application.transferToDirectionId), organizer);
      if (match[2] === 'accept') {
        if (application.status === 'WITHDRAWN') throw new HttpError(409, 'Application is withdrawn and cannot be accepted');
        application.directionId = application.transferToDirectionId;
      } else {
        application.transferredFromDirectionId = null;
      }
      application.transferToDirectionId = null;
      application.updatedAt = now();
      return [200, toResponse(application)];
    }

    if (path === '/expert/applications' && method === 'GET') {
      const expert = requireRole('EXPERT');
      const direction = DIRECTIONS.find((d) => d.expertId === expert.id);
      if (!direction) throw new HttpError(403, 'Expert is not assigned to a direction');
      const visible = applications.filter(
        (a) => a.directionId === direction.id && a.reviewStatus === 'APPROVED' && a.status !== 'WITHDRAWN',
      );
      return [200, visible.map(toExpertResponse)];
    }

    throw new HttpError(404, `Нет подмены для ${method} ${path}`);
  }

  await page.route(`${API}

export async function loginAs(page: Page, user: MockUser, password = VALID_PASSWORD): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Email', { exact: true }).fill(user.email);
  await page.getByLabel('Пароль', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Войти' }).click();
}

export async function logout(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Выйти' }).click();
  await expect(page).toHaveURL(/\/login$/);
}

export function card(page: Page, title: string): Locator {
  return page.getByRole('article').filter({ has: page.getByRole('heading', { name: title, exact: true }) });
}

export function answerConfirm(page: Page, accept: boolean): void {
  page.once('dialog', (dialog) => void (accept ? dialog.accept() : dialog.dismiss()));
}

export function selectField(page: Page, label: string): Locator {
  return page
    .locator('label.select')
    .filter({ has: page.getByText(label, { exact: true }) })
    .locator('select');
}
