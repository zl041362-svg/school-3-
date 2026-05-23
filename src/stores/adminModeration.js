import { defineStore } from 'pinia'
import {
  getAdminNewsApi,
  getAdminLogsApi,
  getAdminPermissionsApi,
  getAdminProductsApi,
  getAdminRolesApi,
  getAdminUsersApi,
  getFarmerVerificationsApi,
  getNewsReviewsApi,
  getProductReviewsApi,
  reviewFarmerVerificationApi,
  reviewNewsApi,
  reviewProductApi,
  updateAdminRoleApi,
  updateAdminUserApi,
} from '@/api/modules/admin'
import {
  mockAdminNews,
  mockAdminLogs,
  mockAdminPermissions,
  mockAdminProducts,
  mockAdminRoles,
  mockAdminUsers,
  mockFarmerVerifications,
  mockNewsReviews,
  mockProductReviews,
} from '@/mocks/admin'
import { resolveItems, resolvePagination } from '@/utils/apiResponse'
import { readJsonStorage, writeJsonStorage } from '@/utils/storage'
import { allowMockFallback } from '@/utils/mockControl'

const ADMIN_STORAGE_KEY = 'ZHHS_ADMIN_MODERATION'
const SECTION_KEYS = [
  'farmerVerifications',
  'productReviews',
  'newsReviews',
  'products',
  'news',
  'users',
  'roles',
  'permissions',
  'logs',
]

const SECTION_CONFIG = {
  farmerVerifications: { fetch: getFarmerVerificationsApi, fallback: mockFarmerVerifications },
  productReviews: { fetch: getProductReviewsApi, fallback: mockProductReviews },
  newsReviews: { fetch: getNewsReviewsApi, fallback: mockNewsReviews },
  products: { fetch: getAdminProductsApi, fallback: mockAdminProducts },
  news: { fetch: getAdminNewsApi, fallback: mockAdminNews },
  users: { fetch: getAdminUsersApi, fallback: mockAdminUsers },
  roles: { fetch: getAdminRolesApi, fallback: mockAdminRoles },
  permissions: { fetch: getAdminPermissionsApi, fallback: mockAdminPermissions },
  logs: { fetch: getAdminLogsApi, fallback: mockAdminLogs },
}

const DEFAULT_PAGINATION = { total: 0, page: 1, pageSize: 20 }

function createDefaultState() {
  if (!allowMockFallback()) {
    return Object.fromEntries(SECTION_KEYS.map((section) => [section, []]))
  }
  return Object.fromEntries(
    SECTION_KEYS.map((section) => [section, structuredClone(SECTION_CONFIG[section].fallback)]),
  )
}

function readStorage() {
  return { ...createDefaultState(), ...readJsonStorage(ADMIN_STORAGE_KEY, {}) }
}

function writeStorage(payload) {
  writeJsonStorage(ADMIN_STORAGE_KEY, payload)
}

function createSectionFallback(section) {
  return createDefaultState()[section]
}

function createSectionMap(valueFactory) {
  return Object.fromEntries(SECTION_KEYS.map((section) => [section, valueFactory()]))
}

export const useAdminModerationStore = defineStore('adminModeration', {
  state: () => ({
    ...readStorage(),
    loadingMap: createSectionMap(() => false),
    error: '',
    paginationMap: createSectionMap(() => ({ ...DEFAULT_PAGINATION })),
  }),
  getters: {
    dashboardCards: (state) => [
      {
        key: 'pendingProducts',
        label: '待审商品',
        value: state.productReviews.filter((item) => item.status === 'pending').length,
        path: '/admin/product-reviews',
      },
      {
        key: 'pendingNews',
        label: '待审资讯',
        value: state.newsReviews.filter((item) => item.status === 'pending').length,
        path: '/admin/news-reviews',
      },
      {
        key: 'pendingFarmers',
        label: '待审农户认证',
        value: state.farmerVerifications.filter((item) => item.status === 'pending').length,
        path: '/admin/farmer-verifications',
      },
      {
        key: 'activeUsers',
        label: '活跃用户',
        value: state.users.filter((item) => item.status === 'active').length,
        path: '/admin/users',
      },
    ],
    pagination: (state) => (section) => state.paginationMap[section] || { total: 0, page: 1, pageSize: 20 },
  },
  actions: {
    persist() {
      writeStorage(Object.fromEntries(SECTION_KEYS.map((section) => [section, this[section]])))
    },
    async hydrateSection(section, { page = 1, pageSize = 20 } = {}) {
      const config = SECTION_CONFIG[section]
      if (!config) {
        return
      }

      this.loadingMap[section] = true
      this.error = ''

      const params = { page, pageSize }

      try {
        const result = await config.fetch(params)
        this[section] = resolveItems(result, allowMockFallback() ? config.fallback : [])
        this.paginationMap[section] = resolvePagination(result)
        this.persist()
      } catch (error) {
        if (allowMockFallback()) {
          const fallback = readStorage()[section] || createSectionFallback(section)
          this[section] = fallback
          this.error = error.message || '管理端接口不可用，已使用本地数据。'
        } else {
          this.error = error.message || '数据加载失败'
          throw error
        }
      } finally {
        this.loadingMap[section] = false
      }
    },
    async reviewFarmerVerification(id, approved, reason = '') {
      this.error = ''
      try {
        await reviewFarmerVerificationApi(id, { approved, reason })
      } catch (error) {
        this.error = error?.message || '审核认证操作失败，已使用本地数据进行更新'
      }

      this.farmerVerifications = this.farmerVerifications.map((item) =>
        item.id === id ? { ...item, status: approved ? 'approved' : 'rejected', reason } : item,
      )
      this.persist()
    },
    async reviewProduct(id, approved, reason = '') {
      this.error = ''
      try {
        await reviewProductApi(id, { approved, reason })
      } catch (error) {
        this.error = error?.message || '商品审核操作失败，已使用本地数据进行更新'
      }

      const reviewed = this.productReviews.find((item) => item.id === id)
      const linkedProductId =
        reviewed?.productId ||
        reviewed?.product_id ||
        this.products.find((item) => item.name === reviewed?.product)?.id ||
        id

      this.productReviews = this.productReviews.map((item) =>
        item.id === id ? { ...item, status: approved ? 'approved' : 'rejected', reason } : item,
      )
      this.products = this.products.map((item) =>
        item.id === linkedProductId ? { ...item, status: approved ? 'published' : 'rejected' } : item,
      )
      this.persist()
    },
    async reviewNews(id, approved, reason = '') {
      this.error = ''
      try {
        await reviewNewsApi(id, { approved, reason })
      } catch (error) {
        this.error = error?.message || '资讯审核操作失败，已使用本地数据进行更新'
      }

      const reviewed = this.newsReviews.find((item) => item.id === id)
      const linkedNewsId =
        reviewed?.newsId ||
        reviewed?.news_id ||
        this.news.find((item) => item.title === reviewed?.title)?.id ||
        id

      this.newsReviews = this.newsReviews.map((item) =>
        item.id === id ? { ...item, status: approved ? 'approved' : 'rejected', reason } : item,
      )
      this.news = this.news.map((item) =>
        item.id === linkedNewsId ? { ...item, status: approved ? 'published' : 'rejected' } : item,
      )
      this.persist()
    },
    async updateUserStatus(id, status) {
      this.error = ''
      try {
        await updateAdminUserApi(id, { status })
      } catch (error) {
        this.error = error?.message || '更新用户状态失败，已使用本地数据进行更新'
      }

      this.users = this.users.map((item) => (item.id === id ? { ...item, status } : item))
      this.logs.unshift({
        id: Date.now(),
        operator: 'admin',
        action: 'update_user_status',
        createdAt: new Date().toLocaleString('zh-CN', { hour12: false }),
        detail: `用户 ${id} 状态更新为 ${status}`,
      })
      this.persist()
    },
    async updateRoleMembers(id, members) {
      this.error = ''
      try {
        await updateAdminRoleApi(id, { members })
      } catch (error) {
        this.error = error?.message || '更新角色成员数失败，已使用本地数据进行更新'
      }

      this.roles = this.roles.map((item) => (item.id === id ? { ...item, members } : item))
      this.persist()
    },
  },
})
