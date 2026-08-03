import axios from 'axios'

export type ApiError = Error & { status?: number; code?: string; traceId?: string; fieldErrors?: Record<string, string> }

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 20_000
})

client.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('evidence.access-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  config.headers['X-Request-Id'] = crypto.randomUUID()
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const payload = error.response?.data
    const normalized = new Error(payload?.message || error.message || '网络请求失败') as ApiError
    normalized.status = error.response?.status
    normalized.code = payload?.code
    normalized.traceId = payload?.traceId
    normalized.fieldErrors = payload?.fieldErrors
    return Promise.reject(normalized)
  }
)

export const requestIdempotencyKey = (prefix = 'web') => `${prefix}-${crypto.randomUUID()}`
export default client
