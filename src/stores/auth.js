import { defineStore } from 'pinia'
import { AUTH_TOKEN_KEY, AUTH_USER_KEY, ROLES } from '@/constants/auth'
import { getProfileApi, loginApi, registerApi } from '@/api/modules/auth'

function readJson(key) {
  const value = localStorage.getItem(key)
  if (!value) {
    return null
  }

  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(AUTH_TOKEN_KEY) || '',
    user: readJson(AUTH_USER_KEY),
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
      localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
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
        localStorage.setItem(AUTH_USER_KEY, JSON.stringify(this.user))
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
