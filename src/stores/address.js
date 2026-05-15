import { defineStore } from 'pinia'
import {
  createAddressApi,
  getAddressesApi,
  removeAddressApi,
  updateAddressApi,
} from '@/api/modules/addresses'
import { mockAddresses } from '@/mocks/addresses'

const ADDRESS_STORAGE_KEY = 'ZHHS_ADDRESS_ITEMS'

function readStorage() {
  const value = localStorage.getItem(ADDRESS_STORAGE_KEY)
  if (!value) {
    return structuredClone(mockAddresses)
  }

  try {
    return JSON.parse(value)
  } catch {
    return structuredClone(mockAddresses)
  }
}

function writeStorage(addresses) {
  localStorage.setItem(ADDRESS_STORAGE_KEY, JSON.stringify(addresses))
}

export const useAddressStore = defineStore('addresses', {
  state: () => ({
    addresses: readStorage(),
    loading: false,
    error: '',
  }),
  getters: {
    count: (state) => state.addresses.length,
    defaultAddress: (state) =>
      state.addresses.find((item) => item.isDefault) || state.addresses[0] || null,
  },
  actions: {
    persist() {
      writeStorage(this.addresses)
    },
    normalizeDefault(id) {
      this.addresses = this.addresses.map((item) => ({
        ...item,
        isDefault: item.id === id,
      }))
    },
    async hydrate() {
      this.loading = true
      this.error = ''

      try {
        const result = await getAddressesApi()
        this.addresses = result.items || result.list || result.data || []
        this.persist()
      } catch (error) {
        this.addresses = readStorage()
        this.error = error.message || '地址接口不可用，已使用本地数据。'
      } finally {
        this.loading = false
      }
    },
    async saveAddress(payload) {
      this.error = ''
      const isEdit = Boolean(payload.id)

      try {
        let saved
        if (isEdit) {
          saved = await updateAddressApi(payload.id, payload)
        } else {
          saved = await createAddressApi(payload)
        }
        if (isEdit) {
          this.addresses = this.addresses.map((item) => (item.id === payload.id ? saved : item))
        } else {
          this.addresses.unshift(saved)
        }

        if (saved?.isDefault) {
          this.normalizeDefault(saved.id)
        }

        this.persist()
        return saved
      } catch (error) {
        this.error = error.message || '保存地址失败，请稍后重试。'
        throw error
      }
    },
    async removeAddress(id) {
      this.error = ''

      try {
        await removeAddressApi(id)
        const removed = this.addresses.find((item) => item.id === id)
        this.addresses = this.addresses.filter((item) => item.id !== id)

        if (removed?.isDefault && this.addresses[0]) {
          this.normalizeDefault(this.addresses[0].id)
        }

        this.persist()
      } catch (error) {
        this.error = error.message || '删除地址失败，请稍后重试。'
        throw error
      }
    },
    async setDefault(id) {
      this.error = ''
      const target = this.addresses.find((item) => item.id === id)
      if (!target) {
        return
      }

      try {
        const saved = await updateAddressApi(id, {
          receiver: target.receiver,
          phone: target.phone,
          address: target.address,
          isDefault: true,
        })
        this.addresses = this.addresses.map((item) => (item.id === id ? saved : item))
        this.normalizeDefault(id)
        this.persist()
      } catch (error) {
        this.error = error.message || '设置默认地址失败，请稍后重试。'
        throw error
      }
    },
  },
})
