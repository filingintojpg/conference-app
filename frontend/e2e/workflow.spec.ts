import { expect, test } from '@playwright/test';
import {
  ANNA,
  CYBER_DIRECTION,
  IVAN,
  MARIA,
  OLEG,
  answerConfirm,
  card,
  loginAs,
  logout,
  makeApplication,
  mockApi,
  selectField,
} from './helpers/mockApi';

test.describe('путь заявки через все роли', () => {
  test('участник подаёт заявку, организатор подтверждает, эксперт её видит', async ({ page }) => {
    await mockApi(page, { user: null, applications: [] });

    await loginAs(page, IVAN);
    await selectField(page, 'Направление').selectOption({ value: '1' });
    await page.getByLabel('Название', { exact: true }).fill('Доклад про нейросети');
    await page.getByLabel('Аннотация', { exact: true }).fill('Коротко о докладе.');
    await page.getByLabel('Содержание', { exact: true }).fill('Подробности доклада.');
    await page.getByRole('button', { name: 'Подать заявку' }).click();
    await expect(card(page, 'Доклад про нейросети').getByText('Ожидает проверки')).toBeVisible();
    await logout(page);

    await loginAs(page, MARIA);
    await expect(page.getByText('Подтверждённых заявок нет')).toBeVisible();
    await logout(page);

    await loginAs(page, ANNA);
    await card(page, 'Доклад про нейросети').getByRole('button', { name: 'Подтвердить' }).click();
    await expect(page.getByText('Заявка прошла проверку')).toBeVisible();
    await logout(page);

    await loginAs(page, MARIA);
    await expect(card(page, 'Доклад про нейросети')).toBeVisible();
    await expect(page.getByText(IVAN.fullName)).toHaveCount(0);
    await logout(page);

    await loginAs(page, IVAN);
    await expect(card(page, 'Доклад про нейросети').getByText('Прошла проверку')).toBeVisible();
  });

  test('отклонённая организатором заявка эксперту не показывается', async ({ page }) => {
    await mockApi(page, { user: null, applications: [makeApplication({ id: 1, title: 'Слабый доклад' })] });

    await loginAs(page, ANNA);
    await card(page, 'Слабый доклад').getByRole('button', { name: 'Отклонить' }).click();
    await expect(page.getByText('Заявка не прошла проверку')).toBeVisible();
    await logout(page);

    await loginAs(page, MARIA);
    await expect(page.getByText('Подтверждённых заявок нет')).toBeVisible();
    await logout(page);

    await loginAs(page, IVAN);
    await expect(card(page, 'Слабый доклад').getByText('Не прошла проверку')).toBeVisible();
    await expect(card(page, 'Слабый доклад').getByRole('button', { name: 'Редактировать' })).toBeDisabled();
  });

  test('заявка, отозванная участником, пропадает у эксперта', async ({ page }) => {
    await mockApi(page, {
      user: null,
      applications: [makeApplication({ id: 1, title: 'Передумал участвовать', reviewStatus: 'APPROVED' })],
    });

    await loginAs(page, MARIA);
    await expect(card(page, 'Передумал участвовать')).toBeVisible();
    await logout(page);

    await loginAs(page, IVAN);
    answerConfirm(page, true);
    await card(page, 'Передумал участвовать').getByRole('button', { name: 'Отозвать' }).click();
    await expect(page.getByText('Заявка отозвана')).toBeVisible();
    await logout(page);

    await loginAs(page, MARIA);
    await expect(page.getByText('Подтверждённых заявок нет')).toBeVisible();
  });

  test('передача заявки: один организатор передаёт, другой принимает, участник видит новое направление', async ({ page }) => {
    await mockApi(page, { user: null, applications: [makeApplication({ id: 1, title: 'Не в то направление' })] });

    await loginAs(page, ANNA);
    await card(page, 'Не в то направление').getByRole('button', { name: 'Передать на другое направление' }).click();
    await selectField(page, 'Передать на направление').selectOption({ value: '2' });
    await page.getByRole('button', { name: 'Передать', exact: true }).click();
    await expect(page.getByText('Передача ожидает решения')).toBeVisible();
    await logout(page);

    await loginAs(page, OLEG);
    await card(page, 'Не в то направление').getByRole('button', { name: 'Принять на данное направление' }).click();
    await expect(page.getByText('Заявка принята на данное направление')).toBeVisible();
    await logout(page);

    await loginAs(page, IVAN);
    const application = card(page, 'Не в то направление');
    await expect(application.getByText(`Направление: ${CYBER_DIRECTION}`)).toBeVisible();
    await expect(application.getByText('Передана с направления «Искусственный интеллект»')).toBeVisible();
    await logout(page);

    await loginAs(page, ANNA);
    await expect(page.getByText('Заявок с такими параметрами нет')).toBeVisible();
  });

  test('если организатор-получатель отверг передачу, заявка остаётся у прежнего организатора', async ({ page }) => {
    await mockApi(page, { user: null, applications: [makeApplication({ id: 1, title: 'Спорная заявка' })] });

    await loginAs(page, ANNA);
    await card(page, 'Спорная заявка').getByRole('button', { name: 'Передать на другое направление' }).click();
    await selectField(page, 'Передать на направление').selectOption({ value: '2' });
    await page.getByRole('button', { name: 'Передать', exact: true }).click();
    await expect(page.getByText('Передача ожидает решения')).toBeVisible();
    await logout(page);

    await loginAs(page, OLEG);
    await card(page, 'Спорная заявка').getByRole('button', { name: 'Отвергнуть' }).click();
    await expect(page.getByText('Передача заявки отвергнута, заявка осталась на прежнем направлении')).toBeVisible();
    await logout(page);

    await loginAs(page, ANNA);
    const application = card(page, 'Спорная заявка');
    await expect(application.getByRole('button', { name: 'Подтвердить' })).toBeVisible();
    await expect(application.getByText('Передача ожидает решения')).toHaveCount(0);
  });
});
