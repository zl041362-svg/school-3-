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

export function updateAdminProductApi(id, payload) {
  return http.put(`/admin/products/${id}`, payload)
}

export function updateAdminProductStatusApi(id, payload) {
  return http.patch(`/admin/products/${id}/status`, payload)
}

export function getAdminNewsApi(params) {
  return http.get('/admin/news', { params })
}

export function updateAdminNewsApi(id, payload) {
  return http.put(`/admin/news/${id}`, payload)
}

export function updateAdminNewsStatusApi(id, payload) {
  return http.patch(`/admin/news/${id}/status`, payload)
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

export function createAdminPermissionApi(payload) {
  return http.post('/admin/permissions', payload)
}

export function updateAdminPermissionApi(id, payload) {
  return http.put(`/admin/permissions/${id}`, payload)
}

export function deleteAdminPermissionApi(id) {
  return http.delete(`/admin/permissions/${id}`)
}

export function createAdminRoleApi(payload) {
  return http.post('/admin/roles', payload)
}

export function deleteAdminRoleApi(id) {
  return http.delete(`/admin/roles/${id}`)
}

export function editAdminRoleApi(id, payload) {
  return http.put(`/admin/roles/${id}`, payload)
}

export function batchReviewProductsApi(payload) {
  return http.post('/admin/product-reviews/batch', payload)
}

export function batchReviewNewsApi(payload) {
  return http.post('/admin/news-reviews/batch', payload)
}

export function batchReviewFarmerVerificationsApi(payload) {
  return http.post('/admin/farmer-verifications/batch', payload)
}

export function getAdminLogsApi(params) {
  return http.get('/admin/logs', { params })
}
