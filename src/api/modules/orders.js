import http from '@/api/http'

export function getOrdersApi(params) {
  return http.get('/orders', { params })
}

export function getOrderDetailApi(id) {
  return http.get(`/orders/${id}`)
}

export function createOrderApi(payload) {
  return http.post('/orders', payload)
}

export function confirmReceiptApi(id) {
  return http.post(`/orders/${id}/confirm-receipt`)
}
