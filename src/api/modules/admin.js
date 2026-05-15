import http from '@/api/http'

export function getFarmerVerificationsApi(params) {
  return http.get('/admin/farmer-verifications', { params })
}

export function reviewFarmerVerificationApi(id, payload) {
  return http.post(`/admin/farmer-verifications/${id}/review`, payload)
}

export function getProductReviewsApi(params) {
  return http.get('/admin/product-reviews', { params })
}

export function reviewProductApi(id, payload) {
  return http.post(`/admin/product-reviews/${id}/review`, payload)
}

export function getNewsReviewsApi(params) {
  return http.get('/admin/news-reviews', { params })
}

export function reviewNewsApi(id, payload) {
  return http.post(`/admin/news-reviews/${id}/review`, payload)
}

export function getAdminProductsApi(params) {
  return http.get('/admin/products', { params })
}

export function getAdminNewsApi(params) {
  return http.get('/admin/news', { params })
}

export function getAdminUsersApi(params) {
  return http.get('/admin/users', { params })
}

export function updateAdminUserApi(id, payload) {
  return http.patch(`/admin/users/${id}`, payload)
}

export function getAdminRolesApi(params) {
  return http.get('/admin/roles', { params })
}

export function updateAdminRoleApi(id, payload) {
  return http.patch(`/admin/roles/${id}`, payload)
}

export function getAdminPermissionsApi(params) {
  return http.get('/admin/permissions', { params })
}

export function getAdminLogsApi(params) {
  return http.get('/admin/logs', { params })
}
