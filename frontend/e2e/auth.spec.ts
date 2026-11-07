import { expect, test } from '@playwright/test';
import { CSRF_VALUE, IVAN, VALID_PASSWORD, mockApi } from './helpers/mockApi';

test.describe('вход и выход', () => {
  test('гостя с главной страницы перенаправляет на вход', async ({ page }) => {
    await mockApi(page);

    await page.goto('/');

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('heading', { name: 'Вход' })).toBeVisible();
  });

  test('успешный вход открывает экран участника', async ({ page }) => {
    await mockApi(page);
    await page.goto('/login');

    await page.getByLabel('Email', { exact: true }).fill(IVAN.email);
    await page.getByLabel('Пароль', { exact: true }).fill(VALID_PASSWORD);
    await page.getByRole('button', { name: 'Войти' }).click();

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();
    await expect(page.getByText('Иванов Иван (участник)')).toBeVisible();
  });

  test('неверный пароль: ошибка сервера и остаёмся на странице входа', async ({ page }) => {
    await mockApi(page);
    await page.goto('/login');

    await page.getByLabel('Email', { exact: true }).fill(IVAN.email);
    await page.getByLabel('Пароль', { exact: true }).fill('wrong-password');
    await page.getByRole('button', { name: 'Войти' }).click();

    await expect(page.getByText('Invalid email or password')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('сессия переживает перезагрузку страницы (восстановление по cookie)', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();

    await page.reload();
    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();
  });

  test('токен не хранится в localStorage и sessionStorage', async ({ page }) => {
    await mockApi(page);
    await page.goto('/login');
    await page.getByLabel('Email', { exact: true }).fill(IVAN.email);
    await page.getByLabel('Пароль', { exact: true }).fill(VALID_PASSWORD);
    await page.getByRole('button', { name: 'Войти' }).click();
    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();

    const storage = await page.evaluate(() => ({
      local: Object.keys(localStorage),
      session: Object.keys(sessionStorage),
    }));

    expect(storage).toEqual({ local: [], session: [] });
  });

  test('выход отправляет CSRF-заголовок и возвращает на страницу входа', async ({ page }) => {
    const api = await mockApi(page, IVAN);
    await page.goto('/');
    await expect(page.getByText('Иванов Иван (участник)')).toBeVisible();

    await page.getByRole('button', { name: 'Выйти' }).click();

    await expect(page).toHaveURL(/\/login$/);
    const logout = api.requests.find((r) => r.method === 'POST' && r.path === '/auth/logout');
    expect(logout?.csrfHeader).toBe(CSRF_VALUE);
  });
});

test.describe('регистрация', () => {
  test('несовпадающие пароли не отправляются на сервер', async ({ page }) => {
    const api = await mockApi(page);
    await page.goto('/register');

    await page.getByLabel('Фамилия', { exact: true }).fill('Иванов');
    await page.getByLabel('Имя', { exact: true }).fill('Иван');
    await page.getByLabel('Email', { exact: true }).fill(IVAN.email);
    await page.getByLabel('Пароль', { exact: true }).fill(VALID_PASSWORD);
    await page.getByLabel('Повторите пароль', { exact: true }).fill('другой-пароль');
    await page.getByRole('button', { name: 'Зарегистрироваться' }).click();

    await expect(page.getByText('Пароли не совпадают')).toBeVisible();
    expect(api.requests.some((r) => r.path === '/auth/register')).toBe(false);
  });

  test('успешная регистрация сразу входит в систему', async ({ page }) => {
    await mockApi(page);
    await page.goto('/register');

    await page.getByLabel('Фамилия', { exact: true }).fill('Иванов');
    await page.getByLabel('Имя', { exact: true }).fill('Иван');
    await page.getByLabel('Email', { exact: true }).fill(IVAN.email);
    await page.getByLabel('Пароль', { exact: true }).fill(VALID_PASSWORD);
    await page.getByLabel('Повторите пароль', { exact: true }).fill(VALID_PASSWORD);
    await page.getByRole('button', { name: 'Зарегистрироваться' }).click();

    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();
  });
});
