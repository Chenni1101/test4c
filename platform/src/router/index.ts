import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
import HomeView from '../views/Home/index.vue'
import EvidenceView from '../views/Evidence/index.vue'
import PerformanceView from '../views/Performance/index.vue'
import AssetManageView from '../views/AssetManage/index.vue'
import QueryView from '../views/Query/index.vue'
import MarketView from '../views/Market/index.vue'
import AuthView from '../views/AuthView.vue'
import AuthorizationView from '../views/AuthorizationView.vue'
import AssetDetailView from '../views/AssetDetailView.vue'
import TraceView from '../views/TraceView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/evidence',
      name: 'evidence',
      component: EvidenceView,
      meta: { requiresAuth: true, roles: ['CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN'] }
    },
    {
      path: '/asset',
      name: 'asset',
      component: AssetManageView,
      meta: { requiresAuth: true, roles: ['CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN'] }
    },
    {
      path: '/query',
      name: 'query',
      component: QueryView,
      meta: { requiresAuth: true, roles: ['CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN'] }
    },
    { path: '/assets/:assetCode', name: 'asset-detail', component: AssetDetailView, meta: { requiresAuth: true, roles: ['CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN'] } },
    { path: '/authorizations', name: 'authorization', component: AuthorizationView, meta: { requiresAuth: true, roles: ['CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN'] } },
    { path: '/trace', name: 'trace', component: TraceView, meta: { requiresAuth: true, roles: ['CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN'] } },
    {
      path: '/market',
      name: 'market',
      component: MarketView
    },
    {
      path: '/performance',
      name: 'performance',
      component: PerformanceView
    },
    { path: '/login', name: 'login', component: AuthView, meta: { publicOnly: true } },
    { path: '/register', name: 'register', component: AuthView, meta: { publicOnly: true } }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.hydrate()
  if (to.meta.publicOnly && auth.isAuthenticated) return '/'
  if (to.meta.requiresAuth && !auth.isAuthenticated) return { name: 'login', query: { redirect: to.fullPath } }
  const roles = (to.meta.roles as string[] | undefined) || []
  if (roles.length && !auth.hasAnyRole(roles)) return '/'
  return true
})

export default router
