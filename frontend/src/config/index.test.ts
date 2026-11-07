import { describe, expect, it } from 'vitest';
import {
  APPLICATION_STATUS,
  REVIEW_STATUS,
  REVIEW_STATUS_LABELS,
  ROLES,
  ROLE_LABELS,
  STATUS_LABELS,
  labelOf,
} from './index';

describe('labelOf', () => {
  it('возвращает подпись известного кода', () => {
    expect(labelOf(STATUS_LABELS, 'SUBMITTED')).toBe('Подана');
  });

  it('для неизвестного кода возвращает сам код', () => {
    expect(labelOf(STATUS_LABELS, 'SOMETHING_NEW')).toBe('SOMETHING_NEW');
  });
});

describe('справочники', () => {
  it('у каждой роли есть подпись', () => {
    expect(Object.keys(ROLE_LABELS).sort()).toEqual(Object.values(ROLES).sort());
  });

  it('у каждого статуса заявки есть подпись', () => {
    expect(Object.keys(STATUS_LABELS).sort()).toEqual(Object.values(APPLICATION_STATUS).sort());
  });

  it('у каждого статуса проверки есть подпись', () => {
    expect(Object.keys(REVIEW_STATUS_LABELS).sort()).toEqual(Object.values(REVIEW_STATUS).sort());
  });
});
