import { beforeEach, describe, it, expect, vi } from 'vitest'

vi.mock('@/api/modules/auth', () => ({
  getProfileApi: vi.fn(async () => ({
    user: {
      id: 1,
      name: '管理员',
      role: 'admin',
    },
  })),
  loginApi: vi.fn(async (payload) => ({
    token: 'mock-token',
    user: {
      id: 2,
      name: '模拟用户',
      role: payload.role || 'customer',
    },
  })),
  registerApi: vi.fn(async () => ({ success: true })),
}))

import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import router from '../router'
import App from '../App.vue'

describe('App', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('mounts with router layout', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)

    const wrapper = mount(App, {
      global: {
        plugins: [pinia, router, ElementPlus],
      },
    })

    await router.isReady()
    expect(router.currentRoute.value.fullPath).toBe('/')
    expect(wrapper.text()).toContain('智慧三农平台')
  })

  it('redirects guest from cart to login', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)

    await router.push('/cart')

    const wrapper = mount(App, {
      global: {
        plugins: [pinia, router, ElementPlus],
      },
    })

    await router.isReady()
    expect(router.currentRoute.value.fullPath).toContain('/auth/login')
    expect(wrapper.text()).toContain('账号登录')
  })

  it('renders not found page for unknown route', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)

    const wrapper = mount(App, {
      global: {
        plugins: [pinia, router, ElementPlus],
      },
    })

    await router.push('/path-that-does-not-exist')

    await router.isReady()
    expect(router.currentRoute.value.name).toBe('not-found')
    expect(wrapper.text()).toContain('404')
  })
})
