import { ROLES } from '@/constants/auth'

const shopRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/ShopLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/shop/HomeView.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'products',
        name: 'products',
        component: () => import('@/views/shop/ProductListView.vue'),
        meta: { title: '商品列表' },
      },
      {
        path: 'products/:id',
        name: 'product-detail',
        component: () => import('@/views/shop/ProductDetailView.vue'),
        meta: { title: '商品详情' },
      },
      {
        path: 'news',
        name: 'news',
        component: () => import('@/views/shop/NewsListView.vue'),
        meta: { title: '三农资讯' },
      },
      {
        path: 'news/:id',
        name: 'news-detail',
        component: () => import('@/views/shop/NewsDetailView.vue'),
        meta: { title: '资讯详情' },
      },
      {
        path: 'cart',
        name: 'cart',
        component: () => import('@/views/shop/CartView.vue'),
        meta: {
          title: '购物车',
          requiresAuth: true,
          roles: [ROLES.CUSTOMER, ROLES.FARMER],
        },
      },
      {
        path: 'checkout',
        name: 'checkout',
        component: () => import('@/views/shop/CheckoutView.vue'),
        meta: {
          title: '确认订单',
          requiresAuth: true,
          roles: [ROLES.CUSTOMER, ROLES.FARMER],
        },
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/shop/OrdersView.vue'),
        meta: {
          title: '我的订单',
          requiresAuth: true,
          roles: [ROLES.CUSTOMER, ROLES.FARMER],
        },
      },
      {
        path: 'orders/:id',
        name: 'order-detail',
        component: () => import('@/views/shop/OrderDetailView.vue'),
        meta: {
          title: '订单详情',
          requiresAuth: true,
          roles: [ROLES.CUSTOMER, ROLES.FARMER],
        },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/shop/ProfileView.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true,
          roles: [ROLES.CUSTOMER, ROLES.FARMER],
        },
      },
      {
        path: 'merchant',
        name: 'merchant-dashboard',
        component: () => import('@/views/shop/MerchantDashboardView.vue'),
        meta: {
          title: '商户后台',
          requiresAuth: true,
          roles: [ROLES.FARMER],
        },
      },
      {
        path: 'merchant/verify',
        name: 'merchant-verify',
        component: () => import('@/views/shop/MerchantVerifyView.vue'),
        meta: {
          title: '农户认证',
          requiresAuth: true,
          roles: [ROLES.FARMER],
        },
      },
      {
        path: 'merchant/products',
        name: 'merchant-products',
        component: () => import('@/views/shop/MerchantProductsView.vue'),
        meta: {
          title: '商品管理',
          requiresAuth: true,
          roles: [ROLES.FARMER],
        },
      },
      {
        path: 'merchant/news',
        name: 'merchant-news',
        component: () => import('@/views/shop/MerchantNewsView.vue'),
        meta: {
          title: '资讯管理',
          requiresAuth: true,
          roles: [ROLES.FARMER],
        },
      },
    ],
  },
]

export default shopRoutes
