import { beforeEach, describe, expect, it, vi } from 'vitest';
import { authApi } from './auth';
import { directionsApi } from './directions';
import { expertApplicationsApi } from './expertApplications';
import { organizerApplicationsApi } from './organizerApplications';
import { participantApplicationsApi } from './participantApplications';
import { reviewStatusesApi } from './reviewStatuses';
import { statusesApi } from './statuses';
import { request } from './client';

vi.mock('./client', () => ({ request: vi.fn() }));

const payload = { title: 'T', abstractText: 'A', content: 'C' };

type Case = [name: string, call: () => unknown, expected: unknown[]];

const cases: Case[] = [
  ['directions.list', () => directionsApi.list(), ['/directions']],
  ['statuses.list', () => statusesApi.list(), ['/application-statuses']],
  ['reviewStatuses.list', () => reviewStatusesApi.list(), ['/review-statuses']],
  ['expert.list', () => expertApplicationsApi.list(), ['/expert/applications']],

  ['participant.list', () => participantApplicationsApi.list(), ['/applications']],
  [
    'participant.create',
    () => participantApplicationsApi.create({ directionId: 10, ...payload }),
    ['/applications', { method: 'POST', body: { directionId: 10, ...payload } }],
  ],
  [
    'participant.update',
    () => participantApplicationsApi.update(5, payload),
    ['/applications/5', { method: 'PUT', body: payload }],
  ],
  ['participant.withdraw', () => participantApplicationsApi.withdraw(5), ['/applications/5/withdraw', { method: 'POST' }]],

  [
    'organizer.list',
    () => organizerApplicationsApi.list(1, 'SUBMITTED', 'PENDING'),
    ['/organizer/applications', { params: { directionId: 1, status: 'SUBMITTED', reviewStatus: 'PENDING' } }],
  ],
  ['organizer.get', () => organizerApplicationsApi.get(5), ['/organizer/applications/5']],
  ['organizer.approve', () => organizerApplicationsApi.approve(5), ['/organizer/applications/5/approve', { method: 'POST' }]],
  ['organizer.reject', () => organizerApplicationsApi.reject(5), ['/organizer/applications/5/reject', { method: 'POST' }]],
  [
    'organizer.transfer',
    () => organizerApplicationsApi.transfer(5, 2),
    ['/organizer/applications/5/transfer', { method: 'POST', body: { targetDirectionId: 2 } }],
  ],
  [
    'organizer.acceptTransfer',
    () => organizerApplicationsApi.acceptTransfer(5),
    ['/organizer/applications/5/transfer/accept', { method: 'POST' }],
  ],
  [
    'organizer.rejectTransfer',
    () => organizerApplicationsApi.rejectTransfer(5),
    ['/organizer/applications/5/transfer/reject', { method: 'POST' }],
  ],

  [
    'auth.login',
    () => authApi.login({ email: 'a@b.c', password: 'secret-pass' }),
    ['/auth/login', { method: 'POST', body: { email: 'a@b.c', password: 'secret-pass' }, auth: false }],
  ],
  [
    'auth.register',
    () => authApi.register({ lastName: 'И', firstName: 'И', email: 'a@b.c', password: 'secret-pass' }),
    [
      '/auth/register',
      { method: 'POST', body: { lastName: 'И', firstName: 'И', email: 'a@b.c', password: 'secret-pass' }, auth: false },
    ],
  ],
  ['auth.logout', () => authApi.logout(), ['/auth/logout', { method: 'POST', auth: false }]],
  ['auth.me', () => authApi.me(), ['/auth/me']],
];

describe('API-модули: адреса и методы запросов', () => {
  beforeEach(() => {
    vi.mocked(request).mockReset();
    vi.mocked(request).mockResolvedValue(null);
  });

  it.each(cases)('%s', async (_name, call, expected) => {
    await call();

    expect(request).toHaveBeenCalledTimes(1);
    expect(request).toHaveBeenCalledWith(...expected);
  });
});
