import { expect, test } from '@playwright/test';
import { IVAN, ROLE_SCREENS, VALID_PASSWORD, answerConfirm, card, loginAs, logout, mockApi } from './helpers/mockApi';

test.describe('вход в систему', () => {
  test('гостя с главной страницы перенаправляет на вход', async ({ page }) => {
    await mockApi(page);

    await page.goto('/');

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('heading', { name: 'Вход' })).toBeVisible();
  });

  for (const { user, roleLabel, heading } of ROLE_SCREENS) {
    test(`вход: ${roleLabel} попадает на свой экран`, async ({ page }) => {
      await mockApi(page);

      await loginAs(page, user);

      await expect(page).toHaveURL('/');
      await expect(page.getByRole('heading', { name: heading })).toBeVisible();
      await expect(page.getByText(`${user.fullName} (${roleLabel})`)).toBeVisible();
    });
  }

  test('неверный пароль: показано сообщение об ошибке, пользователь остаётся на странице входа', async ({ page }) => {
    await mockApi(page);

    await loginAs(page, IVAN, 'wrong-password');

    await expect(page.getByText('Invalid email or password')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('неизвестный email: войти нельзя', async ({ page }) => {
    await mockApi(page);

    await loginAs(page, { ...IVAN, email: 'nobody@example.com' });

    await expect(page.getByText('Invalid email or password')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('с пустыми полями форма входа ничего не отправляет', async ({ page }) => {
    const api = await mockApi(page);
    await page.goto('/login');

    await page.getByRole('button', { name: 'Войти' }).click();

    await expect(page).toHaveURL(/\/login$/);
    expect(api.requests.some((r) => r.path === '/auth/login')).toBe(false);
  });

  test('если сервер недоступен, пользователь видит понятное сообщение', async ({ page }) => {
    const api = await mockApi(page);
    await page.goto('/login');
    api.setOffline(true);

    await page.getByLabel('Email', { exact: true }).fill(IVAN.email);
    await page.getByLabel('Пароль', { exact: true }).fill(VALID_PASSWORD);
    await page.getByRole('button', { name: 'Войти' }).click();

    await expect(page.getByText('Бэкенд недоступен. Проверьте, что он запущен.')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('уже вошедшего пользователя со страницы входа перенаправляет на его экран', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/login');

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();
  });
});

test.describe('сессия', () => {
  for (const { user, heading, roleLabel } of ROLE_SCREENS) {
    test(`после обновления страницы ${roleLabel} остаётся в системе`, async ({ page }) => {
      await mockApi(page, user);

      await page.goto('/');
      await expect(page.getByRole('heading', { name: heading })).toBeVisible();

      await page.reload();

      await expect(page.getByRole('heading', { name: heading })).toBeVisible();
      await expect(page.getByText(user.fullName)).toBeVisible();
    });
  }

  for (const { user, roleLabel } of ROLE_SCREENS) {
    test(`выход: ${roleLabel} возвращается на страницу входа и не может вернуться назад без пароля`, async ({ page }) => {
      const api = await mockApi(page, user);
      await page.goto('/');
      await expect(page.getByText(`${user.fullName} (${roleLabel})`)).toBeVisible();

      await logout(page);

      expect(api.requests.some((r) => r.method === 'POST' && r.path === '/auth/logout')).toBe(true);
      await page.goto('/');
      await expect(page).toHaveURL(/\/login$/);
      await page.reload();
      await expect(page.getByRole('heading', { name: 'Вход' })).toBeVisible();
    });
  }

  test('если время сессии истекло, пользователя возвращают на страницу входа', async ({ page }) => {
    const api = await mockApi(page, IVAN);
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();

    api.setSession(null);
    answerConfirm(page, true);
    await card(page, 'Защита паролей').getByRole('button', { name: 'Отозвать' }).click();

    await expect(page).toHaveURL(/\/login$/);
  });

  test('неизвестный адрес ведёт на главную страницу', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/такой-страницы-нет');

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();
  });
});

test.describe('регистрация', () => {
  async function fillRegistration(page: import('@playwright/test').Page, repeatPassword = VALID_PASSWORD) {
    await page.goto('/register');
    await page.getByLabel('Фамилия', { exact: true }).fill('Фёдоров');
    await page.getByLabel('Имя', { exact: true }).fill('Фёдор');
    await page.getByLabel('Email', { exact: true }).fill('fedor@example.com');
    await page.getByLabel('Пароль', { exact: true }).fill(VALID_PASSWORD);
    await page.getByLabel('Повторите пароль', { exact: true }).fill(repeatPassword);
    await page.getByRole('button', { name: 'Зарегистрироваться' }).click();
  }

  test('если пароли не совпадают, заявка на регистрацию не отправляется', async ({ page }) => {
    const api = await mockApi(page);

    await fillRegistration(page, 'другой-пароль');

    await expect(page.getByText('Пароли не совпадают')).toBeVisible();
    expect(api.requests.some((r) => r.path === '/auth/register')).toBe(false);
  });

  test('после регистрации новый участник сразу попадает в систему', async ({ page }) => {
    await mockApi(page);

    await fillRegistration(page);

    await expect(page.getByRole('heading', { name: 'Мои заявки' })).toBeVisible();
    await expect(page.getByText('Фёдоров Фёдор (участник)')).toBeVisible();
    await expect(page.getByText('У вас пока нет заявок')).toBeVisible();
  });

  test('если email уже занят, показано сообщение и регистрация не проходит', async ({ page }) => {
    const api = await mockApi(page);
    api.failNext('POST', '/auth/register', 409, 'Email already registered');

    await fillRegistration(page);

    await expect(page.getByText('Email already registered')).toBeVisible();
    await expect(page).toHaveURL(/\/register$/);
  });

  test('со страницы входа можно перейти к регистрации и обратно', async ({ page }) => {
    await mockApi(page);
    await page.goto('/login');

    await page.getByRole('link', { name: 'Зарегистрироваться' }).click();
    await expect(page.getByRole('heading', { name: 'Регистрация' })).toBeVisible();

    await page.getByRole('link', { name: 'Войти' }).click();
    await expect(page.getByRole('heading', { name: 'Вход' })).toBeVisible();
  });

  test('уже вошедшего пользователя со страницы регистрации перенаправляет на главную', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/register');

    await expect(page).toHaveURL('/');
  });
});
