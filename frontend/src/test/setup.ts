import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// Без globals Testing Library сама не убирает отрисованные компоненты между тестами.
afterEach(() => {
  cleanup();
});
