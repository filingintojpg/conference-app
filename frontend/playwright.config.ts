import { defineConfig, devices } from '@playwright/test';

// E2E-тесты открывают настоящее приложение в браузере. Запросы к бэкенду подменяются (e2e/helpers/mockApi.ts),
// поэтому для запуска нужен только фронтенд: Playwright сам поднимает `npm run dev`.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
  },
});
