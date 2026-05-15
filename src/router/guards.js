import NProgress from 'nprogress'
import { ROLE_HOME_MAP } from '@/constants/auth'
import { useAuthStore } from '@/stores/auth'

NProgress.configure({ showSpinner: false })

function canAccessRole(allowedRoles, currentRole) {
  if (!allowedRoles || allowedRoles.length === 0) {
    return true
  }
  return allowedRoles.includes(currentRole)
}

export function registerRouterGuards(router) {
  router.beforeEach(async (to) => {
    NProgress.start()

    const authStore = useAuthStore()
    await authStore.hydrate()

    const requiresAuth = Boolean(to.meta?.requiresAuth)
    const guestOnly = Boolean(to.meta?.guestOnly)
    const allowedRoles = to.meta?.roles || []

    if (requiresAuth && !authStore.isAuthenticated) {
      return {
        path: '/auth/login',
        query: { redirect: to.fullPath },
      }
    }

    if (guestOnly && authStore.isAuthenticated) {
      const homePath = ROLE_HOME_MAP[authStore.role] || '/'
      return homePath
    }

    if (!canAccessRole(allowedRoles, authStore.role)) {
      return '/403'
    }

    return true
  })

  router.afterEach((to) => {
    const title = to.meta?.title ? `${to.meta.title} | 智慧三农` : '智慧三农'
    document.title = title
    NProgress.done()
  })
}
