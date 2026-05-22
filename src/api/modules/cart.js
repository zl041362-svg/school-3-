import http from '@/api/http'

export function getCartApi() {
  return http.get('/cart')
}

export function addCartItemApi(payload) {
  return http.post('/cart/items', payload)
}

export function updateCartItemApi(id, payload) {
  return http.put(`/cart/items/${id}`, payload)
}

export function removeCartItemApi(id) {
  return http.delete(`/cart/items/${id}`)
}

export function clearCartApi() {
  return http.delete('/cart')
}
