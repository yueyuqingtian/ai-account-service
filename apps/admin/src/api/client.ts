import axios from 'axios'

function resolveBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || ''
}

export const api = axios.create({ baseURL: resolveBaseUrl(), timeout: 30000 })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use((response) => {
  const body = response.data
  if (body.code !== 0) throw new Error(body.message || '请求失败')
  return body
}, (error) => {
  const message = error.response?.data?.message || error.message || '网络请求失败'
  throw new Error(message)
})
