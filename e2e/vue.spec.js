import { test, expect } from '@playwright/test'

test('visits the app root url', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'Marketplace Home' })).toBeVisible()
})

test('redirects guest to login when visiting cart and returns after sign in', async ({ page }) => {
  await page.goto('/cart')
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible()

  await page.getByPlaceholder('11-digit phone').fill('13800000000')
  await page.getByPlaceholder('Password').fill('12345678')
  await page.getByRole('button', { name: 'Sign In' }).click()

  await expect(page).toHaveURL(/\/cart$/)
  await expect(page.getByRole('heading', { name: 'Cart' })).toBeVisible()
})

test('allows seeded admin session to open admin dashboard', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('AUTH_TOKEN', 'playwright-admin-token')
    localStorage.setItem(
      'AUTH_USER',
      JSON.stringify({
        id: 1,
        name: 'Playwright Admin',
        role: 'admin',
      }),
    )
  })

  await page.route('**/api/auth/profile', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        user: {
          id: 1,
          name: 'Playwright Admin',
          role: 'admin',
        },
      }),
    })
  })

  await page.goto('/admin')
  await expect(page.getByRole('heading', { name: 'Admin Dashboard' })).toBeVisible()
  await expect(page.getByText('ZHHS Admin')).toBeVisible()
})
