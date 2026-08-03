<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { listMyAssets, type EvidenceAsset } from '@/services/assetApi'
import { getIpfsFile } from '@/services/assetIssuanceApi'

const loading = ref(false); const loadError = ref(''); const search = ref(''); const assets = ref<EvidenceAsset[]>([])
const detail = ref<EvidenceAsset | null>(null); const txDetail = ref<EvidenceAsset | null>(null); const cidDetail = ref<any>(null)
const detailOpen = ref(false); const txOpen = ref(false); const cidOpen = ref(false); const cidLoading = ref(false)
const columns = [{ title: '资产名称', dataIndex: 'assetName', key: 'assetName', width: 220 }, { title: '类型', dataIndex: 'assetType', key: 'assetType', width: 100 }, { title: '状态', dataIndex: 'status', key: 'status', width: 110 }, { title: '内容哈希', dataIndex: 'fileHash', key: 'fileHash', width: 240 }, { title: '交易标识', dataIndex: 'txId', key: 'txId', width: 190 }, { title: '模式', dataIndex: 'mode', key: 'mode', width: 90 }, { title: '操作', key: 'action', fixed: 'right', width: 180 }]
const filtered = computed(() => assets.value.filter((item) => { const text = search.value.trim().toLowerCase(); return !text || [item.assetName, item.fileHash, item.txId].some((value) => value?.toLowerCase().includes(text)) }))
async function loadAssets() { loading.value = true; loadError.value = ''; try { assets.value = (await listMyAssets()).data } catch (error: any) { loadError.value = error.message || '资产加载失败' } finally { loading.value = false } }
function openDetail(record: EvidenceAsset) { detail.value = record; detailOpen.value = true }
function openTx(record: EvidenceAsset) { txDetail.value = record; txOpen.value = true }
async function previewCid(asset: EvidenceAsset) { if (!asset.cid) { message.info('该记录未返回 CID'); return }; cidOpen.value = true; cidLoading.value = true; cidDetail.value = null; try { cidDetail.value = (await getIpfsFile({ cid: asset.cid })).data } catch (error: any) { message.error(error.message || 'CID 查询失败') } finally { cidLoading.value = false } }
onMounted(loadAssets)
</script>

<template>
  <section class="asset-page">
    <a-page-header class="page-header" title="我的资产" sub-title="数据来自后端，已按当前账号的资产归属和机构权限过滤。"><template #extra><a-button type="primary" @click="$router.push('/evidence')">发行资产</a-button><a-button :loading="loading" @click="loadAssets">刷新</a-button></template></a-page-header>
    <a-alert class="mb" type="warning" show-icon message="Demo 模式提示" description="Demo CID 与 Demo 交易标识仅用于平台演示，不代表真实 IPFS 或真实链上交易。" />
    <a-card><a-input-search v-model:value="search" class="mb" placeholder="按资产名、哈希或交易标识筛选" allow-clear /><a-spin :spinning="loading"><a-alert v-if="loadError" type="error" show-icon :message="loadError" class="mb"><template #action><a-button size="small" @click="loadAssets">重试</a-button></template></a-alert><a-empty v-else-if="!loading && !filtered.length" description="暂无可见资产。可先发行一件资产，或确认当前账户拥有资产访问权限。" /><a-table v-else :columns="columns" :data-source="filtered" :scroll="{ x: 1100 }" row-key="fileHash" :pagination="{ pageSize: 8 }"><template #bodyCell="{ column, record }"><template v-if="column.key === 'status'"><a-tag :color="record.status === 'CERTIFIED' || record.status === 'AUTHORIZED' ? 'green' : 'orange'">{{ record.status }}</a-tag></template><template v-else-if="column.key === 'fileHash'"><a-typography-text code copyable>{{ record.fileHash }}</a-typography-text></template><template v-else-if="column.key === 'txId'"><a-typography-text code copyable>{{ record.txId }}</a-typography-text></template><template v-else-if="column.key === 'mode'"><a-tag :color="record.mode === 'DEMO' ? 'orange' : 'green'">{{ record.mode }}</a-tag></template><template v-else-if="column.key === 'action'"><a-space><a-button type="link" size="small" @click="openDetail(record)">详情</a-button><a-button type="link" size="small" @click="openTx(record)">交易</a-button><a-button type="link" size="small" @click="previewCid(record)">CID</a-button></a-space></template></template></a-table></a-spin></a-card>
    <a-modal v-model:open="detailOpen" title="资产详情" :footer="null" width="760px"><a-descriptions v-if="detail" bordered :column="{ xs: 1, sm: 2 }"><a-descriptions-item label="资产名称" :span="2">{{ detail.assetName }}</a-descriptions-item><a-descriptions-item label="创作者">{{ detail.creator }}</a-descriptions-item><a-descriptions-item label="机构">{{ detail.organization || '-' }}</a-descriptions-item><a-descriptions-item label="哈希" :span="2"><a-typography-text code copyable>{{ detail.fileHash }}</a-typography-text></a-descriptions-item><a-descriptions-item label="CID" :span="2"><a-typography-text code copyable>{{ detail.cid || '-' }}</a-typography-text></a-descriptions-item></a-descriptions></a-modal>
    <a-modal v-model:open="txOpen" title="交易详情" :footer="null"><a-descriptions v-if="txDetail" bordered :column="1"><a-descriptions-item label="交易标识"><a-typography-text code copyable>{{ txDetail.txId }}</a-typography-text></a-descriptions-item><a-descriptions-item label="区块高度">{{ txDetail.blockHeight }}</a-descriptions-item><a-descriptions-item label="网络">{{ txDetail.chainNetwork }}</a-descriptions-item><a-descriptions-item label="模式"><a-tag :color="txDetail.mode === 'DEMO' ? 'orange' : 'green'">{{ txDetail.mode }}</a-tag></a-descriptions-item></a-descriptions></a-modal>
    <a-modal v-model:open="cidOpen" title="CID 预览" :footer="null"><a-spin :spinning="cidLoading"><a-descriptions v-if="cidDetail" bordered :column="1"><a-descriptions-item label="CID"><a-typography-text code copyable>{{ cidDetail.cid }}</a-typography-text></a-descriptions-item><a-descriptions-item label="提供商">{{ cidDetail.provider }}</a-descriptions-item><a-descriptions-item label="固定状态">{{ cidDetail.pinStatus }}</a-descriptions-item><a-descriptions-item label="模式"><a-tag color="orange">{{ cidDetail.mode }}</a-tag></a-descriptions-item></a-descriptions><a-empty v-else-if="!cidLoading" description="暂无 CID 记录" /></a-spin></a-modal>
  </section>
</template>

<style scoped>
.asset-page { padding: 16px 20px 24px; background: linear-gradient(180deg, #f7fbff, #f5f7f9); min-height: 100%; }.page-header { margin-bottom: 16px; border: 1px solid #e2e8f0; border-radius: 16px; background: #fff; box-shadow: 0 14px 28px rgba(15,23,42,.08); }.mb { margin-bottom: 16px; }@media (max-width: 767px) { .asset-page { padding: 12px; } }
</style>
