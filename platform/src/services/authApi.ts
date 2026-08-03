import client from './apiClient'

export type AuthUser = {
  id: number
  username: string
  displayName: string
  email?: string
  organization?: string
  status: string
  roles: string[]
}

export const login = (username: string, password: string) => client.post('/auth/login', { username, password })
export const register = (payload: { username: string; password: string; displayName: string; email: string; organization?: string; role?: 'GUEST' | 'CREATOR' }) => client.post('/auth/register', payload)
export const getCurrentUser = () => client.get<AuthUser>('/auth/me')
