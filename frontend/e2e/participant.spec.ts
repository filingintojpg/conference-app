import { expect, test } from '@playwright/test';
import { ARCHIVE_DIRECTION, CYBER_DIRECTION, IVAN, PETR, answerConfirm, card, mockApi, selectField } from './helpers/mockApi';
import { openApplicationForm, openMyApplications } from './helpers/participant';

test.describe('участник: вкладки', () => {
  test('по умолчанию открыта форма подачи заявки, список заявок скрыт', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/');

    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Подать заявку' })).toHaveAttribute('aria-selected', 'true');
    await expect(page.getByRole('tab', { name: 'Мои заявки' })).toHaveAttribute('aria-selected', 'false');
    await expect(page.getByRole('article')).toHaveCount(0);
  });

  test('участник переключается между формой и списком своих заявок', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');

    await openMyApplications(page);
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toHaveCount(0);

    await openApplicationForm(page);
    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toBeVisible();
    await expect(page.getByRole('article')).toHaveCount(0);
  });

  test('сообщение об успехе пропадает при переключении вкладок', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');

    await selectField(page, 'Направление').selectOption({ value: '1' });
    await page.getByLabel('Название', { exact: true }).fill('Тема для проверки');
    await page.getByLabel('Аннотация', { exact: true }).fill('Коротко.');
    await page.getByLabel('Содержание', { exact: true }).fill('Подробно.');
    await page.getByRole('button', { name: 'Подать заявку' }).click();
    await expect(page.getByText('Заявка подана')).toBeVisible();

    await openApplicationForm(page);
    await openMyApplications(page);

    await expect(page.getByText('Заявка подана')).toHaveCount(0);
    await expect(card(page, 'Тема для проверки')).toBeVisible();
  });
});

test.describe('участник: свои заявки', () => {
  test('участник видит только свои заявки, чужих нет', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/');
    await openMyApplications(page);

    await expect(card(page, 'Нейросети в медицине')).toBeVisible();
    await expect(card(page, 'Генерация текстов')).toBeVisible();
    await expect(card(page, 'Защита паролей')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Распознавание речи' })).toHaveCount(0);
    await expect(page.getByRole('heading', { name: 'Отозванная работа' })).toHaveCount(0);
  });

  test('у каждой заявки видно направление, статус и результат проверки', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/');
    await openMyApplications(page);

    const waiting = card(page, 'Нейросети в медицине');
    await expect(waiting.getByText('Направление: Искусственный интеллект')).toBeVisible();
    await expect(waiting.getByText('Подана', { exact: true })).toBeVisible();
    await expect(waiting.getByText('Ожидает проверки')).toBeVisible();

    await expect(card(page, 'Генерация текстов').getByText('Прошла проверку')).toBeVisible();
  });

  test('у участника, который ещё ничего не подавал, показана подсказка', async ({ page }) => {
    await mockApi(page, { user: IVAN, applications: [] });

    await page.goto('/');
    await openMyApplications(page);

    await expect(page.getByText('У вас пока нет заявок')).toBeVisible();
  });

  test('другой участник видит свои заявки, а не заявки Ивана', async ({ page }) => {
    await mockApi(page, PETR);

    await page.goto('/');
    await openMyApplications(page);

    await expect(card(page, 'Распознавание речи')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Нейросети в медицине' })).toHaveCount(0);
  });
});

test.describe('участник: подача заявки', () => {
  test('участник подаёт новую заявку, и она появляется в списке', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');

    await selectField(page, 'Направление').selectOption({ value: '2' });
    await page.getByLabel('Название', { exact: true }).fill('Безопасность умного дома');
    await page.getByLabel('Аннотация', { exact: true }).fill('Как защитить устройства умного дома.');
    await page.getByLabel('Содержание', { exact: true }).fill('Подробное описание доклада.');
    await page.getByRole('button', { name: 'Подать заявку' }).click();

    await expect(page.getByRole('tab', { name: 'Мои заявки' })).toHaveAttribute('aria-selected', 'true');
    await expect(page.getByText('Заявка подана')).toBeVisible();
    const created = card(page, 'Безопасность умного дома');
    await expect(created).toBeVisible();
    await expect(created.getByText(`Направление: ${CYBER_DIRECTION}`)).toBeVisible();
    await expect(created.getByText('Подана', { exact: true })).toBeVisible();
    await expect(created.getByText('Ожидает проверки')).toBeVisible();
  });

  test('после подачи заявки форма очищается', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');

    await selectField(page, 'Направление').selectOption({ value: '1' });
    await page.getByLabel('Название', { exact: true }).fill('Новая тема');
    await page.getByLabel('Аннотация', { exact: true }).fill('Коротко.');
    await page.getByLabel('Содержание', { exact: true }).fill('Подробно.');
    await page.getByRole('button', { name: 'Подать заявку' }).click();
    await expect(page.getByText('Заявка подана')).toBeVisible();

    await openApplicationForm(page);

    await expect(selectField(page, 'Направление')).toHaveValue('');
    await expect(page.getByLabel('Название', { exact: true })).toHaveValue('');
    await expect(page.getByLabel('Аннотация', { exact: true })).toHaveValue('');
    await expect(page.getByLabel('Содержание', { exact: true })).toHaveValue('');
  });

  test('нельзя подать заявку, не заполнив обязательные поля', async ({ page }) => {
    const api = await mockApi(page, IVAN);
    await page.goto('/');

    await page.getByLabel('Название', { exact: true }).fill('Только название');
    await page.getByRole('button', { name: 'Подать заявку' }).click();

    expect(api.requests.some((r) => r.method === 'POST' && r.path === '/applications')).toBe(false);
    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toBeVisible();

    await openMyApplications(page);
    await expect(page.getByRole('heading', { name: 'Только название' })).toHaveCount(0);
  });

  test('направление с закрытым приёмом выбрать нельзя', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');

    const closed = page.getByRole('option', { name: new RegExp(`${ARCHIVE_DIRECTION}.*приём закрыт`) });

    await expect(closed).toBeDisabled();
  });

  test('если сервер отказал, участник видит причину и введённые данные не теряются', async ({ page }) => {
    const api = await mockApi(page, IVAN);
    api.failNext('POST', '/applications', 409, 'Приём заявок на это направление закрыт');
    await page.goto('/');

    await selectField(page, 'Направление').selectOption({ value: '1' });
    await page.getByLabel('Название', { exact: true }).fill('Опоздавшая тема');
    await page.getByLabel('Аннотация', { exact: true }).fill('Коротко.');
    await page.getByLabel('Содержание', { exact: true }).fill('Подробно.');
    await page.getByRole('button', { name: 'Подать заявку' }).click();

    await expect(page.getByText('Приём заявок на это направление закрыт')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toBeVisible();
    await expect(page.getByLabel('Название', { exact: true })).toHaveValue('Опоздавшая тема');
  });
});

test.describe('участник: редактирование', () => {
  test('участник исправляет заявку, которую ещё не проверили', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Редактировать' }).click();
    await expect(page.getByRole('heading', { name: 'Редактирование заявки' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Редактирование заявки' })).toHaveAttribute('aria-selected', 'true');
    await page.getByLabel('Название', { exact: true }).fill('Нейросети в диагностике');
    await page.getByRole('button', { name: 'Сохранить' }).click();

    await expect(page.getByText('Заявка обновлена')).toBeVisible();
    const edited = card(page, 'Нейросети в диагностике');
    await expect(edited.getByText('Изменена', { exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Нейросети в медицине' })).toHaveCount(0);
    await expect(page.getByRole('tab', { name: 'Подать заявку' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Редактирование заявки' })).toHaveCount(0);
  });

  test('при редактировании направление изменить нельзя', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Редактировать' }).click();

    await expect(selectField(page, 'Направление')).toBeDisabled();
  });

  test('отмена редактирования возвращает к списку заявок без изменений', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Редактировать' }).click();
    await page.getByLabel('Название', { exact: true }).fill('Случайная правка');
    await page.getByRole('button', { name: 'Отмена' }).click();

    await expect(page.getByRole('tab', { name: 'Мои заявки' })).toHaveAttribute('aria-selected', 'true');
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Случайная правка' })).toHaveCount(0);

    await openApplicationForm(page);
    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toBeVisible();
    await expect(page.getByLabel('Название', { exact: true })).toHaveValue('');
  });

  test('заявку, уже проверенную организатором, редактировать нельзя - только отозвать', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    const checked = card(page, 'Генерация текстов');

    await expect(checked.getByRole('button', { name: 'Редактировать' })).toBeDisabled();
    await expect(checked.getByRole('button', { name: 'Отозвать' })).toBeEnabled();
  });

  test('если приём заявок закрыт, заявку нельзя ни изменить, ни отозвать', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    const old = card(page, 'Старая работа');

    await expect(old.getByRole('button', { name: 'Редактировать' })).toBeDisabled();
    await expect(old.getByRole('button', { name: 'Отозвать' })).toBeDisabled();
  });
});

test.describe('участник: отзыв заявки', () => {
  test('участник отзывает заявку, и она помечается как отозванная', async ({ page }) => {
    const api = await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    answerConfirm(page, true);
    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Отозвать' }).click();

    await expect(page.getByText('Заявка отозвана')).toBeVisible();
    const withdrawn = card(page, 'Нейросети в медицине');
    await expect(withdrawn.getByText('Отозвана', { exact: true })).toBeVisible();
    await expect(withdrawn.getByRole('button', { name: 'Отозвать' })).toBeDisabled();
    await expect(withdrawn.getByRole('button', { name: 'Редактировать' })).toBeDisabled();
    expect(api.applications().find((a) => a.id === 1)?.status).toBe('WITHDRAWN');
  });

  test('перед отзывом спрашивают подтверждение с названием заявки', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    let question = '';
    page.once('dialog', async (dialog) => {
      question = dialog.message();
      await dialog.dismiss();
    });
    await card(page, 'Защита паролей').getByRole('button', { name: 'Отозвать' }).click();

    await expect.poll(() => question).toBe('Отозвать заявку «Защита паролей»?');
  });

  test('если участник передумал отзывать, заявка остаётся как была', async ({ page }) => {
    const api = await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    answerConfirm(page, false);
    await card(page, 'Защита паролей').getByRole('button', { name: 'Отозвать' }).click();

    await expect(card(page, 'Защита паролей').getByText('Подана', { exact: true })).toBeVisible();
    await expect(page.getByText('Заявка отозвана')).toHaveCount(0);
    expect(api.requests.some((r) => r.path.endsWith('/withdraw'))).toBe(false);
  });

  test('если отозвать не удалось, участник видит причину', async ({ page }) => {
    const api = await mockApi(page, IVAN);
    api.failNext('POST', /\/withdraw$/, 409, 'Application is already withdrawn');
    await page.goto('/');
    await openMyApplications(page);

    answerConfirm(page, true);
    await card(page, 'Защита паролей').getByRole('button', { name: 'Отозвать' }).click();

    await expect(page.getByText('Application is already withdrawn')).toBeVisible();
  });

  test('участник может отозвать заявку, которую начал редактировать', async ({ page }) => {
    await mockApi(page, IVAN);
    await page.goto('/');
    await openMyApplications(page);

    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Редактировать' }).click();
    await expect(page.getByRole('heading', { name: 'Редактирование заявки' })).toBeVisible();

    await openMyApplications(page);
    answerConfirm(page, true);
    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Отозвать' }).click();

    await expect(page.getByText('Заявка отозвана')).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Редактирование заявки' })).toHaveCount(0);

    await openApplicationForm(page);
    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toBeVisible();
    await expect(page.getByLabel('Название', { exact: true })).toHaveValue('');
  });
});

test.describe('участник: чего на экране нет', () => {
  test('у участника нет кнопок организатора', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/');
    await openMyApplications(page);
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();

    await expect(page.getByRole('button', { name: 'Подтвердить' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Отклонить' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: /Передать/ })).toHaveCount(0);
  });

  test('участник не видит данных других участников', async ({ page }) => {
    await mockApi(page, IVAN);

    await page.goto('/');
    await openMyApplications(page);
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();

    await expect(page.getByText(PETR.email)).toHaveCount(0);
    await expect(page.getByText(/Участник:/)).toHaveCount(0);
  });
});
