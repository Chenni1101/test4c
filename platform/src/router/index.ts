import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
import HomeView from '../views/Home/index.vue'
import EvidenceView from '../views/Evidence/index.vue'
import PerformanceView from '../views/Performance/index.vue'
import AssetManageView from '../views/AssetManage/index.vue'
import QueryView from '../views/Query/index.vue'
import MarketView from '../views/Market/index.vue'
import AuthorizationView from '../views/AuthorizationView.vue'
import AssetDetailView from '../views/AssetDetailView.vue'
import TraceView from '../views/TraceView.vue'
import BorrowManagementView from '../views/BorrowManagementView.vue'

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
      component: EvidenceView
    },
    {
      path: '/asset',
      name: 'asset',
      component: AssetManageView
    },
    {
      path: '/query',
      name: 'query',
      component: QueryView
    },
    { path: '/assets/:assetCode', name: 'asset-detail', component: AssetDetailView },
    { path: '/authorizations', name: 'authorization', component: AuthorizationView },
    { path: '/trace', name: 'trace', component: TraceView },
    { path: '/borrow', name: 'borrow', component: BorrowManagementView },
    {
      path: '/market',
      name: 'market',
      component: MarketView
    },
    {
      path: '/performance',
      name: 'performance',
      component: PerformanceView
    }
  ]
})

export default router
