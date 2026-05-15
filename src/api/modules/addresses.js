import http from '@/api/http'

export function getAddressesApi() {
  return http.get('/addresses')
}

export function createAddressApi(payload) {
  return http.post('/addresses', payload)
}

export function updateAddressApi(id, payload) {
  return http.put(`/addresses/${id}`, payload)
}

export function removeAddressApi(id) {
  return http.delete(`/addresses/${id}`)
}
