<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeOutlined,
  SafetyCertificateOutlined,
  CloudUploadOutlined,
  SearchOutlined,
  BarChartOutlined,
  BlockOutlined,
  RobotOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(false)
const isMobile = ref(false)
const route = useRoute()
const router = useRouter()

const selectedKeys = computed(() => [route.name as string])

const menuItems = [
  {
    key: 'home',
    icon: HomeOutlined,
    label: '首页',
    title: '首页'
  },
  {
    key: 'evidence',
    icon: SafetyCertificateOutlined,
    label: '版权存证',
    title: '版权存证'
  },
  {
    key: 'asset',
    icon: CloudUploadOutlined,
    label: '资产管理',
    title: '资产管理'
  },
  {
    key: 'query',
    icon: SearchOutlined,
    label: '存证查询',
    title: '存证查询'
  },
  {
    key: 'market',
    icon: RobotOutlined,
    label: '市场分析',
    title: '市场分析'
  },
  {
    key: 'performance',
    icon: BarChartOutlined,
    label: '性能展示',
    title: '性能展示'
  }
]

const mainOffset = computed(() => {
  if (isMobile.value) return 0
  return collapsed.value ? 80 : 200
})

const updateIsMobile = () => {
  const mobile = window.innerWidth < 768
  isMobile.value = mobile
  if (mobile) {
    collapsed.value = true
  }
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
})

const handleMenuClick = ({ key }: { key: string }) => {
  router.push({ name: key })
  if (isMobile.value) {
    collapsed.value = true
  }
}
</script>

<template>
  <a-layout class="app-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :collapsed-width="isMobile ? 0 : 80"
      :width="200"
      class="app-sider"
      :class="{ 'app-sider-mobile': isMobile }"
    >
      <div class="logo">
        <BlockOutlined class="logo-icon" />
        <div v-show="!collapsed" class="logo-text">
          <span>星图共链</span>
        </div>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-menu-item v-for="item in menuItems" :key="item.key">
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <div v-if="isMobile && !collapsed" class="sider-mask" @click="collapsed = true"></div>

    <a-layout class="main-layout" :style="{ marginLeft: `${mainOffset}px` }">
      <a-layout-header class="app-header">
        <div class="header-left">
          <a-button type="text" @click="collapsed = !collapsed">
            <template #icon>
              <menu-unfold-outlined v-if="collapsed" />
              <menu-fold-outlined v-else />
            </template>
          </a-button>
          <div class="header-title-group">
            <span class="header-title">文旅数字资产版权存证平台</span>
          </div>
        </div>
        <div v-if="!isMobile" class="header-right">
          <a-tag color="green" class="status-tag">测试网络已连接</a-tag>
          <a-tag color="gold" class="status-tag status-gold">稳定运行</a-tag>
        </div>
      </a-layout-header>
      <a-layout-content class="app-content" :class="{ 'app-content-mobile': isMobile }">
        <RouterView />
      </a-layout-content>
      <a-layout-footer class="app-footer">
        文旅数字资产可信存证系统 ©2026 上海对外经贸大学
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script lang="ts">
import { MenuUnfoldOutlined, MenuFoldOutlined } from '@ant-design/icons-vue'
export default {
  components: { MenuUnfoldOutlined, MenuFoldOutlined }
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: var(--surface-2);
}

.main-layout {
  transition: margin-left 0.2s;
  background: transparent;
}

.app-sider {
  overflow: auto;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
  background: linear-gradient(180deg, #0b1f2a 0%, #0b4f6c 60%, #0f172a 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.12);
}

.app-sider-mobile {
  z-index: 20;
}

.sider-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 15;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  margin: 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  gap: 10px;
}

.logo-icon {
  font-size: 26px;
  color: var(--brand-gold);
  filter: drop-shadow(0 6px 12px rgba(212, 162, 72, 0.35));
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

.logo-text span {
  font-size: 15px;
  letter-spacing: 0.6px;
}

.logo-text small {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.65);
}

.app-sider :deep(.ant-menu-dark) {
  background: transparent;
}

.app-sider :deep(.ant-menu-item) {
  margin: 4px 10px;
  border-radius: 10px;
}

.app-sider :deep(.ant-menu-item-selected) {
  background: rgba(212, 162, 72, 0.2);
  color: #fff;
}

.app-sider :deep(.ant-menu-item-selected::after) {
  border-right: 3px solid var(--brand-gold);
}

.app-sider :deep(.ant-menu-item:hover) {
  background: rgba(14, 165, 233, 0.15);
}

.app-header {
  background: linear-gradient(90deg, #ffffff 0%, #f8fafc 100%);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.header-title-group {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--brand-blue-strong);
  font-family: var(--font-serif);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-subtitle {
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 0.4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-tag {
  border-radius: 999px;
  padding: 2px 10px;
  font-weight: 600;
}

.status-gold {
  color: #92400e;
  border-color: rgba(212, 162, 72, 0.6);
  background: rgba(212, 162, 72, 0.18);
}

.app-content {
  margin: 20px;
  padding: 0;
  background: transparent;
  min-height: calc(100vh - 64px - 70px - 48px - 48px);
  border-radius: 16px;
}

.app-content-mobile {
  margin: 12px;
  padding: 0;
  border-radius: 12px;
}

.app-footer {
  text-align: center;
  color: rgba(15, 23, 42, 0.5);
  letter-spacing: 0.4px;
}

@media (max-width: 767px) {
  .app-header {
    padding: 0 12px;
  }

  .header-left {
    gap: 8px;
  }

  .header-title {
      font-size: 14px;
  }

    .header-subtitle {
      display: none;
    }

  .app-footer {
    padding: 12px;
    font-size: 12px;
    line-height: 1.5;
  }
}
</style>
