import { expect, type Page } from '@playwright/test';

export const PARTICIPANT_HOME_HEADING = 'Новая заявка';

export async function openMyApplications(page: Page): Promise<void> {
  const tab = page.getByRole('tab', { name: 'Мои заявки', exact: true });
  await tab.click();
  await expect(tab).toHaveAttribute('aria-selected', 'true');
}

export async function openApplicationForm(page: Page): Promise<void> {
  const tab = page.getByRole('tab', { name: /^(Подать заявку|Редактирование заявки)$/ });
  await tab.click();
  await expect(tab).toHaveAttribute('aria-selected', 'true');
}
