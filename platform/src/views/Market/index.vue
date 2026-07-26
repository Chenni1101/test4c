<template>
  <div class="market-page">
    <a-page-header
      class="page-header"
      title="市场分析中枢"
      sub-title="AI 驱动的场景洞察与运营建议"
    />

    <div class="market-kpis">
      <div class="market-kpi" v-for="kpi in marketKpis" :key="kpi.label">
        <span>{{ kpi.label }}</span>
        <strong>{{ kpi.value }}</strong>
        <small>{{ kpi.note }}</small>
      </div>
    </div>
    <div class="market-panel">
      <MarketAnalysisPanel :assets="marketAssets" show-dynamic-rights />
    </div>
  </div>
</template>

<script setup>
import MarketAnalysisPanel from '../../components/MarketAnalysisPanel.vue'

const marketAssets = [
  { id: 1, name: '新艺术葡萄少女花瓶', certified: true, type: 'image' },
  { id: 2, name: '韦奇伍德蓝陶双耳瓶', certified: true, type: '3d' },
  { id: 3, name: '彩绘巴洛克执壶', certified: true, type: 'video' },
  { id: 4, name: '绿地粉彩中国风茶具', certified: true, type: 'document' },
  { id: 5, name: '白瓷北极熊雕塑', certified: true, type: 'image' },
  { id: 6, name: '粉彩花卉茶壶套组', certified: true, type: 'video' }
]

const marketKpis = [
  {
    label: '分析资产',
    value: `200件`,
    note: '覆盖核心馆藏样本'
  },
  {
    label: '场景维度',
    value: '12类',
    note: '检索/授权/交易多场景'
  },
  {
    label: '洞察输出',
    value: 'AI + 规则',
    note: '双引擎生成建议'
  }
]
</script>

<style scoped>
.market-page {
  padding: 16px 20px 24px;
  background:
    radial-gradient(900px 420px at 12% -12%, rgba(14, 116, 144, 0.14), transparent 55%),
    linear-gradient(180deg, #f7fbff 0%, #f5f7f9 100%);
}

.page-header {
  margin-bottom: 16px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 70%, #fff7ed 100%);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.page-header :deep(.ant-page-header-heading-title) {
  font-family: var(--font-serif);
  color: var(--brand-blue-strong);
}

.page-header :deep(.ant-page-header-heading-sub-title) {
  color: var(--text-muted);
}

.market-kpis {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.market-kpi {
  padding: 16px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 14px 26px rgba(15, 23, 42, 0.08);
  display: grid;
  gap: 6px;
}

.market-kpi span {
  font-size: 12px;
  color: var(--text-muted);
}

.market-kpi strong {
  font-size: 20px;
  color: var(--brand-blue-strong);
}

.market-kpi small {
  font-size: 11px;
  color: var(--text-subtle);
}

.market-panel {
  margin-top: 16px;
}

@media (max-width: 991px) {
  .market-kpis {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .market-page {
    padding: 12px;
  }
}
</style>
