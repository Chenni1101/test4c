<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { wakeUpBackend, type ApiError } from '@/services/apiClient'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isRegister = ref(route.path === '/register')
const connecting = ref(true)
const connectionError = ref('')
const form = reactive({ username: '', password: '', displayName: '', email: '', organization: '' })
const usernameRules = [
  { required: true, message: '请输入用户名' },
  { pattern: /^[a-zA-Z0-9_.-]{3,64}$/, message: '用户名为 3-64 位字母、数字、下划线、点或横线' }
]
const passwordRules = computed(() => isRegister.value
  ? [{ required: true, message: '请输入密码' }, { min: 12, message: '注册密码至少 12 位' }, { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: '密码必须同时包含字母和数字' }]
  : [{ required: true, message: '请输入密码' }])
const displayNameRules = [{ required: true, message: '请输入显示名称' }, { max: 128, message: '显示名称不能超过 128 个字符' }]
const emailRules = [{ required: true, message: '请输入邮箱' }, { type: 'email', message: '请输入正确的邮箱地址' }]

async function submit() {
  try {
    if (isRegister.value) {
      await auth.signUp({ ...form, organization: form.organization || undefined, role: 'CREATOR' })
      message.success('注册成功，请登录')
      isRegister.value = false
      return
    }
    await auth.signIn(form.username, form.password)
    message.success('登录成功')
    await router.replace((route.query.redirect as string) || '/')
  } catch (error: any) {
    const apiError = error as ApiError
    if (apiError.isTimeout) {
      message.error('请求等待超时。请先确认下方服务连接成功后，再重新提交；注册不要连续重复点击。')
    } else {
      message.error(apiError.message || '请求失败')
    }
  }
}

async function connectBackend() {
  connecting.value = true
  connectionError.value = ''
  try {
    await wakeUpBackend()
  } catch (error: any) {
    connectionError.value = (error as ApiError).isTimeout
      ? '服务启动超过预期，请稍后点击“重新连接”。'
      : '暂时无法连接平台服务，请检查网络后重试。'
  } finally {
    connecting.value = false
  }
}

onMounted(connectBackend)

function submitFailed() {
  message.warning('请按提示补全并检查表单信息')
}

async function switchMode() {
  isRegister.value = !isRegister.value
  await router.replace(isRegister.value ? '/register' : '/login')
}
</script>

<template>
  <main class="auth-page">
    <a-card class="auth-card" :title="isRegister ? '注册创作者账户' : '登录平台'">
      <a-alert
        v-if="connecting || connectionError"
        class="mb"
        :type="connectionError ? 'warning' : 'info'"
        show-icon
        :message="connectionError || '正在连接平台服务，首次访问可能需要约一分钟，请不要重复提交。'"
      >
        <template v-if="connectionError" #action><a-button size="small" @click="connectBackend">重新连接</a-button></template>
      </a-alert>
      <a-alert v-if="isRegister" class="mb" type="info" show-icon message="注册默认创建 CREATOR 角色；管理员角色由平台超级管理员分配。" />
      <a-form :model="form" layout="vertical" @finish="submit" @finish-failed="submitFailed">
        <a-form-item label="用户名" name="username" :rules="usernameRules"><a-input v-model:value="form.username" autocomplete="username" placeholder="3-64 位字母、数字或 ._-" /></a-form-item>
        <a-form-item v-if="isRegister" label="显示名称" name="displayName" :rules="displayNameRules"><a-input v-model:value="form.displayName" /></a-form-item>
        <a-form-item v-if="isRegister" label="邮箱" name="email" :rules="emailRules"><a-input v-model:value="form.email" type="email" /></a-form-item>
        <a-form-item v-if="isRegister" label="所属机构"><a-input v-model:value="form.organization" /></a-form-item>
        <a-form-item label="密码" name="password" :rules="passwordRules"><a-input-password v-model:value="form.password" :autocomplete="isRegister ? 'new-password' : 'current-password'" :placeholder="isRegister ? '至少 12 位，且包含字母和数字' : '请输入密码'" /></a-form-item>
        <a-button type="primary" html-type="submit" block :loading="auth.loading || connecting" :disabled="Boolean(connectionError)">{{ isRegister ? '注册' : '登录' }}</a-button>
      </a-form>
      <a-button type="link" block @click="switchMode">{{ isRegister ? '已有账户？去登录' : '没有账户？注册创作者' }}</a-button>
    </a-card>
  </main>
</template>

<style scoped>
.auth-page { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: radial-gradient(circle at top left, #d9f3ff, transparent 44%), #f7fafc; }
.auth-card { width: min(100%, 420px); border-radius: 16px; box-shadow: 0 16px 40px rgba(15, 23, 42, .14); }
.mb { margin-bottom: 16px; }
</style>
