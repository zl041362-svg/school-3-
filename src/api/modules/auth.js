import http from '@/api/http'

export function loginApi(payload) {
  return http.post('/auth/login', payload)
}

export function registerApi(payload) {
  return http.post('/auth/register', payload)
}

export function getProfileApi() {
  return http.get('/auth/profile')
}
