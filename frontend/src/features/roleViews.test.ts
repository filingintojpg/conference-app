import { describe, expect, it } from 'vitest';
import { ROLES } from '../config';
import { roleViews } from './roleViews';

describe('roleViews', () => {
  it('для каждой роли есть свой экран', () => {
    for (const role of Object.values(ROLES)) {
      expect(roleViews[role], `нет экрана для роли ${role}`).toBeTypeOf('function');
    }
  });

  it('у разных ролей разные экраны', () => {
    const screens = new Set(Object.values(roleViews));

    expect(screens.size).toBe(Object.keys(ROLES).length);
  });
});
