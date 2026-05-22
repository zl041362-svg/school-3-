import http from '@/api/http'

export function getProductsApi(params) {
  return http.get('/products', { params })
}

export function getProductDetailApi(id) {
  return http.get(`/products/${id}`)
}

export function getProductEvaluationsApi(id, params) {
  return http.get(`/products/${id}/evaluations`, { params })
}

export function canReviewProductApi(id) {
  return http.get(`/products/${id}/can-review`)
}

export function createProductEvaluationApi(id, payload) {
  return http.post(`/products/${id}/evaluations`, payload)
}

export function deleteProductEvaluationApi(id) {
  return http.delete(`/products/evaluations/${id}`)
}

export function getMyEvaluationsApi(params) {
  return http.get('/products/evaluations/my', { params })
}
