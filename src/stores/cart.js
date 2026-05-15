import { defineStore } from 'pinia'
import {
  addCartItemApi,
  getCartApi,
  removeCartItemApi,
  updateCartItemApi,
} from '@/api/modules/cart'
import { mockCartItems } from '@/mocks/transaction'

const CART_STORAGE_KEY = 'ZHHS_CART_ITEMS'

function readStorage() {
  const value = localStorage.getItem(CART_STORAGE_KEY)
  if (!value) {
    return structuredClone(mockCartItems)
  }

  try {
    return JSON.parse(value)
  } catch {
    return structuredClone(mockCartItems)
  }
}

function writeStorage(items) {
  localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items))
}

function normalizeQty(value, stock) {
  const parsed = Number.parseInt(value, 10)
  if (!Number.isFinite(parsed) || parsed < 1) {
    return 1
  }

  if (typeof stock === 'number' && stock > 0) {
    return Math.min(parsed, stock)
  }

  return parsed
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: readStorage(),
    loading: false,
    error: '',
  }),
  getters: {
    itemCount: (state) => state.items.reduce((sum, item) => sum + item.qty, 0),
    totalAmount: (state) => state.items.reduce((sum, item) => sum + item.qty * item.price, 0),
  },
  actions: {
    persist() {
      writeStorage(this.items)
    },
    async hydrate() {
      this.loading = true
      this.error = ''

      try {
        const result = await getCartApi()
        this.items = result.items || result.list || result.data || []
        this.persist()
      } catch (error) {
        this.items = readStorage()
        this.error = error.message || '购物车接口不可用，已使用本地数据。'
      } finally {
        this.loading = false
      }
    },
    async addItem(product, qty = 1) {
      this.error = ''
      const normalizedQty = normalizeQty(qty, product.stock)

      try {
        const saved = await addCartItemApi({ productId: product.id, qty: normalizedQty })
        const existing = this.items.find((item) => item.id === saved.id || item.productId === saved.productId)
        if (existing) {
          Object.assign(existing, saved)
        } else {
          this.items.unshift(saved)
        }
        this.persist()
        return saved
      } catch (error) {
        this.error = error.message || '加入购物车失败，请稍后重试。'
        throw error
      }
    },
    async updateQty(id, qty) {
      this.error = ''
      const target = this.items.find((item) => item.id === id)
      if (!target) {
        return
      }

      const normalizedQty = normalizeQty(qty, target.stock)

      try {
        const saved = await updateCartItemApi(id, { qty: normalizedQty })
        Object.assign(target, saved)
        this.persist()
        return saved
      } catch (error) {
        this.error = error.message || '更新购物车失败，请稍后重试。'
        throw error
      }
    },
    async removeItem(id) {
      this.error = ''

      try {
        await removeCartItemApi(id)
        this.items = this.items.filter((item) => item.id !== id)
        this.persist()
      } catch (error) {
        this.error = error.message || '移除购物车商品失败，请稍后重试。'
        throw error
      }
    },
    clear() {
      this.items = []
      this.persist()
    },
  },
})
