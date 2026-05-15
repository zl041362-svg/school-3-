import http from '@/api/http'

export const submitVerificationApi = (payload) => http.post('/merchant/verify', payload)

export const getMerchantDashboardApi = () => http.get('/merchant/dashboard')

export const getMerchantProductsApi = (params) => http.get('/merchant/products', { params })

export const createMerchantProductApi = (payload) => http.post('/merchant/products', payload)

export const updateMerchantProductApi = (id, payload) => http.put(`/merchant/products/${id}`, payload)

export const deleteMerchantProductApi = (id) => http.delete(`/merchant/products/${id}`)

export const getMerchantNewsApi = (params) => http.get('/merchant/news', { params })

export const createMerchantNewsApi = (payload) => http.post('/merchant/news', payload)

export const updateMerchantNewsApi = (id, payload) => http.put(`/merchant/news/${id}`, payload)

export const deleteMerchantNewsApi = (id) => http.delete(`/merchant/news/${id}`)
