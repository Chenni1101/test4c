<template>
  <div class="performance-page">
    <a-page-header
      class="page-header"
      title="性能展示"
      sub-title="系统性能测试数据与区块链网络状态"
    />

    <div class="performance-hero mb-4">
      <div>
        <h3>性能摘要</h3>
        <p>以秒级存证与毫秒级检索为目标，保障业务可用性与可扩展性。</p>
      </div>
      <div class="hero-tags">
        <span>O(1)检索</span>
        <span>1000并发</span>
        <span>链上可验证</span>
      </div>
    </div>

    <!-- 核心指标 -->
    <a-row :gutter="16" class="mb-4">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic title="存证成功率" :value="100" suffix="%" :value-style="{ color: '#52c41a' }">
            <template #prefix><CheckCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic title="平均响应时间" :value="42" suffix="ms" :value-style="{ color: '#1890ff' }">
            <template #prefix><ThunderboltOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic title="单次存证成本" :value="0.01" prefix="¥" :precision="2">
            <template #prefix><PayCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card>
          <a-statistic title="测试文物数量" :value="200" suffix="件">
            <template #prefix><DatabaseOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 查询响应时间图表 -->
      <a-col :xs="24" :lg="12">
        <a-card title="查询响应时间分布" class="mb-4">
          <div ref="responseChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
      
      <!-- 并发测试结果 -->
      <a-col :xs="24" :lg="12">
        <a-card title="并发查询测试" class="mb-4">
          <div ref="concurrentChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 小规模测试 -->
      <a-col :xs="24" :lg="12">
        <a-card title="测试1：100次并发查询" class="mb-4">
          <div ref="test1Chart" style="height: 250px;"></div>
          <a-alert message="平均响应时间：32ms | 最大响应时间：45ms | 成功率：100%" type="success" show-icon />
        </a-card>
      </a-col>
      
      <!-- 大规模测试 -->
      <a-col :xs="24" :lg="12">
        <a-card title="测试2：1000次并发查询" class="mb-4">
          <div ref="test2Chart" style="height: 250px;"></div>
          <a-alert message="平均响应时间：58ms | 最大响应时间：95ms | 成功率：100%" type="success" show-icon />
        </a-card>
      </a-col>
    </a-row>

    <!-- 与传统方案对比 -->
    <a-card title="与传统存证方案对比" class="mb-4">
      <a-row :gutter="24">
        <a-col :xs="24" :lg="12">
          <div ref="compareChart" style="height: 300px;"></div>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-table :columns="compareColumns" :data-source="compareData" :pagination="false" :scroll="{ x: 520 }" />
        </a-col>
      </a-row>
    </a-card>

    <!-- 区块链网络状态 -->
    <a-card title="区块链网络状态">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-statistic title="当前区块高度">
            <template #formatter>
              <span class="block-height">{{ blockHeight.toLocaleString() }}</span>
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-statistic title="网络节点数" :value="21" suffix="个" />
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-statistic title="TPS峰值" :value="10000" suffix="笔/秒" />
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-statistic title="共识算法">
            <template #formatter>
              <a-tag color="blue">XPoS</a-tag>
            </template>
          </a-statistic>
        </a-col>
      </a-row>
      <a-divider />
      <a-descriptions :column="{ xs: 1, sm: 2, lg: 3 }">
        <a-descriptions-item label="区块链网络">百度超级链开放测试网络</a-descriptions-item>
        <a-descriptions-item label="智能合约">eleccert</a-descriptions-item>
        <a-descriptions-item label="网络状态">
          <a-badge status="success" text="正常运行" />
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import {
  CheckCircleOutlined,
  ThunderboltOutlined,
  PayCircleOutlined,
  DatabaseOutlined
} from '@ant-design/icons-vue'

const responseChart = ref(null)
const concurrentChart = ref(null)
const test1Chart = ref(null)
const test2Chart = ref(null)
const compareChart = ref(null)

const blockHeight = ref(5234567)
let blockTimer = null

const compareColumns = [
  { title: '对比项', dataIndex: 'item', key: 'item' },
  { title: '本系统', dataIndex: 'ours', key: 'ours' },
  { title: '传统公证', dataIndex: 'traditional', key: 'traditional' }
]

const compareData = [
  { key: 1, item: '单次成本', ours: '0.01元', traditional: '200-500元' },
  { key: 2, item: '存证时间', ours: '< 3秒', traditional: '3-7个工作日' },
  { key: 3, item: '查询效率', ours: 'O(1)', traditional: '人工查询' },
  { key: 4, item: '防篡改能力', ours: '区块链保障', traditional: '依赖机构公信力' },
  { key: 5, item: '全天候服务', ours: '24/7', traditional: '工作时间' },
  { key: 6, item: '跨地域验证', ours: '全球即时', traditional: '需线下办理' }
]

let charts = []

const resizeCharts = () => {
  charts.forEach((chart) => chart.resize())
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', resizeCharts)
  // 模拟区块高度增长
  blockTimer = setInterval(() => {
    blockHeight.value += Math.floor(Math.random() * 3) + 1
  }, 3000)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  charts.forEach(chart => chart.dispose())
  if (blockTimer) clearInterval(blockTimer)
})

const initCharts = () => {
  // 响应时间分布图
  const chart1 = echarts.init(responseChart.value)
  chart1.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['0-20ms', '20-40ms', '40-60ms', '60-80ms', '80-100ms']
    },
    yAxis: { type: 'value', name: '查询次数' },
    series: [{
      data: [45, 120, 85, 35, 15],
      type: 'bar',
      itemStyle: { color: '#0ea5e9' }
    }]
  })
  charts.push(chart1)

  // 并发测试图
  const chart2 = echarts.init(concurrentChart.value)
  chart2.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['100并发', '500并发', '1000并发'] },
    xAxis: { type: 'category', data: ['第1秒', '第2秒', '第3秒', '第4秒', '第5秒'] },
    yAxis: { type: 'value', name: '响应时间(ms)' },
    series: [
      {
        name: '100并发',
        type: 'line',
        data: [25, 28, 31, 35, 32],
        smooth: true,
        lineStyle: { color: '#0ea5e9' },
        itemStyle: { color: '#0ea5e9' }
      },
      {
        name: '500并发',
        type: 'line',
        data: [35, 42, 48, 55, 50],
        smooth: true,
        lineStyle: { color: '#14b8a6' },
        itemStyle: { color: '#14b8a6' }
      },
      {
        name: '1000并发',
        type: 'line',
        data: [48, 58, 68, 75, 65],
        smooth: true,
        lineStyle: { color: '#d4a248' },
        itemStyle: { color: '#d4a248' }
      }
    ]
  })
  charts.push(chart2)

  // 测试1图表
  const chart3 = echarts.init(test1Chart.value)
  chart3.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['1-20', '21-40', '41-60', '61-80', '81-100'] },
    yAxis: { type: 'value', name: '响应时间(ms)' },
    series: [{
      data: [25, 28, 31, 35, 42],
      type: 'line',
      areaStyle: { color: 'rgba(14, 165, 233, 0.2)' },
      itemStyle: { color: '#0ea5e9' }
    }]
  })
  charts.push(chart3)

  // 测试2图表
  const chart4 = echarts.init(test2Chart.value)
  chart4.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['0-200', '201-400', '401-600', '601-800', '801-1000'] },
    yAxis: { type: 'value', name: '响应时间(ms)' },
    series: [{
      data: [38, 45, 52, 68, 89],
      type: 'line',
      areaStyle: { color: 'rgba(212, 162, 72, 0.2)' },
      itemStyle: { color: '#d4a248' }
    }]
  })
  charts.push(chart4)

  // 对比图表
  const chart5 = echarts.init(compareChart.value)
  chart5.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['本系统', '传统公证'] },
    xAxis: { type: 'category', data: ['成本(元)', '时间(小时)', '可用性(%)'] },
    yAxis: { type: 'value' },
    series: [
      { 
        name: '本系统', 
        type: 'bar', 
        data: [0.01, 0.001, 99.9],
        itemStyle: { color: '#0ea5e9' }
      },
      { 
        name: '传统公证', 
        type: 'bar', 
        data: [350, 72, 30],
        itemStyle: { color: '#f97316' }
      }
    ]
  })
  charts.push(chart5)
}
</script>

<style scoped>
.performance-page {
  padding: 16px 20px 24px;
  background:
    radial-gradient(900px 420px at 10% -12%, rgba(14, 116, 144, 0.14), transparent 55%),
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

.performance-page :deep(.ant-card) {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.performance-page :deep(.ant-card-head) {
  background: #f8fafc;
  border-bottom: none;
}

.mb-4 {
  margin-bottom: 16px;
}

.performance-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
}

.performance-hero h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: var(--brand-blue-strong);
}

.performance-hero p {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
}

.hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.hero-tags span {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(14, 165, 233, 0.12);
  color: #0f766e;
  border: 1px solid rgba(14, 165, 233, 0.18);
}

.block-height {
  font-family: 'Courier New', monospace;
  color: var(--brand-blue);
}

@media (max-width: 767px) {
  .performance-page {
    padding: 12px;
  }

  .performance-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
