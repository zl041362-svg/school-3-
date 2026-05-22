import http from '@/api/http'

export function getNewsListApi(params) {
  return http.get('/news', { params })
}

export function getNewsDetailApi(id) {
  return http.get(`/news/${id}`)
}

export function toggleFavoriteApi(id) {
  return http.post(`/news/${id}/favorite`)
}

export function getFavoriteStatusApi(id) {
  return http.get(`/news/${id}/favorited`)
}

export function getFavoritesApi(params) {
  return http.get('/news/favorites', { params })
}
