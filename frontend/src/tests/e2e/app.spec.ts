import { test, expect } from '@playwright/test';

test.describe('ResuMatch ATS System E2E Suite', () => {
  test('should render landing page correctly', async ({ page }) => {
    await page.goto('http://localhost:5173/');
    await expect(page).toHaveTitle(/ResuMatch/i);
    await expect(page.locator('h1')).toContainText('Smart ATS Resume Evaluation System');
  });

  test('should navigate to login page', async ({ page }) => {
    await page.goto('http://localhost:5173/login');
    await expect(page.locator('h2')).toContainText('Welcome Back');
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
  });

  test('should navigate to dashboard routes', async ({ page }) => {
    await page.goto('http://localhost:5173/dashboard');
    await expect(page.locator('body')).toBeVisible();
  });
});
