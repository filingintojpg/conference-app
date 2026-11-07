import { expect, test } from '@playwright/test';
import {
  ANNA,
  ARCHIVE_DIRECTION,
  CYBER_DIRECTION,
  IVAN,
  OLEG,
  ORGANIZER_WITHOUT_DIRECTIONS,
  PETR,
  card,
  makeApplication,
  mockApi,
  selectField,
} from './helpers/mockApi';

test.describe('организатор: список заявок', () => {
  test('организатор видит заявки своего направления, ожидающие проверки', async ({ page }) => {
    await mockApi(page, ANNA);

    await page.goto('/');

    await expect(page.getByRole('heading', { name: 'Заявки по направлению' })).toBeVisible();
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();
    await expect(card(page, 'Распознавание речи')).toBeVisible();
  });

  test('в списке нет заявок других направлений и уже проверенных заявок', async ({ page }) => {
    await mockApi(page, ANNA);

    await page.goto('/');
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();

    await expect(page.getByRole('heading', { name: 'Защита паролей' })).toHaveCount(0);
    await expect(page.getByRole('heading', { name: 'Генерация текстов' })).toHaveCount(0);
  });

  test('у заявки указан автор: имя и email', async ({ page }) => {
    await mockApi(page, ANNA);

    await page.goto('/');

    const application = card(page, 'Нейросети в медицине');
    await expect(application.getByText(`Участник: ${IVAN.fullName} (${IVAN.email})`)).toBeVisible();
  });

  test('организатор без направлений видит пояснение вместо списка', async ({ page }) => {
    await mockApi(page, ORGANIZER_WITHOUT_DIRECTIONS);

    await page.goto('/');

    await expect(page.getByText('У этого организатора нет направлений')).toBeVisible();
  });

  test('если направление не выбрано вручную, показано первое из своих, а чужих в выборе нет', async ({ page }) => {
    await mockApi(page, OLEG);

    await page.goto('/');

    const select = selectField(page, 'Направление');
    await expect(select).toHaveValue('2');
    await expect(select.getByRole('option')).toHaveText([CYBER_DIRECTION, ARCHIVE_DIRECTION]);
  });

  test('организатор с несколькими направлениями переключается между ними', async ({ page }) => {
    await mockApi(page, OLEG);
    await page.goto('/');
    await expect(card(page, 'Защита паролей')).toBeVisible();

    await selectField(page, 'Направление').selectOption({ label: ARCHIVE_DIRECTION });

    await expect(card(page, 'Старая работа')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Защита паролей' })).toHaveCount(0);
  });
});

test.describe('организатор: фильтры', () => {
  test('можно посмотреть уже подтверждённые заявки', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    await selectField(page, 'Проверка').selectOption({ label: 'Прошла проверку' });

    await expect(card(page, 'Генерация текстов')).toBeVisible();
    await expect(card(page, 'Этика искусственного интеллекта')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Нейросети в медицине' })).toHaveCount(0);
  });

  test('можно показать только отозванные заявки', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    await selectField(page, 'Проверка').selectOption({ label: 'Все' });
    await selectField(page, 'Статус заявки').selectOption({ label: 'Отозвана' });

    await expect(card(page, 'Отозванная работа')).toBeVisible();
    await expect(card(page, 'Снятая с конференции')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Нейросети в медицине' })).toHaveCount(0);
  });

  test('если по фильтрам ничего не нашлось, показано сообщение', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    await selectField(page, 'Проверка').selectOption({ label: 'Не прошла проверку' });

    await expect(page.getByText('Заявок с такими параметрами нет')).toBeVisible();
  });
});

test.describe('организатор: проверка заявок', () => {
  test('организатор подтверждает заявку, и она переходит в «прошедшие проверку»', async ({ page }) => {
    const api = await mockApi(page, ANNA);
    await page.goto('/');

    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Подтвердить' }).click();

    await expect(page.getByText('Заявка прошла проверку')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Нейросети в медицине' })).toHaveCount(0);
    expect(api.applications().find((a) => a.id === 1)?.reviewStatus).toBe('APPROVED');

    await selectField(page, 'Проверка').selectOption({ label: 'Прошла проверку' });
    await expect(card(page, 'Нейросети в медицине').getByText('Прошла проверку')).toBeVisible();
  });

  test('организатор отклоняет заявку, и она переходит в «не прошедшие проверку»', async ({ page }) => {
    const api = await mockApi(page, ANNA);
    await page.goto('/');

    await card(page, 'Распознавание речи').getByRole('button', { name: 'Отклонить' }).click();

    await expect(page.getByText('Заявка не прошла проверку')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Распознавание речи' })).toHaveCount(0);
    expect(api.applications().find((a) => a.id === 4)?.reviewStatus).toBe('REJECTED');

    await selectField(page, 'Проверка').selectOption({ label: 'Не прошла проверку' });
    await expect(card(page, 'Распознавание речи').getByText('Не прошла проверку')).toBeVisible();
  });

  test('у отозванной заявки нет кнопок проверки', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    const withdrawn = card(page, 'Отозванная работа');

    await expect(withdrawn).toBeVisible();
    await expect(withdrawn.getByRole('button')).toHaveCount(0);
  });

  test('у уже проверенной заявки нет кнопок проверки', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    await selectField(page, 'Проверка').selectOption({ label: 'Прошла проверку' });

    await expect(card(page, 'Генерация текстов')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Подтвердить' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Отклонить' })).toHaveCount(0);
  });

  test('если заявку уже успел проверить кто-то другой, организатор видит причину и обновлённый список', async ({ page }) => {
    const api = await mockApi(page, ANNA);
    api.failNext('POST', /\/approve$/, 409, 'Application has already been reviewed');
    await page.goto('/');

    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Подтвердить' }).click();

    await expect(page.getByText('Application has already been reviewed')).toBeVisible();
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();
  });

  test('если время сессии истекло во время работы, организатора возвращают на страницу входа', async ({ page }) => {
    const api = await mockApi(page, ANNA);
    await page.goto('/');
    await expect(card(page, 'Нейросети в медицине')).toBeVisible();

    api.setSession(null);
    await card(page, 'Нейросети в медицине').getByRole('button', { name: 'Подтвердить' }).click();

    await expect(page).toHaveURL(/\/login$/);
  });

  test('у организатора нет формы подачи заявки и кнопок участника', async ({ page }) => {
    await mockApi(page, ANNA);

    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Заявки по направлению' })).toBeVisible();

    await expect(page.getByRole('heading', { name: 'Новая заявка' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Редактировать' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Отозвать' })).toHaveCount(0);
  });
});

test.describe('организатор: передача заявки на другое направление', () => {
  async function startTransfer(page: import('@playwright/test').Page, title: string) {
    await card(page, title).getByRole('button', { name: 'Передать на другое направление' }).click();
  }

  test('организатор передаёт заявку другому направлению, и она ждёт решения', async ({ page }) => {
    const api = await mockApi(page, ANNA);
    await page.goto('/');

    await startTransfer(page, 'Нейросети в медицине');
    await selectField(page, 'Передать на направление').selectOption({ label: `${CYBER_DIRECTION} (${OLEG.fullName})` });
    await page.getByRole('button', { name: 'Передать', exact: true }).click();

    await expect(
      page.getByText(`Заявка передана на направление «${CYBER_DIRECTION}», ожидает решения организатора`),
    ).toBeVisible();
    const waiting = card(page, 'Нейросети в медицине');
    await expect(waiting.getByText('Передача ожидает решения')).toBeVisible();
    await expect(waiting.getByRole('button', { name: 'Подтвердить' })).toHaveCount(0);
    await expect(waiting.getByRole('button', { name: 'Отклонить' })).toHaveCount(0);
    expect(api.applications().find((a) => a.id === 1)?.transferToDirectionId).toBe(2);
  });

  test('в списке для передачи нет текущего направления заявки', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    await startTransfer(page, 'Нейросети в медицине');

    const options = selectField(page, 'Передать на направление').getByRole('option');
    await expect(options).toHaveText([
      'Выберите направление',
      `${CYBER_DIRECTION} (${OLEG.fullName})`,
      `${ARCHIVE_DIRECTION} (${OLEG.fullName})`,
    ]);
  });

  test('пока направление не выбрано, кнопка «Передать» недоступна', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    await startTransfer(page, 'Нейросети в медицине');

    await expect(page.getByRole('button', { name: 'Передать', exact: true })).toBeDisabled();
  });

  test('организатор может передумать и отменить передачу до отправки', async ({ page }) => {
    const api = await mockApi(page, ANNA);
    await page.goto('/');

    await startTransfer(page, 'Нейросети в медицине');
    await page.getByRole('button', { name: 'Отмена' }).click();

    await expect(card(page, 'Нейросети в медицине').getByRole('button', { name: 'Передать на другое направление' })).toBeVisible();
    expect(api.requests.some((r) => r.path.endsWith('/transfer'))).toBe(false);
  });

  test('проверенную заявку передать нельзя: кнопки передачи нет', async ({ page }) => {
    await mockApi(page, ANNA);
    await page.goto('/');

    await selectField(page, 'Проверка').selectOption({ label: 'Прошла проверку' });

    await expect(card(page, 'Генерация текстов')).toBeVisible();
    await expect(page.getByRole('button', { name: /Передать/ })).toHaveCount(0);
  });

  test('организатор-получатель видит входящую заявку и принимает её', async ({ page }) => {
    const incoming = makeApplication({
      id: 50,
      title: 'Заявка из другого направления',
      directionId: 1,
      transferredFromDirectionId: 1,
      transferToDirectionId: 2,
    });
    const api = await mockApi(page, { user: OLEG, applications: [incoming] });
    await page.goto('/');

    const application = card(page, 'Заявка из другого направления');
    await expect(application.getByText(`Передана с направления «Искусственный интеллект» на направление «${CYBER_DIRECTION}», ожидает решения`)).toBeVisible();
    await application.getByRole('button', { name: 'Принять на данное направление' }).click();

    await expect(page.getByText('Заявка принята на данное направление')).toBeVisible();
    await expect(card(page, 'Заявка из другого направления').getByText('Передана с направления «Искусственный интеллект»')).toBeVisible();
    await expect(card(page, 'Заявка из другого направления').getByText('Передача ожидает решения')).toHaveCount(0);
    expect(api.applications().find((a) => a.id === 50)?.directionId).toBe(2);
  });

  test('организатор-получатель может отвергнуть входящую заявку', async ({ page }) => {
    const incoming = makeApplication({
      id: 50,
      title: 'Заявка из другого направления',
      directionId: 1,
      transferredFromDirectionId: 1,
      transferToDirectionId: 2,
    });
    const api = await mockApi(page, { user: OLEG, applications: [incoming] });
    await page.goto('/');

    await card(page, 'Заявка из другого направления').getByRole('button', { name: 'Отвергнуть' }).click();

    await expect(page.getByText('Передача заявки отвергнута, заявка осталась на прежнем направлении')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Заявка из другого направления' })).toHaveCount(0);
    const stored = api.applications().find((a) => a.id === 50);
    expect(stored?.directionId).toBe(1);
    expect(stored?.transferToDirectionId).toBeNull();
  });

  test('отозванную входящую заявку можно только отвергнуть, принять нельзя', async ({ page }) => {
    const incoming = makeApplication({
      id: 50,
      title: 'Отозванная входящая',
      participantId: PETR.id,
      status: 'WITHDRAWN',
      directionId: 1,
      transferredFromDirectionId: 1,
      transferToDirectionId: 2,
    });
    await mockApi(page, { user: OLEG, applications: [incoming] });
    await page.goto('/');

    const application = card(page, 'Отозванная входящая');

    await expect(application.getByRole('button', { name: 'Отвергнуть' })).toBeVisible();
    await expect(application.getByRole('button', { name: 'Принять на данное направление' })).toHaveCount(0);
  });
});
