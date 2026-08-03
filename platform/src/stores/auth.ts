import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login, register, type AuthUser } from '@/services/authApi'

const TOKEN_KEY = 'evidence.access-token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(sessionStorage.getItem(TOKEN_KEY) || '')
  const user = ref<AuthUser | null>(null)
  const initialized = ref(false)
  const loading = ref(false)
  const roles = computed(() => user.value?.roles || [])
  const isAuthenticated = computed(() => Boolean(token.value && user.value))

  function saveToken(value: string) {
    token.value = value
    sessionStorage.setItem(TOKEN_KEY, value)
  }

  function logout() {
    token.value = ''
    user.value = null
    sessionStorage.removeItem(TOKEN_KEY)
  }

  async function hydrate() {
    if (initialized.value) return
    initialized.value = true
    if (!token.value) return
    try {
      user.value = (await getCurrentUser()).data
    } catch {
      logout()
    }
  }

  async function signIn(username: string, password: string) {
    loading.value = true
    try {
      const response = await login(username, password)
      saveToken(response.data.accessToken)
      user.value = (await getCurrentUser()).data
      initialized.value = true
    } finally {
      loading.value = false
    }
  }

  async function signUp(payload: Parameters<typeof register>[0]) {
    loading.value = true
    try {
      await register(payload)
    } finally {
      loading.value = false
    }
  }

  function hasAnyRole(accepted: string[] = []) {
    return accepted.length === 0 || accepted.some((role) => roles.value.includes(role))
  }

  return { token, user, roles, initialized, loading, isAuthenticated, hydrate, signIn, signUp, logout, hasAnyRole }
})
