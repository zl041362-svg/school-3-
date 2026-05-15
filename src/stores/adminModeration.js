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

const ADMIN_STORAGE_KEY = 'ZHHS_ADMIN_MODERATION'

function createDefaultState() {
  return {
    farmerVerifications: structuredClone(mockFarmerVerifications),
    productReviews: structuredClone(mockProductReviews),
    newsReviews: structuredClone(mockNewsReviews),
    products: structuredClone(mockAdminProducts),
    news: structuredClone(mockAdminNews),
    users: structuredClone(mockAdminUsers),
    roles: structuredClone(mockAdminRoles),
    permissions: structuredClone(mockAdminPermissions),
    logs: structuredClone(mockAdminLogs),
  }
}

function readStorage() {
  const value = localStorage.getItem(ADMIN_STORAGE_KEY)
  if (!value) {
    return createDefaultState()
  }

  try {
    return { ...createDefaultState(), ...JSON.parse(value) }
  } catch {
    return createDefaultState()
  }
}

function writeStorage(payload) {
  localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(payload))
}

function resolveList(result, fallback) {
  return result.items || result.list || result.data || fallback
}

function resolvePagination(result) {
  return {
    total: result.total || 0,
    page: result.page || 1,
    pageSize: result.pageSize || 20,
  }
}

function createSectionFallback(section) {
  return createDefaultState()[section]
}

export const useAdminModerationStore = defineStore('adminModeration', {
  state: () => ({
    ...readStorage(),
    loadingMap: {
      farmerVerifications: false,
      productReviews: false,
      newsReviews: false,
      products: false,
      news: false,
      users: false,
      roles: false,
      permissions: false,
      logs: false,
    },
    error: '',
    paginationMap: {
      farmerVerifications: { total: 0, page: 1, pageSize: 20 },
      productReviews: { total: 0, page: 1, pageSize: 20 },
      newsReviews: { total: 0, page: 1, pageSize: 20 },
      products: { total: 0, page: 1, pageSize: 20 },
      news: { total: 0, page: 1, pageSize: 20 },
      users: { total: 0, page: 1, pageSize: 20 },
      roles: { total: 0, page: 1, pageSize: 20 },
      permissions: { total: 0, page: 1, pageSize: 20 },
      logs: { total: 0, page: 1, pageSize: 20 },
    },
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
      writeStorage({
        farmerVerifications: this.farmerVerifications,
        productReviews: this.productReviews,
        newsReviews: this.newsReviews,
        products: this.products,
        news: this.news,
        users: this.users,
        roles: this.roles,
        permissions: this.permissions,
        logs: this.logs,
      })
    },
    async hydrateSection(section, { page = 1, pageSize = 20 } = {}) {
      this.loadingMap[section] = true
      this.error = ''

      const params = { page, pageSize }

      try {
        if (section === 'farmerVerifications') {
          const result = await getFarmerVerificationsApi(params)
          this.farmerVerifications = resolveList(result, mockFarmerVerifications)
          this.paginationMap.farmerVerifications = resolvePagination(result)
        }

        if (section === 'productReviews') {
          const result = await getProductReviewsApi(params)
          this.productReviews = resolveList(result, mockProductReviews)
          this.paginationMap.productReviews = resolvePagination(result)
        }

        if (section === 'newsReviews') {
          const result = await getNewsReviewsApi(params)
          this.newsReviews = resolveList(result, mockNewsReviews)
          this.paginationMap.newsReviews = resolvePagination(result)
        }

        if (section === 'products') {
          const result = await getAdminProductsApi(params)
          this.products = resolveList(result, mockAdminProducts)
          this.paginationMap.products = resolvePagination(result)
        }

        if (section === 'news') {
          const result = await getAdminNewsApi(params)
          this.news = resolveList(result, mockAdminNews)
          this.paginationMap.news = resolvePagination(result)
        }

        if (section === 'users') {
          const result = await getAdminUsersApi(params)
          this.users = resolveList(result, mockAdminUsers)
          this.paginationMap.users = resolvePagination(result)
        }

        if (section === 'roles') {
          const result = await getAdminRolesApi(params)
          this.roles = resolveList(result, mockAdminRoles)
          this.paginationMap.roles = resolvePagination(result)
        }

        if (section === 'permissions') {
          const result = await getAdminPermissionsApi(params)
          this.permissions = resolveList(result, mockAdminPermissions)
          this.paginationMap.permissions = resolvePagination(result)
        }

        if (section === 'logs') {
          const result = await getAdminLogsApi(params)
          this.logs = resolveList(result, mockAdminLogs)
          this.paginationMap.logs = resolvePagination(result)
        }

        this.persist()
      } catch (error) {
        const fallback = readStorage()[section] || createSectionFallback(section)
        this[section] = fallback
        this.error = error.message || '管理端接口不可用，已使用本地数据。'
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
