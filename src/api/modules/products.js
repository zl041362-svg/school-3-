import http from '@/api/http'

export function getProductsApi(params) {
  return http.get('/products', { params })
}

export function getProductDetailApi(id) {
  return http.get(`/products/${id}`)
}
