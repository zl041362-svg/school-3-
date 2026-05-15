import http from '@/api/http'

export function getNewsListApi(params) {
  return http.get('/news', { params })
}

export function getNewsDetailApi(id) {
  return http.get(`/news/${id}`)
}
