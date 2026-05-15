import { createRouter, createWebHistory } from 'vue-router'
import authRoutes from './routes/auth'
import shopRoutes from './routes/shop'
import adminRoutes from './routes/admin'
import { registerRouterGuards } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...authRoutes,
    ...shopRoutes,
    ...adminRoutes,
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('@/views/common/ForbiddenView.vue'),
      meta: { title: '禁止访问' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/common/NotFoundView.vue'),
      meta: { title: '页面不存在' },
    },
  ],
})

registerRouterGuards(router)

export default router
