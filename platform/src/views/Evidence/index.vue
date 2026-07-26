<template>
  <div class="evidence-page">
    <a-page-header
      class="page-header"
      title="版权存证工作台"
      sub-title="数字资产上链存证，形成可验证的版权证据链"
    >
      <template #extra>
        <a-button @click="resetForm">重置</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">
          <template #icon><SafetyCertificateOutlined /></template>
          提交存证
        </a-button>
      </template>
    </a-page-header>

    <a-row :gutter="24">
      <a-col :xs="24" :lg="16">
        <!-- 存证流程步骤 -->
        <a-card title="可信存证流程" class="mb-4">
          <a-steps :current="currentStep" size="small">
            <a-step title="上传资产" description="选择要存证的文件" />
            <a-step title="填写信息" description="完善资产元数据" />
            <a-step title="生成哈希" description="计算唯一标识" />
            <a-step title="上链存证" description="写入区块链" />
          </a-steps>
        </a-card>

        <!-- 文件上传 -->
        <a-card title="1. 上传资产素材" class="mb-4">
          <a-upload-dragger
            v-model:fileList="fileList"
            name="file"
            :multiple="false"
            :before-upload="beforeUpload"
            @change="handleFileChange"
          >
            <p class="ant-upload-drag-icon">
              <inbox-outlined />
            </p>
            <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p class="ant-upload-hint">
              支持图片、文档、音视频等多种格式，单个文件不超过100MB
            </p>
          </a-upload-dragger>
        </a-card>

        <!-- 资产信息表单 -->
        <a-card title="2. 资产元数据" class="mb-4">
          <a-form
            :model="formState"
            :label-col="{ xs: { span: 24 }, sm: { span: 6 }, lg: { span: 4 } }"
            :wrapper-col="{ xs: { span: 24 }, sm: { span: 18 }, lg: { span: 20 } }"
          >
            <a-form-item label="资产名称" required>
              <a-input v-model:value="formState.assetName" placeholder="请输入数字资产名称" />
            </a-form-item>
            <a-form-item label="资产类型" required>
              <a-select v-model:value="formState.assetType" placeholder="选择资产类型">
                <a-select-option value="image">图片/照片</a-select-option>
                <a-select-option value="video">视频</a-select-option>
                <a-select-option value="audio">音频</a-select-option>
                <a-select-option value="3d">3D模型</a-select-option>
                <a-select-option value="document">文档</a-select-option>
                <a-select-option value="other">其他</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="创作者">
              <a-input v-model:value="formState.creator" placeholder="请输入创作者姓名或机构" />
            </a-form-item>
            <a-form-item label="所属机构">
              <a-input v-model:value="formState.organization" placeholder="如：上海对外经贸大学博物馆" />
            </a-form-item>
            <a-form-item label="资产描述">
              <a-textarea v-model:value="formState.description" :rows="4" placeholder="请详细描述该数字资产的内容、来源、历史背景等信息" />
            </a-form-item>
            <a-form-item label="关键词">
              <a-select v-model:value="formState.keywords" mode="tags" placeholder="添加关键词标签">
                <a-select-option value="文物">文物</a-select-option>
                <a-select-option value="艺术品">艺术品</a-select-option>
                <a-select-option value="历史文献">历史文献</a-select-option>
                <a-select-option value="数字藏品">数字藏品</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <!-- 右侧信息面板 -->
      <a-col :xs="24" :lg="8">
        <!-- 哈希信息 -->
        <a-card title="3. 可信哈希" class="mb-4">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="文件哈希">
              <a-typography-text v-if="hashInfo.fileHash" code copyable>
                {{ hashInfo.fileHash }}
              </a-typography-text>
              <span v-else class="text-muted">待生成</span>
            </a-descriptions-item>
            <a-descriptions-item label="时间戳">
              {{ hashInfo.timestamp || '待生成' }}
            </a-descriptions-item>
            <a-descriptions-item label="唯一标识">
              <a-typography-text v-if="hashInfo.uniqueId" code copyable>
                {{ hashInfo.uniqueId }}
              </a-typography-text>
              <span v-else class="text-muted">待生成</span>
            </a-descriptions-item>
          </a-descriptions>
          <a-divider />
          <p class="hash-formula">H = Hash(Asset content ∥ Timestamp)</p>
        </a-card>

        <!-- 链上信息 -->
        <a-card title="4. 链上存证" class="mb-4">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="区块链网络">
              <a-tag color="blue">百度超级链测试网</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="合约名称">
              eleccert
            </a-descriptions-item>
            <a-descriptions-item label="存证状态">
              <a-tag :color="txResult.status === 'success' ? 'green' : txResult.status === 'pending' ? 'orange' : 'default'">
                {{ txResult.statusText }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-if="txResult.txId" label="交易ID">
              <a-typography-text code copyable style="font-size: 12px;">
                {{ txResult.txId }}
              </a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item v-if="txResult.blockHeight" label="区块高度">
              {{ txResult.blockHeight }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="存证要点" class="mb-4">
          <div class="defense-points">
            <div class="defense-point" v-for="point in defensePoints" :key="point.title">
              <span>{{ point.tag }}</span>
              <strong>{{ point.title }}</strong>
              <p>{{ point.desc }}</p>
            </div>
          </div>
        </a-card>

        <!-- 费用估算 -->
        <a-card title="存证费用">
          <a-statistic title="预估存证费用" :value="0.01" prefix="¥" :precision="2" />
          <p class="fee-note">* 实际费用以链上执行结果为准</p>
        </a-card>
      </a-col>
    </a-row>

    <!-- 存证成功弹窗 -->
    <a-modal v-model:open="showSuccessModal" title="存证成功" :footer="null" width="92%">
      <a-result status="success" title="版权存证成功！" sub-title="您的数字资产已成功上链，获得不可篡改的版权证明">
        <template #extra>
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="交易ID">{{ txResult.txId }}</a-descriptions-item>
            <a-descriptions-item label="虚拟NFT ID">{{ virtualNftId }}</a-descriptions-item>
            <a-descriptions-item label="区块高度">{{ txResult.blockHeight }}</a-descriptions-item>
            <a-descriptions-item label="存证时间">{{ txResult.time }}</a-descriptions-item>
          </a-descriptions>
          <div class="success-actions">
            <a-button type="primary" @click="goToQuery">查看存证记录</a-button>
            <a-button @click="resetAndClose">继续存证</a-button>
          </div>
        </template>
      </a-result>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SafetyCertificateOutlined, InboxOutlined } from '@ant-design/icons-vue'
import { calculateFileHash } from '../../utils/hash'
import { saveEvidenceRecord } from '../../services/evidenceStore'

const router = useRouter()
const fileList = ref([])
const submitting = ref(false)
const showSuccessModal = ref(false)
const virtualNftId = ref('')

const formState = reactive({
  assetName: '',
  assetType: undefined,
  creator: '',
  organization: '',
  description: '',
  keywords: []
})

const hashInfo = reactive({
  fileHash: '',
  timestamp: '',
  uniqueId: ''
})

const txResult = reactive({
  status: 'idle',
  statusText: '待提交',
  txId: '',
  blockHeight: '',
  time: ''
})

const defensePoints = [
  {
    tag: '可信',
    title: '哈希 + 时间戳',
    desc: '生成唯一指纹，确保内容可校验、可追溯。'
  },
  {
    tag: '安全',
    title: '链上存证',
    desc: '多节点共识写入区块链，防篡改。'
  },
  {
    tag: '效率',
    title: '快速确权',
    desc: '秒级完成确权流程，适配日常确权需求。'
  }
]

const currentStep = computed(() => {
  if (txResult.status === 'success') return 3
  if (hashInfo.uniqueId) return 2
  if (fileList.value.length > 0) return 1
  return 0
})

const beforeUpload = (file) => {
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    message.error('文件大小不能超过100MB!')
  }
  return false // 阻止自动上传
}

const handleFileChange = (info) => {
  if (info.fileList.length > 0) {
    generateHash(info.fileList[0].originFileObj)
  }
}

const generateHash = async (file) => {
  const timestamp = new Date().toISOString()
  const fileHash = await calculateFileHash(file)

  hashInfo.fileHash = fileHash
  hashInfo.timestamp = timestamp
  hashInfo.uniqueId = 'H_' + fileHash.slice(7, 23)
}

const buildVirtualNftId = () => {
  const seed = hashInfo.uniqueId || Math.random().toString(16).slice(2, 10)
  return `VNFT_${seed}_${Date.now().toString(36).slice(-6)}`
}

const handleSubmit = async () => {
  if (!fileList.value.length) {
    message.warning('请先上传文件')
    return
  }
  if (!formState.assetName) {
    message.warning('请填写资产名称')
    return
  }

  submitting.value = true
  txResult.status = 'pending'
  txResult.statusText = '存证中...'

  // 模拟上链过程
  setTimeout(() => {
    txResult.status = 'success'
    txResult.statusText = '存证成功'
    txResult.txId = 'tx_' + hashInfo.fileHash.slice(7, 23) + '_' + Date.now().toString(36).slice(-6)
    txResult.blockHeight = 5200000 + Math.abs(parseInt(hashInfo.fileHash.slice(7, 15), 16) % 900000)
    txResult.time = new Date().toLocaleString()
    virtualNftId.value = buildVirtualNftId()
    saveEvidenceRecord({
      assetName: formState.assetName,
      assetType: formState.assetType,
      creator: formState.creator || '未填写',
      organization: formState.organization || '未填写',
      description: formState.description,
      keywords: formState.keywords,
      hash: hashInfo.fileHash,
      uniqueId: hashInfo.uniqueId,
      certifyTime: txResult.time,
      blockHeight: txResult.blockHeight,
      txId: txResult.txId,
      cid: `bafybei${hashInfo.fileHash.slice(7, 31)}`,
      chainNetwork: '百度超级链开放测试网络',
      contractName: 'eleccert'
    })
    submitting.value = false
    showSuccessModal.value = true
    message.success('存证成功！')
  }, 2000)
}

const resetForm = () => {
  fileList.value = []
  Object.assign(formState, {
    assetName: '',
    assetType: undefined,
    creator: '',
    organization: '',
    description: '',
    keywords: []
  })
  Object.assign(hashInfo, {
    fileHash: '',
    timestamp: '',
    uniqueId: ''
  })
  Object.assign(txResult, {
    status: 'idle',
    statusText: '待提交',
    txId: '',
    blockHeight: '',
    time: ''
  })
  virtualNftId.value = ''
}

const goToQuery = () => {
  showSuccessModal.value = false
  router.push('/query')
}

const resetAndClose = () => {
  showSuccessModal.value = false
  resetForm()
}
</script>

<style scoped>
.evidence-page {
  padding: 16px 20px 24px;
  background:
    radial-gradient(900px 420px at 8% -10%, rgba(14, 116, 144, 0.14), transparent 55%),
    linear-gradient(180deg, #f7fbff 0%, #f5f7f9 100%);
}

.page-header {
  margin-bottom: 16px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 65%, #fff7ed 100%);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.page-header :deep(.ant-page-header-heading-title) {
  font-family: var(--font-serif);
  color: var(--brand-blue-strong);
}

.page-header :deep(.ant-page-header-heading-sub-title) {
  color: var(--text-muted);
}

.evidence-page :deep(.ant-card) {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.evidence-page :deep(.ant-card-head) {
  background: #f8fafc;
  border-bottom: none;
}

.mb-4 {
  margin-bottom: 16px;
}

.text-muted {
  color: rgba(0, 0, 0, 0.45);
}

.hash-formula {
  text-align: center;
  font-family: 'Courier New', monospace;
  color: var(--brand-blue);
  font-weight: 500;
  margin: 0;
}

.fee-note {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
  margin-top: 8px;
  margin-bottom: 0;
}

.defense-points {
  display: grid;
  gap: 12px;
}

.defense-point {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(14, 116, 144, 0.16);
  background: rgba(14, 165, 233, 0.06);
  display: grid;
  gap: 6px;
}

.defense-point span {
  font-size: 11px;
  color: #0f766e;
  letter-spacing: 0.6px;
}

.defense-point strong {
  font-size: 14px;
  color: var(--brand-blue-strong);
}

.defense-point p {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

.success-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

@media (max-width: 767px) {
  .evidence-page {
    padding: 12px;
  }

  .page-header :deep(.ant-page-header-heading) {
    flex-wrap: wrap;
    row-gap: 8px;
  }

  .page-header :deep(.ant-page-header-heading-extra) {
    width: 100%;
    display: flex;
    gap: 8px;
  }

  .page-header :deep(.ant-page-header-heading-extra .ant-btn) {
    flex: 1;
  }

  .success-actions :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
