import { ROLES } from '@/constants/auth'

const adminRoutes = [
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: {
      requiresAuth: true,
      roles: [ROLES.ADMIN],
    },
    children: [
      {
        path: '',
        name: 'admin-home',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '控制台' },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/UsersView.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'farmer-verifications',
        name: 'admin-farmer-verifications',
        component: () => import('@/views/admin/FarmerVerificationsView.vue'),
        meta: { title: '农户审核' },
      },
      {
        path: 'products',
        name: 'admin-products',
        component: () => import('@/views/admin/ProductsView.vue'),
        meta: { title: '商品管理' },
      },
      {
        path: 'product-reviews',
        name: 'admin-product-reviews',
        component: () => import('@/views/admin/ProductReviewsView.vue'),
        meta: { title: '商品审核' },
      },
      {
        path: 'news',
        name: 'admin-news',
        component: () => import('@/views/admin/NewsView.vue'),
        meta: { title: '资讯管理' },
      },
      {
        path: 'news-reviews',
        name: 'admin-news-reviews',
        component: () => import('@/views/admin/NewsReviewsView.vue'),
        meta: { title: '资讯审核' },
      },
      {
        path: 'roles',
        name: 'admin-roles',
        component: () => import('@/views/admin/RolesView.vue'),
        meta: { title: '角色管理' },
      },
      {
        path: 'permissions',
        name: 'admin-permissions',
        component: () => import('@/views/admin/PermissionsView.vue'),
        meta: { title: '权限配置' },
      },
      {
        path: 'logs',
        name: 'admin-logs',
        component: () => import('@/views/admin/LogsView.vue'),
        meta: { title: '操作日志' },
      },
    ],
  },
]

export default adminRoutes
