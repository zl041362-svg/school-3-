import { defineStore } from 'pinia'
import {
  addCartItemApi,
  getCartApi,
  removeCartItemApi,
  updateCartItemApi,
  clearCartApi,
} from '@/api/modules/cart'
import { mockCartItems } from '@/mocks/transaction'
import { resolveItems } from '@/utils/apiResponse'
import { normalizeQty } from '@/utils/quantity'
import { readJsonStorage, writeJsonStorage } from '@/utils/storage'
import { allowMockFallback } from '@/utils/mockControl'

const CART_STORAGE_KEY = 'ZHHS_CART_ITEMS'

function readStorage() {
  return readJsonStorage(CART_STORAGE_KEY, allowMockFallback() ? mockCartItems : [])
}

function writeStorage(items) {
  writeJsonStorage(CART_STORAGE_KEY, items)
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
        this.items = resolveItems(result)
        this.persist()
      } catch (error) {
        if (allowMockFallback()) {
          this.items = readStorage()
          this.error = error.message || '购物车接口不可用，已使用本地数据。'
        } else {
          this.error = error.message || '购物车加载失败'
          throw error
        }
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
    async clear() {
      try {
        await clearCartApi()
      } catch {
        // ignore - clear local regardless
      }
      this.items = []
      this.persist()
    },
  },
})
