import { defineStore } from 'pinia'
import { AUTH_TOKEN_KEY, AUTH_USER_KEY, ROLES } from '@/constants/auth'
import { getProfileApi, loginApi, registerApi } from '@/api/modules/auth'
import { readJsonStorage, writeJsonStorage } from '@/utils/storage'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(AUTH_TOKEN_KEY) || '',
    user: readJsonStorage(AUTH_USER_KEY),
    hydrated: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    role: (state) => state.user?.role || ROLES.GUEST,
  },
  actions: {
    setSession(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem(AUTH_TOKEN_KEY, token)
      writeJsonStorage(AUTH_USER_KEY, user)
    },
    clearSession() {
      this.token = ''
      this.user = null
      localStorage.removeItem(AUTH_TOKEN_KEY)
      localStorage.removeItem(AUTH_USER_KEY)
    },
    async hydrate() {
      if (this.hydrated) {
        return
      }

      if (!this.token) {
        this.hydrated = true
        return
      }

      try {
        const profile = await getProfileApi()
        this.user = profile.user || profile
        writeJsonStorage(AUTH_USER_KEY, this.user)
      } catch (error) {
        if (error?.status === 401) {
          this.clearSession()
        }
      } finally {
        this.hydrated = true
      }
    },
    async login(payload) {
      const result = await loginApi(payload)
      this.setSession(result.token, result.user)
      return result
    },
    async register(payload) {
      return registerApi(payload)
    },
    logout() {
      this.clearSession()
    },
  },
})
