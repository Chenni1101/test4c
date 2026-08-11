import axios from 'axios'

export type ApiError = Error & { status?: number; code?: string; traceId?: string; fieldErrors?: Record<string, string>; isTimeout?: boolean }

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  // Render 免费实例从休眠恢复通常需要约一分钟，20 秒会让首次登录/注册过早失败。
  timeout: 75_000
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
    const isTimeout = error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT'
    const normalized = new Error(
      isTimeout ? '平台服务启动时间较长，请稍候后重试。' : (payload?.message || error.message || '网络请求失败')
    ) as ApiError
    normalized.status = error.response?.status
    normalized.code = isTimeout ? 'REQUEST_TIMEOUT' : payload?.code
    normalized.traceId = payload?.traceId
    normalized.fieldErrors = payload?.fieldErrors
    normalized.isTimeout = isTimeout
    return Promise.reject(normalized)
  }
)

export const requestIdempotencyKey = (prefix = 'web') => `${prefix}-${crypto.randomUUID()}`

/** 在登录、注册前触发一次轻量健康检查，优先完成免费实例的冷启动。 */
export const wakeUpBackend = () => client.get('/health', { timeout: 75_000 })
export default client
