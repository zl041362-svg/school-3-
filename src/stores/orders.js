import { defineStore } from 'pinia'
import { createOrderApi, getOrderDetailApi, getOrdersApi } from '@/api/modules/orders'
import { mockOrders } from '@/mocks/transaction'

const ORDER_STORAGE_KEY = 'ZHHS_ORDER_ITEMS'

function readStorage() {
  const value = localStorage.getItem(ORDER_STORAGE_KEY)
  if (!value) {
    return structuredClone(mockOrders)
  }

  try {
    return JSON.parse(value)
  } catch {
    return structuredClone(mockOrders)
  }
}

function writeStorage(orders) {
  localStorage.setItem(ORDER_STORAGE_KEY, JSON.stringify(orders))
}

export const useOrderStore = defineStore('orders', {
  state: () => ({
    orders: readStorage(),
    loading: false,
    error: '',
  }),
  actions: {
    persist() {
      writeStorage(this.orders)
    },
    async hydrate() {
      this.loading = true
      this.error = ''

      try {
        const result = await getOrdersApi({ page: 1, pageSize: 20 })
        this.orders = result.items || result.list || result.data || []
        this.persist()
      } catch (error) {
        this.orders = readStorage()
        this.error = error.message || '订单接口不可用，已使用本地数据。'
      } finally {
        this.loading = false
      }
    },
    async createOrder(payload) {
      this.error = ''

      try {
        const result = await createOrderApi(payload)
        const order = result.order || result.data || result
        this.orders.unshift(order)
        this.persist()
        return order
      } catch (error) {
        this.error = error.message || '订单提交失败，请稍后重试。'
        throw error
      }
    },
    async getDetail(id) {
      this.error = ''

      try {
        const result = await getOrderDetailApi(id)
        return result.order || result.data || result
      } catch (error) {
        this.error = error?.message || '订单详情加载失败'
        throw error
      }
    },
  },
})
