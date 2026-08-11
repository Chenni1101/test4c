<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import BorrowRecordModal from '@/components/BorrowRecordModal.vue'
import { getBorrowRecords, getBorrowStatus, returnBorrowRecord, type BorrowDirection, type BorrowRecord, type BorrowStatus } from '@/services/borrowRecords'

const records = ref<BorrowRecord[]>([])
const filter = ref<'ALL' | BorrowDirection | BorrowStatus>('ALL')
const modalOpen = ref(false)
const modalDirection = ref<BorrowDirection>('IN')
const columns = [
  { title: '记录编号', dataIndex: 'recordNo', key: 'recordNo', width: 175 },
  { title: '类型', key: 'direction', width: 88 },
  { title: '藏品名称', dataIndex: 'artifactName', key: 'artifactName', width: 180 },
  { title: '对方机构', dataIndex: 'counterparty', key: 'counterparty', width: 160 },
  { title: '借展用途', dataIndex: 'purpose', key: 'purpose', width: 110 },
  { title: '借展周期', key: 'period', width: 205 },
  { title: '当前状态', key: 'status', width: 100 },
  { title: '操作时间', dataIndex: 'operatedAt', key: 'operatedAt', width: 155 },
  { title: '操作', key: 'action', fixed: 'right', width: 100 }
]
const statusText: Record<BorrowStatus, string> = { ONGOING: '进行中', COMPLETED: '已完成', OVERDUE: '已逾期' }
const statusColor: Record<BorrowStatus, string> = { ONGOING: 'orange', COMPLETED: 'default', OVERDUE: 'red' }
const filteredRecords = computed(() => records.value.filter((record) => filter.value === 'ALL' || record.direction === filter.value || getBorrowStatus(record) === filter.value))
const activeCount = computed(() => records.value.filter((record) => getBorrowStatus(record) === 'ONGOING').length)
const overdueCount = computed(() => records.value.filter((record) => getBorrowStatus(record) === 'OVERDUE').length)

function loadRecords() { records.value = getBorrowRecords() }
function openInbound() { modalDirection.value = 'IN'; modalOpen.value = true }
function onSaved() { loadRecords() }
function returnRecord(record: BorrowRecord) {
  returnBorrowRecord(record.id)
  loadRecords()
  message.success(`已登记归还：${record.artifactName}`)
}
onMounted(loadRecords)
</script>

<template>
  <section class="borrow-page">
    <a-page-header class="page-header" title="藏品借展管理" sub-title="借展台账仅保存在当前浏览器，用于业务演示与现场操作留痕。">
      <template #extra><a-button type="primary" @click="openInbound">登记借入</a-button><a-button @click="loadRecords">刷新记录</a-button></template>
    </a-page-header>
    <a-row :gutter="[16, 16]" class="summary"><a-col :xs="24" :sm="8"><a-card><a-statistic title="借展记录" :value="records.length" suffix="条" /></a-card></a-col><a-col :xs="24" :sm="8"><a-card><a-statistic title="进行中" :value="activeCount" suffix="条" :value-style="{ color: '#d97706' }" /></a-card></a-col><a-col :xs="24" :sm="8"><a-card><a-statistic title="已逾期" :value="overdueCount" suffix="条" :value-style="{ color: overdueCount ? '#dc2626' : '#64748b' }" /></a-card></a-col></a-row>
    <a-card title="借展记录" class="record-card">
      <template #extra><a-radio-group v-model:value="filter" button-style="solid" class="filters"><a-radio-button value="ALL">全部</a-radio-button><a-radio-button value="OUT">借出</a-radio-button><a-radio-button value="IN">借入</a-radio-button><a-radio-button value="ONGOING">进行中</a-radio-button><a-radio-button value="COMPLETED">已完成</a-radio-button></a-radio-group></template>
      <a-empty v-if="!filteredRecords.length" description="没有符合条件的借展记录" />
      <a-table v-else :columns="columns" :data-source="filteredRecords" :pagination="{ pageSize: 8 }" :scroll="{ x: 1320 }" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'recordNo'"><a-typography-text code copyable>{{ record.recordNo }}</a-typography-text></template>
          <template v-else-if="column.key === 'direction'"><a-tag :color="record.direction === 'OUT' ? 'blue' : 'green'">{{ record.direction === 'OUT' ? '借出' : '借入' }}</a-tag></template>
          <template v-else-if="column.key === 'period'"><div>{{ record.startDate }} 至 {{ record.endDate }}</div><small v-if="record.returnedAt" class="returned">归还：{{ record.returnedAt }}</small></template>
          <template v-else-if="column.key === 'status'"><a-tag :color="statusColor[getBorrowStatus(record)]">{{ statusText[getBorrowStatus(record)] }}</a-tag></template>
          <template v-else-if="column.key === 'action'"><a-button v-if="getBorrowStatus(record) !== 'COMPLETED'" size="small" @click="returnRecord(record)">归还</a-button><span v-else class="muted">已归还</span></template>
        </template>
      </a-table>
    </a-card>
    <BorrowRecordModal v-model:open="modalOpen" :direction="modalDirection" @saved="onSaved" />
  </section>
</template>

<style scoped>
.borrow-page { padding: 16px 20px 24px; min-height: 100%; background: linear-gradient(180deg, #f7fbff, #f5f7f9); }
.page-header, .record-card { border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 14px 28px rgba(15, 23, 42, .08); }
.page-header, .summary { margin-bottom: 16px; }
.returned, .muted { color: #64748b; }
.filters { max-width: 100%; overflow-x: auto; white-space: nowrap; }
@media (max-width: 767px) { .borrow-page { padding: 12px; } .filters { margin-top: 8px; } }
</style>
