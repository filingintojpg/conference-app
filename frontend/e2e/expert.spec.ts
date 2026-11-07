import { expect, test } from '@playwright/test';
import { AI_DIRECTION, EXPERT_WITHOUT_DIRECTION, IVAN, MARIA, PETR, card, mockApi } from './helpers/mockApi';

test.describe('эксперт', () => {
  test('эксперт видит название своего направления и подтверждённые заявки', async ({ page }) => {
    await mockApi(page, MARIA);

    await page.goto('/');

    await expect(page.getByRole('heading', { name: 'Подтверждённые заявки' })).toBeVisible();
    await expect(page.locator('p').filter({ hasText: `Направление: ${AI_DIRECTION}` })).toBeVisible();
    await expect(card(page, 'Генерация текстов')).toBeVisible();
    await expect(card(page, 'Этика искусственного интеллекта')).toBeVisible();
  });

  test('эксперт не видит заявки, которые ещё не подтверждены организатором', async ({ page }) => {
    await mockApi(page, MARIA);

    await page.goto('/');
    await expect(card(page, 'Генерация текстов')).toBeVisible();

    await expect(page.getByRole('heading', { name: 'Нейросети в медицине' })).toHaveCount(0);
    await expect(page.getByRole('heading', { name: 'Распознавание речи' })).toHaveCount(0);
  });

  test('эксперт не видит отозванные заявки, даже если они были подтверждены', async ({ page }) => {
    await mockApi(page, MARIA);

    await page.goto('/');
    await expect(card(page, 'Генерация текстов')).toBeVisible();

    await expect(page.getByRole('heading', { name: 'Снятая с конференции' })).toHaveCount(0);
  });

  test('эксперт не видит заявки чужих направлений', async ({ page }) => {
    await mockApi(page, MARIA);

    await page.goto('/');
    await expect(card(page, 'Генерация текстов')).toBeVisible();

    await expect(page.getByRole('heading', { name: 'Анализ вредоносного кода' })).toHaveCount(0);
  });

  test('авторы заявок эксперту не показываются', async ({ page }) => {
    await mockApi(page, MARIA);

    await page.goto('/');
    await expect(card(page, 'Генерация текстов')).toBeVisible();

    await expect(page.getByText(/Участник:/)).toHaveCount(0);
    await expect(page.getByText(IVAN.fullName)).toHaveCount(0);
    await expect(page.getByText(IVAN.email)).toHaveCount(0);
    await expect(page.getByText(PETR.fullName)).toHaveCount(0);
  });

  test('эксперт может раскрыть содержание заявки и прочитать его', async ({ page }) => {
    await mockApi(page, MARIA);
    await page.goto('/');

    const application = card(page, 'Генерация текстов');
    await application.getByText('Содержание', { exact: true }).click();

    await expect(application.getByText('Содержание: Генерация текстов')).toBeVisible();
  });

  test('у эксперта нет кнопок для изменения заявок и формы подачи', async ({ page }) => {
    await mockApi(page, MARIA);

    await page.goto('/');
    await expect(card(page, 'Генерация текстов')).toBeVisible();

    await expect(card(page, 'Генерация текстов').getByRole('button')).toHaveCount(0);
    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Подтвердить' })).toHaveCount(0);
  });

  test('если подтверждённых заявок нет, показано сообщение', async ({ page }) => {
    await mockApi(page, { user: MARIA, applications: [] });

    await page.goto('/');

    await expect(page.getByText('Подтверждённых заявок нет')).toBeVisible();
  });

  test('эксперт, не закреплённый за направлением, видит сообщение об отказе', async ({ page }) => {
    await mockApi(page, EXPERT_WITHOUT_DIRECTION);

    await page.goto('/');

    await expect(page.getByText('Expert is not assigned to a direction')).toBeVisible();
  });
});
