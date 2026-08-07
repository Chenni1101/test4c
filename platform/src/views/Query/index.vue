<template>
  <div class="query-page">
    <a-page-header
      class="page-header"
      title="存证查询中心"
      sub-title="链上验证数字资产的版权存证信息"
    />

    <div class="query-flow mb-4">
      <div class="query-step" v-for="(step, index) in querySteps" :key="step.title">
        <span class="step-index">{{ index + 1 }}</span>
        <div>
          <h4>{{ step.title }}</h4>
          <p>{{ step.desc }}</p>
        </div>
      </div>
    </div>

    <!-- 查询方式选择 -->
    <a-card class="mb-4">
      <a-tabs v-model:activeKey="queryType">
        <a-tab-pane key="hash" tab="按哈希值查询">
          <a-space direction="vertical" style="width: 100%;">
            <a-input-search
              v-model:value="hashQuery"
              placeholder="请输入 64 位内容哈希（可带 sha256: 前缀）"
              size="large"
              enter-button="查询"
              :loading="searching"
              @search="handleHashQuery"
            />
            <p class="query-hint">支持带或不带 sha256: 前缀的 64 位内容哈希。</p>
          </a-space>
        </a-tab-pane>
        <a-tab-pane key="file" tab="按文件查询">
          <a-input v-model:value="expectedHash" class="mb-4" placeholder="粘贴预期 SHA-256（用于篡改核验）" />
          <a-upload-dragger
            v-model:fileList="queryFileList"
            name="file"
            :multiple="false"
            :before-upload="() => false"
            @change="handleFileQuery"
          >
            <p class="ant-upload-drag-icon">
              <inbox-outlined />
            </p>
            <p class="ant-upload-text">上传文件进行验证</p>
            <p class="ant-upload-hint">系统将计算文件哈希并在链上查询存证记录</p>
          </a-upload-dragger>
        </a-tab-pane>
        <a-tab-pane key="advanced" tab="高级查询">
          <a-form layout="inline" class="advanced-form">
            <a-form-item label="创作者">
              <a-input v-model:value="advancedQuery.creator" placeholder="创作者名称" />
            </a-form-item>
            <a-form-item label="机构">
              <a-input v-model:value="advancedQuery.organization" placeholder="所属机构" />
            </a-form-item>
            <a-form-item label="时间范围">
              <a-range-picker v-model:value="advancedQuery.dateRange" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="searching" @click="handleAdvancedQuery">查询</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 查询结果 -->
    <a-card title="查询结果" v-if="queryResult">
      <a-result
        v-if="queryResult.found"
        status="success"
        title="存证验证通过"
        sub-title="后端已返回可见范围内的存证/溯源结果，请结合外部回执复核。"
      >
        <template #extra>
          <a-descriptions :column="2" bordered class="result-desc">
            <a-descriptions-item label="资产名称" :span="2">
              {{ queryResult.data.assetName || queryResult.data.assetCode || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="资产编号" :span="2">
              <a-typography-text code copyable>{{ queryResult.data.assetCode || queryResult.data.uniqueId || '-' }}</a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="资产哈希">
              <a-typography-text code copyable style="font-size: 12px;">
              {{ queryResult.data.hash || queryResult.data.computedSha256 || '-' }}
              </a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="唯一标识">
              <a-typography-text code>{{ queryResult.data.uniqueId }}</a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="创作者">{{ queryResult.data.creator }}</a-descriptions-item>
            <a-descriptions-item label="所属机构">{{ queryResult.data.organization }}</a-descriptions-item>
            <a-descriptions-item label="存证时间">{{ queryResult.data.certifyTime }}</a-descriptions-item>
            <a-descriptions-item label="区块高度">{{ queryResult.data.blockHeight }}</a-descriptions-item>
            <a-descriptions-item label="交易ID" :span="2">
              <a-typography-text code copyable>{{ queryResult.data.txId }}</a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="区块链网络" :span="2">
              <a-tag color="blue">{{ queryResult.data.chainNetwork || '后端溯源接口' }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
          
          <div class="result-actions">
            <a-button type="primary" @click="downloadCertificate">
              <template #icon><DownloadOutlined /></template>
              下载存证证书
            </a-button>
            <a-button @click="viewOnChain">
              <template #icon><LinkOutlined /></template>
              区块链浏览器查看
            </a-button>
          </div>
        </template>
      </a-result>

      <a-result
        v-else
        status="warning"
        title="未找到存证记录"
        sub-title="该资产尚未在区块链上进行版权存证"
      >
        <template #extra>
          <a-button type="primary" @click="$router.push('/evidence')">立即存证</a-button>
          <a-button @click="clearResult">重新查询</a-button>
        </template>
      </a-result>
    </a-card>

    <!-- 最近查询记录 -->
    <a-card title="最近查询记录" class="mt-4" v-if="!queryResult">
      <a-table
        :columns="historyColumns"
        :data-source="queryHistory"
        :pagination="false"
        :scroll="{ x: 720 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.found ? 'green' : 'orange'">
              {{ record.found ? '已存证' : '未存证' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'query'">
            <a-typography-text code style="font-size: 12px;">
              {{ record.query.length > 30 ? record.query.slice(0, 30) + '...' : record.query }}
            </a-typography-text>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="requery(record)">重新查询</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { InboxOutlined, DownloadOutlined, LinkOutlined } from '@ant-design/icons-vue'
import { calculateFileHash } from '../../utils/hash'
import { getEvidenceByHash } from '@/services/assetApi'
import { verifyFileHash } from '@/services/authorizationProvenanceApi'

const router = useRouter()
const queryType = ref('hash')
const hashQuery = ref('')
const queryFileList = ref([])
const expectedHash = ref('')
const searching = ref(false)
const queryResult = ref(null)

const querySteps = [
  {
    title: '输入检索',
    desc: '支持哈希值、文件上传与高级检索条件。'
  },
  {
    title: '计算校验',
    desc: '生成指纹并与链上哈希进行比对。'
  },
  {
    title: '链上验证',
    desc: '校验区块高度与交易ID，确保证据可追溯。'
  },
  {
    title: '可信报告',
    desc: '生成可下载的存证证书与链上引用。'
  }
]

const advancedQuery = reactive({
  creator: '',
  organization: '',
  dateRange: null
})

const historyColumns = [
  { title: '查询内容', dataIndex: 'query', key: 'query' },
  { title: '查询时间', dataIndex: 'time', key: 'time', width: 180 },
  { title: '状态', dataIndex: 'found', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 }
]

const queryHistory = ref([])

const handleHashQuery = async () => {
  if (!hashQuery.value.trim()) {
    message.warning('请输入查询内容')
    return
  }
  searching.value = true
  try {
    const response = await getEvidenceByHash(hashQuery.value.trim())
    queryResult.value = { found: true, data: { ...response.data, hash: response.data.fileHash } }
    queryHistory.value.unshift({
      id: Date.now(),
      query: hashQuery.value,
      time: new Date().toLocaleString(),
      found: true
    })
  } catch (error) {
    queryResult.value = { found: false }
    if (error.status && error.status !== 404) message.error(error.message || '查询失败')
    queryHistory.value.unshift({ id: Date.now(), query: hashQuery.value, time: new Date().toLocaleString(), found: false })
  } finally { searching.value = false }
}

const handleFileQuery = async (info) => {
  if (info.fileList.length > 0) {
    if (!expectedHash.value.trim()) { message.warning('请先提供预期 SHA-256'); return }
    searching.value = true
    try {
      const file = info.fileList[0].originFileObj
      const localHash = await calculateFileHash(file)
      const response = await verifyFileHash({ file, expectedHash: expectedHash.value.trim() })
      queryResult.value = { found: response.data.matches, data: { ...response.data, hash: localHash, uniqueId: response.data.versionId, certifyTime: new Date().toLocaleString() } }
      if (!response.data.matches) message.warning('文件哈希与预期值不一致，可能已被篡改或选错文件')
    } catch (error) { queryResult.value = { found: false }; message.error(error.message || '文件核验失败') } finally { searching.value = false }
  }
}

const handleAdvancedQuery = () => {
  if (!advancedQuery.creator && !advancedQuery.organization) {
    message.warning('请至少填写一个查询条件')
    return
  }
  message.info('当前后端尚未提供按创作者/机构的高级检索接口，请使用资产哈希或文件核验。')
}

const clearResult = () => {
  queryResult.value = null
  hashQuery.value = ''
  queryFileList.value = []
  expectedHash.value = ''
}

const requery = (record) => {
  hashQuery.value = record.query
  queryType.value = 'hash'
  handleHashQuery()
}

const downloadCertificate = () => {
  message.info('当前仅支持从资产详情页导出溯源报告。')
}

const viewOnChain = () => {
  message.info('当前交易未配置可访问的链浏览器地址。')
}
</script>

<style scoped>
.query-page {
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

.query-page :deep(.ant-card) {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.query-page :deep(.ant-card-head) {
  background: #f8fafc;
  border-bottom: none;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}

.query-hint {
  color: var(--text-subtle);
  font-size: 12px;
  margin: 0;
}

.query-flow {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
}

.query-step {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(14, 116, 144, 0.14);
  background: rgba(14, 165, 233, 0.05);
}

.step-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9, #d4a248);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.query-step h4 {
  margin: 0 0 4px;
  font-size: 13px;
  color: var(--brand-blue-strong);
}

.query-step p {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

.result-desc {
  text-align: left;
  margin-bottom: 24px;
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 16px;
  flex-wrap: wrap;
}

@media (max-width: 767px) {
  .query-page {
    padding: 12px;
  }

  .query-flow {
    grid-template-columns: 1fr;
  }

  .advanced-form :deep(.ant-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .advanced-form :deep(.ant-form-item-control) {
    width: 100%;
  }

  .result-actions :deep(.ant-btn) {
    width: 100%;
  }
}

@media (max-width: 991px) and (min-width: 768px) {
  .query-flow {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
