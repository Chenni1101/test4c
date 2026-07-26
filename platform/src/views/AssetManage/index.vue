<template>
  <div class="asset-page">
    <div class="asset-hero">
      <div class="asset-hero-copy">
        <div class="asset-hero-kicker">
          <span>文旅数字资产版权存证</span>
          <span class="kicker-divider"></span>
          <span>可信证据链平台</span>
        </div>
        <h1>让展品被看见，也被记住</h1>
        <p>
          从静态数据到场景化叙事，展品图像、链上存证、收藏行为与交易流统一呈现，
          让每一件藏品既有证据，也有温度
        </p>
        <div class="asset-hero-tags">
          <span>文旅数字资产</span>
          <span>区块链可信存证</span>
          <span>馆藏协作</span>
        </div>
        <a-space>
          <a-button type="primary" size="large" @click="$router.push('/evidence')">
            <template #icon><PlusOutlined /></template>
            新增存证
          </a-button>
          <a-button size="large" @click="scrollToTransactions">查看交易图像</a-button>
        </a-space>
      </div>
      <div class="asset-hero-visual">
        <img :src="heroImage" alt="展品场景" />
        <div class="hero-seal">
          <span>可信存证</span>
          <strong>文旅联盟链</strong>
          <small>Block #12,586,402</small>
        </div>
      </div>
    </div>

    <a-row :gutter="16" class="mb-4">
      <a-col :xs="12" :sm="12" :lg="6">
        <a-card class="metric-card">
          <a-statistic title="总资产数" :value="200" suffix="件">
            <template #prefix><FolderOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :lg="6">
        <a-card class="metric-card">
          <a-statistic title="已存证" :value="200" suffix="件" :value-style="{ color: '#389e0d' }">
            <template #prefix><CheckCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="12" :lg="6">
        <a-card class="metric-card">
          <a-statistic title="收藏人数" :value="totalLoves" suffix="人次" :value-style="{ color: '#cf1322' }">
            <template #prefix><FireOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <div class="chain-timeline mb-4">
      <div class="chain-step" v-for="(step, index) in chainSteps" :key="step.title">
        <div class="chain-index">{{ index + 1 }}</div>
        <div class="chain-body">
          <h4>{{ step.title }}</h4>
          <p>{{ step.desc }}</p>
        </div>
        <span class="chain-tag">{{ step.tag }}</span>
      </div>
    </div>

    <div class="scene-grid mb-4">
      <div class="scene-card" v-for="item in sceneCards" :key="item.title">
        <img :src="item.image" :alt="item.title" />
        <div class="scene-mask">
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </div>

    <a-card class="mb-4 filter-card" title="筛选资产">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :lg="7">
          <a-input-search v-model:value="searchText" placeholder="搜索资产名称" @search="handleSearch" />
        </a-col>
        <a-col :xs="12" :sm="8" :lg="4">
          <a-select v-model:value="filterType" placeholder="资产类型" style="width: 100%" allow-clear>
            <a-select-option value="image">图片</a-select-option>
            <a-select-option value="video">视频</a-select-option>
            <a-select-option value="3d">3D模型</a-select-option>
            <a-select-option value="document">文档</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="12" :sm="8" :lg="4">
          <a-select v-model:value="filterStatus" placeholder="存证状态" style="width: 100%" allow-clear>
            <a-select-option value="certified">已存证</a-select-option>
            <a-select-option value="pending">待存证</a-select-option>
          </a-select>
        </a-col>
      </a-row>
    </a-card>

    <a-row :gutter="16" class="mb-4 asset-main-grid">
      <a-col :xs="24" :lg="18">
        <a-card title="资产列表" class="asset-table-card">
          <a-table
            :columns="columns"
            :data-source="filteredAssets"
            :pagination="{ pageSize: 6 }"
            :scroll="{ x: 1500 }"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'thumbnail'">
                <img class="thumb" :src="record.image" :alt="record.name" />
              </template>
              <template v-if="column.key === 'name'">
                <a-space>
                  <FileImageOutlined v-if="record.type === 'image'" style="color: #1677ff;" />
                  <VideoCameraOutlined v-else-if="record.type === 'video'" style="color: #13a8a8;" />
                  <FileTextOutlined v-else style="color: #389e0d;" />
                  <span>{{ record.name }}</span>
                </a-space>
              </template>
              <template v-if="column.key === 'type'">
                <a-tag>{{ typeMap[record.type] }}</a-tag>
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'certified' ? 'green' : 'orange'">
                  {{ record.status === 'certified' ? '已存证' : '待存证' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'likes'">
                <span class="likes-cell">
                  <HeartFilled style="color: #ff4d4f" /> {{ record.likes }}
                </span>
              </template>
              <template v-if="column.key === 'hash'">
                <a-typography-text v-if="record.hash" code copyable style="font-size: 12px;">
                  {{ record.hash.slice(0, 14) }}...
                </a-typography-text>
                <span v-else class="text-muted">-</span>
              </template>
              <template v-if="column.key === 'usageScenes'">
                <div v-if="record.status === 'certified'" class="usage-tags">
                  <a-tag
                    v-for="scene in usageTopScenesFor(record.name, 3)"
                    :key="scene.name"
                    color="blue"
                  >
                    {{ scene.name }} {{ scene.count }}
                  </a-tag>
                  <a-tag v-if="usageExtraCountFor(record.name, 3)" color="default">
                    +{{ usageExtraCountFor(record.name, 3) }}
                  </a-tag>
                </div>
                <span v-else class="text-muted">-</span>
              </template>
              <template v-if="column.key === 'usageTotal'">
                <span v-if="record.status === 'certified'" class="usage-total">
                  {{ usageTotalFor(record.name) }}
                </span>
                <span v-else class="text-muted">-</span>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="viewDetail(record)">详情</a-button>
                  <a-button type="link" size="small" @click="toggleCollect(record)">
                    <HeartFilled v-if="record.collected" style="color: #ff4d4f" />
                    <HeartOutlined v-else />
                  </a-button>
                  <a-button v-if="record.status !== 'certified'" type="link" size="small" @click="certifyAsset(record)">
                    存证
                  </a-button>
                  <a-button type="link" size="small" danger @click="deleteAsset(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="6">
        <a-card title="我的收藏" class="favorite-card mb-4">
          <div v-if="collectedAssets.length" class="favorite-list">
            <div class="favorite-item" v-for="item in collectedAssets" :key="item.id">
              <img :src="item.image" :alt="item.name" />
              <div>
                <h4>{{ item.name }}</h4>
                <p>{{ item.scene }}</p>
              </div>
            </div>
          </div>
          <a-empty v-else description="还没有收藏展品" />
        </a-card>

        <a-card title="资产状态概览" class="insight-card mb-4">
          <div class="insight-item">
            <div class="insight-row">
              <span>已存证</span>
              <strong>{{ certifiedCount }} / {{ assets.length }}</strong>
            </div>
            <a-progress :percent="certifyRate" :show-info="false" stroke-color="#13a8a8" />
          </div>
          <div class="insight-item" v-for="item in typeStats" :key="item.label">
            <div class="insight-row">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}件</strong>
            </div>
            <a-progress :percent="item.percent" :show-info="false" :stroke-color="item.color" />
          </div>
        </a-card>

        <a-card title="快捷操作" class="quick-card">
          <a-button block type="primary" class="mb-2" @click="$router.push('/evidence')">
            发起新存证
          </a-button>
          <a-button block class="mb-2" @click="$router.push('/query')">
            去存证查询
          </a-button>
          <a-button block @click="scrollToTransactions">
            跳转交易图像流
          </a-button>
        </a-card>
      </a-col>
    </a-row>

    <a-card id="tx-visual" title="最近交易图像流" class="mb-4 tx-card">
      <div class="tx-flow">
        <div class="tx-item" v-for="tx in recentTransactions" :key="tx.id" @click="openTxDrawer(tx)">
          <img :src="tx.image" :alt="tx.name" />
          <div class="tx-content">
            <h4>{{ tx.name }}</h4>
            <p>{{ tx.note }}</p>
            <div class="tx-bottom">
              <span>{{ tx.time }}</span>
              <a-tag color="green">交易上链完成</a-tag>
              <a-tag color="blue">点击查看详情</a-tag>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <a-drawer
      v-model:open="showTxDrawer"
      title="交易详情"
      placement="right"
      width="460"
    >
      <template v-if="currentTx">
        <div class="tx-drawer-cover">
          <img :src="currentTx.image" :alt="currentTx.name" />
        </div>
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="交易展品">{{ currentTx.name }}</a-descriptions-item>
          <a-descriptions-item label="链上哈希">
            <a-typography-text code copyable>{{ currentTx.hash }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="交易ID">
            <a-typography-text code copyable>{{ currentTx.txId }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="授权方">{{ currentTx.licensor }}</a-descriptions-item>
          <a-descriptions-item label="受让方">{{ currentTx.assignee }}</a-descriptions-item>
          <a-descriptions-item label="交易时间">{{ currentTx.time }}</a-descriptions-item>
          <a-descriptions-item label="交易说明">{{ currentTx.note }}</a-descriptions-item>
        </a-descriptions>
      </template>
    </a-drawer>

    <a-modal v-model:open="showDetailModal" title="资产详情" width="900px" :footer="null">
      <template v-if="currentAsset">
        <div class="detail-cover">
          <img :src="currentAsset.image" :alt="currentAsset.name" />
        </div>
        <a-descriptions :column="{ xs: 1, sm: 2 }" bordered>
          <a-descriptions-item label="资产名称" :span="2">{{ currentAsset.name }}</a-descriptions-item>
          <a-descriptions-item label="资产类型">{{ typeMap[currentAsset.type] }}</a-descriptions-item>
          <a-descriptions-item label="存证状态">
            <a-tag :color="currentAsset.status === 'certified' ? 'green' : 'orange'">
              {{ currentAsset.status === 'certified' ? '已存证' : '待存证' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="展陈场景" :span="2">{{ currentAsset.scene }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ currentAsset.createTime }}</a-descriptions-item>
          <a-descriptions-item label="创作者">{{ currentAsset.creator }}</a-descriptions-item>
          <a-descriptions-item label="哈希值" :span="2" v-if="currentAsset.hash">
            <a-typography-text code copyable>{{ currentAsset.hash }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="交易ID" :span="2" v-if="currentAsset.txId">
            <a-typography-text code copyable>{{ currentAsset.txId }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="描述" :span="2">{{ currentAsset.description }}</a-descriptions-item>
          <a-descriptions-item
            v-if="currentAsset.status === 'certified'"
            label="调用统计"
            :span="2"
          >
            <div class="usage-tags">
              <a-tag
                v-for="scene in usageTopScenesFor(currentAsset.name, 5)"
                :key="scene.name"
                color="blue"
              >
                {{ scene.name }} {{ scene.count }}
              </a-tag>
              <a-tag v-if="usageExtraCountFor(currentAsset.name, 5)" color="default">
                +{{ usageExtraCountFor(currentAsset.name, 5) }}
              </a-tag>
            </div>
          </a-descriptions-item>
        </a-descriptions>
      </template>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  FolderOutlined,
  CheckCircleOutlined,
  RiseOutlined,
  FireOutlined,
  FileImageOutlined,
  VideoCameraOutlined,
  FileTextOutlined,
  HeartOutlined,
  HeartFilled
} from '@ant-design/icons-vue'
import { getUsageSnapshot, formatUsageScenes } from '../../services/assetUsage'

const heroImage = '/exhibits/44c6dedd234af2dade7c7bf2fc3b1383.jpg'

const searchText = ref('')
const filterType = ref(undefined)
const filterStatus = ref(undefined)
const showDetailModal = ref(false)
const currentAsset = ref(null)
const showTxDrawer = ref(false)
const currentTx = ref(null)

const typeMap = {
  image: '图片',
  video: '视频',
  '3d': '3D模型',
  document: '文档'
}

const columns = [
  { title: '图像', dataIndex: 'image', key: 'thumbnail', width: 84 },
  { title: '资产名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '热度', dataIndex: 'likes', key: 'likes', width: 90 },
  { title: '哈希值', dataIndex: 'hash', key: 'hash', width: 180 },
  { title: '调用场景', dataIndex: 'usageScenes', key: 'usageScenes', width: 220 },
  { title: '调用次数', dataIndex: 'usageTotal', key: 'usageTotal', width: 110 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 180 }
]

const assets = ref([
  {
    id: 1,
    name: '新艺术葡萄少女花瓶',
    type: 'image',
    status: 'certified',
    likes: 96,
    collected: true,
    image: '/exhibits/6b84b365fdb2bfb285d529708f797a8b.jpg',
    scene: '文旅馆A厅 - 光影沉浸展柜',
    hash: 'sha256:9a24fe5d1ac6b7efb0f4',
    createTime: '2026-03-21 10:30',
    creator: '博物馆数字化团队',
    txId: 'tx_8273612091',
    description: '新艺术时期瓷艺代表作，适用于展陈故事化传播与文创衍生。'
  },
  {
    id: 2,
    name: '韦奇伍德蓝陶双耳瓶',
    type: '3d',
    status: 'certified',
    likes: 132,
    collected: true,
    image: '/exhibits/a8402b3d96aae48cf0889878e3545781.jpg',
    scene: '数字孪生馆 - 360交互陈列',
    hash: 'sha256:5be90c82df3c7ca0f89b',
    createTime: '2026-03-19 14:20',
    creator: '3D建模团队',
    txId: 'tx_9283746510',
    description: '浮雕与釉色细节保存完整，适合高精度数字资产交易。'
  },
  {
    id: 3,
    name: '彩绘巴洛克执壶',
    type: 'video',
    status: 'pending',
    likes: 64,
    collected: false,
    image: '/exhibits/3bff7f176da38618331cbf101c3c6e78.jpg',
    scene: '流动展演车 - 城市巡展',
    hash: '',
    createTime: '2026-03-18 09:15',
    creator: '修复部门',
    description: '修复前后对比影像已完成，待发起链上存证。'
  },
  {
    id: 4,
    name: '绿地粉彩中国风茶具',
    type: 'document',
    status: 'certified',
    likes: 78,
    collected: false,
    image: '/exhibits/ce099df5b991e285684b5b5021a178d4.jpg',
    scene: '东方器物展 - 茶文化章节',
    hash: 'sha256:ef310be4c8d7793512f8',
    createTime: '2026-03-16 16:45',
    creator: '鉴定专家组',
    txId: 'tx_3344556677',
    description: '包含图录、鉴定报告与外销瓷背景说明。'
  },
  {
    id: 5,
    name: '白瓷北极熊雕塑',
    type: 'image',
    status: 'pending',
    likes: 42,
    collected: false,
    image: '/exhibits/68746e32f68eda77bae91e0b7c24828a.jpg',
    scene: '北欧工艺专题厅 - 冬季限定',
    hash: '',
    createTime: '2026-03-15 11:00',
    creator: '摄影团队',
    description: '高清主图已上传，等待版权确权流程完成。'
  },
  {
    id: 6,
    name: '粉彩花卉茶壶套组',
    type: 'video',
    status: 'certified',
    likes: 110,
    collected: false,
    image: '/exhibits/f580ff0de4447ac727a07cc87c80baf2.jpg',
    scene: '古典生活美学馆 - 沉浸茶席',
    hash: 'sha256:89bc7f22d9ef1c4306a1',
    createTime: '2026-03-14 13:30',
    creator: '展览部',
    txId: 'tx_1122334455',
    description: '器物故事短片已上链，适合导览视频和交易预览。'
  },
  {
    id: 7,
    name: '青金釉葫芦瓶藏品图像',
    type: 'image',
    status: 'certified',
    likes: 148,
    collected: true,
    image: '/exhibits/6fe46f53cce0cb23d0aa90936c8f3bb4.jpg',
    scene: '重点展陈区 - 高光陈列',
    hash: 'sha256:4f1c9ab772a4c5d9207e',
    createTime: '2026-03-12 09:40',
    creator: '展陈策划组',
    txId: 'tx_9901122334',
    description: '展厅主视觉物料已更新，适合跨馆宣传与数字展陈。'
  },
  {
    id: 8,
    name: '洛可可风瓷钢琴音乐盒',
    type: 'video',
    status: 'certified',
    likes: 126,
    collected: false,
    image: '/exhibits/148271cf7d8fa34e60f8201bfb01dfac.jpg',
    scene: '音乐器物区 - 互动展演',
    hash: 'sha256:71be5c9e882d4e9a2c1f',
    createTime: '2026-03-11 15:20',
    creator: '文创研发组',
    txId: 'tx_6655443322',
    description: '联动导览音轨与短视频素材，适用于数字衍生授权。'
  },
  {
    id: 9,
    name: '馆藏器物纹样资料汇编',
    type: 'document',
    status: 'pending',
    likes: 72,
    collected: false,
    image: '/exhibits/ce099df5b991e285684b5b5021a178d4.jpg',
    scene: '数字典藏中心 - 文献柜',
    hash: '',
    createTime: '2026-03-10 11:30',
    creator: '资料整理组',
    description: '纹样数据库完成初稿，待校对后提交存证。'
  }
])

const usageSnapshot = ref(getUsageSnapshot())

const getUsage = (assetName) => usageSnapshot.value[assetName] || { total: 0, scenes: {} }
const usageScenesFor = (assetName) => formatUsageScenes(getUsage(assetName).scenes)
const usageTopScenesFor = (assetName, limit = 3) => usageScenesFor(assetName).slice(0, limit)
const usageExtraCountFor = (assetName, limit = 3) => Math.max(usageScenesFor(assetName).length - limit, 0)
const usageTotalFor = (assetName) => getUsage(assetName).total

const sceneCards = [
  {
    title: '沉浸展柜场景',
    desc: '借助图像叠层与光影脚本，还原器物在真实展厅中的观看体验。',
    image: '/exhibits/0490e91125a9137ef2d705a7ae746eb5.jpg'
  },
  {
    title: '文创交易场景',
    desc: '展品从授权到交易全链路可视化，观众可直观看到成交图像。',
    image: '/exhibits/d860fed8f6fcf709ba4a4d542e7bea26.jpg'
  },
  {
    title: '修复对比场景',
    desc: '通过同件展品前后图像比对，提升内容叙事和教育价值。',
    image: '/exhibits/5ac89737dfed5148068dc92928cd0f13.jpg'
  }
]

const chainSteps = [
  {
    title: '采集归档',
    desc: '图像/视频/3D统一入库，形成数字资产底座。',
    tag: '采集'
  },
  {
    title: '可信存证',
    desc: '哈希与时间戳上链，确保证据可验可追溯。',
    tag: '上链'
  },
  {
    title: '协作授权',
    desc: '跨馆共享与授权说明同步，提升展陈协作效率。',
    tag: '协作'
  },
  {
    title: '交易追踪',
    desc: '交易图像流可视化，完整呈现流转链路。',
    tag: '交易'
  }
]

const recentTransactions = ref([
  {
    id: 1,
    name: '彩绘神话双耳瓶',
    image: '/exhibits/8962040077ff00fd335806a0f3b4f65a.jpg',
    time: '12分钟前',
    note: '完成二次授权交易，链上确认耗时 1.3s。',
    hash: 'sha256:177f7fd49944aa2192f113caf4e5e8a3d9278e',
    txId: 'tx_5639017288',
    licensor: '上海对外经贸大学博物馆',
    assignee: '城际数字文创联盟'
  },
  {
    id: 2,
    name: '洛可可风瓷钢琴音乐盒',
    image: '/exhibits/148271cf7d8fa34e60f8201bfb01dfac.jpg',
    time: '38分钟前',
    note: '交易凭证已生成，可用于异地联合展。',
    hash: 'sha256:4dcf2be74a93c06bb87ef11d3bf27ac5fbe8cc',
    txId: 'tx_7102458893',
    licensor: '数字展陈版权中心',
    assignee: '临港文旅集团'
  },
  {
    id: 3,
    name: '青金釉葫芦瓶',
    image: '/exhibits/6fe46f53cce0cb23d0aa90936c8f3bb4.jpg',
    time: '1小时前',
    note: '完成权益切分交易，更新收藏人可见。',
    hash: 'sha256:9fe5e2ab8c2b6129ef4d3128a94bde77f16c1a',
    txId: 'tx_2998173401',
    licensor: '馆藏数字化工作室',
    assignee: '浦东公共文化云'
  },
  {
    id: 4,
    name: '黑釉鎏金提梁壶',
    image: '/exhibits/6b84b365fdb2bfb285d529708f797a8b.jpg',
    time: '2小时前',
    note: '完成跨馆授权交易，新增联合展示权益。',
    hash: 'sha256:3a2f4b7d8c1f23aa7c1b27',
    txId: 'tx_4029185566',
    licensor: '华东文博联盟',
    assignee: '虹桥艺术中心'
  },
  {
    id: 5,
    name: '粉彩花卉茶壶套组',
    image: '/exhibits/f580ff0de4447ac727a07cc87c80baf2.jpg',
    time: '昨天',
    note: '生成联名文创交易记录，预计两日内上架。',
    hash: 'sha256:0e71f23d8c0abf77c1a9d4',
    txId: 'tx_1900287744',
    licensor: '古典生活美学馆',
    assignee: '文创孵化中心'
  },
  {
    id: 6,
    name: '新艺术葡萄少女花瓶',
    image: '/exhibits/44c6dedd234af2dade7c7bf2fc3b1383.jpg',
    time: '2天前',
    note: '完成展陈视觉授权交易，支持多馆同步投放。',
    hash: 'sha256:84f0a1c90ad3e2fb4a1d55',
    txId: 'tx_7788332211',
    licensor: '展览运营中心',
    assignee: '城市文化云'
  }
])

const certifiedCount = computed(() => assets.value.filter((item) => item.status === 'certified').length)
const pendingCount = computed(() => assets.value.filter((item) => item.status === 'pending').length)
const totalLoves = computed(() => assets.value.reduce((sum, item) => sum + item.likes, 0))
const collectedAssets = computed(() => assets.value.filter((item) => item.collected))
const certifyRate = computed(() => {
  if (!assets.value.length) return 0
  return Math.round((certifiedCount.value / assets.value.length) * 100)
})

const typeStats = computed(() => {
  const total = assets.value.length || 1
  const items = [
    { key: 'image', label: '图片资产', color: '#1677ff' },
    { key: 'video', label: '视频资产', color: '#13a8a8' },
    { key: '3d', label: '3D资产', color: '#722ed1' },
    { key: 'document', label: '文档资产', color: '#389e0d' }
  ]

  return items
    .map((item) => {
      const count = assets.value.filter((asset) => asset.type === item.key).length
      return {
        label: item.label,
        color: item.color,
        count,
        percent: Math.round((count / total) * 100)
      }
    })
    .filter((item) => item.count > 0)
})

const filteredAssets = computed(() => {
  let result = assets.value
  if (searchText.value) {
    result = result.filter((item) => item.name.includes(searchText.value))
  }
  if (filterType.value) {
    result = result.filter((item) => item.type === filterType.value)
  }
  if (filterStatus.value) {
    result = result.filter((item) => item.status === filterStatus.value)
  }
  return result
})

const handleSearch = () => {
  // 搜索通过计算属性自动生效
}

const viewDetail = (record) => {
  currentAsset.value = record
  showDetailModal.value = true
}

const scrollToTransactions = () => {
  const element = document.getElementById('tx-visual')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const openTxDrawer = (tx) => {
  currentTx.value = tx
  showTxDrawer.value = true
}

const toggleCollect = (record) => {
  record.collected = !record.collected
  record.likes += record.collected ? 1 : -1
  if (record.collected) {
    message.success(`已收藏「${record.name}」`)
    return
  }
  message.info(`已取消收藏「${record.name}」`)
}

const certifyAsset = (record) => {
  message.info(`即将为「${record.name}」进行存证`)
}

const deleteAsset = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除资产「${record.name}」吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk() {
      const index = assets.value.findIndex((item) => item.id === record.id)
      if (index > -1) {
        assets.value.splice(index, 1)
        message.success('删除成功')
      }
    }
  })
}
</script>

<style scoped>
.asset-page {
  position: relative;
  padding: 16px 20px 24px;
  background:
    radial-gradient(1200px 600px at 12% -10%, rgba(18, 108, 122, 0.18), transparent 60%),
    radial-gradient(900px 500px at 88% 0%, rgba(251, 191, 36, 0.18), transparent 55%),
    linear-gradient(180deg, #f7fbff 0%, #f4f6f8 100%);
  color: #0f172a;
  font-family: "HarmonyOS Sans SC", "Noto Sans SC", "Microsoft YaHei", sans-serif;
  isolation: isolate;
}

.asset-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(90deg, rgba(15, 23, 42, 0.05) 1px, transparent 1px),
    linear-gradient(0deg, rgba(15, 23, 42, 0.05) 1px, transparent 1px);
  background-size: 80px 80px;
  opacity: 0.35;
  pointer-events: none;
}

.asset-page::after {
  content: '';
  position: absolute;
  top: -140px;
  right: -120px;
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.2), transparent 70%);
  opacity: 0.6;
  pointer-events: none;
}

.asset-page > * {
  position: relative;
  z-index: 1;
}

.asset-hero {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 24px;
  padding: 28px;
  border-radius: 20px;
  margin-bottom: 16px;
  background:
    radial-gradient(900px 400px at 0% 20%, rgba(255, 255, 255, 0.12), transparent 60%),
    linear-gradient(125deg, #0b1f2a 0%, #0b4f6c 55%, #1f7a8c 100%);
  color: #fff;
  align-items: center;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 24px 50px rgba(2, 6, 23, 0.4);
}

.asset-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(120deg, rgba(255, 255, 255, 0.12) 0%, transparent 45%),
    radial-gradient(circle at 92% 8%, rgba(94, 234, 212, 0.22), transparent 55%);
  opacity: 0.8;
  pointer-events: none;
}

.asset-hero > * {
  position: relative;
  z-index: 1;
}

.asset-hero-copy {
  max-width: 640px;
}

.asset-hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 12px;
  letter-spacing: 0.6px;
  margin-bottom: 14px;
}

.kicker-divider {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #fbbf24;
  box-shadow: 0 0 8px rgba(251, 191, 36, 0.8);
}

.asset-hero-copy h1 {
  margin: 0 0 12px;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 0.5px;
  line-height: 1.2;
  font-family: "Source Han Serif SC", "STSong", "Songti SC", serif;
  text-shadow: 0 12px 30px rgba(2, 6, 23, 0.45);
}

.asset-hero-copy p {
  margin: 0 0 20px;
  max-width: 600px;
  line-height: 1.8;
  opacity: 0.9;
}

.asset-hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.asset-hero-tags span {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.4px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.asset-hero :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #f59e0b, #22c1c3);
  border: none;
  box-shadow: 0 12px 24px rgba(15, 118, 110, 0.35);
}

.asset-hero :deep(.ant-btn-default) {
  border-color: rgba(255, 255, 255, 0.45);
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.asset-hero :deep(.ant-btn-default:hover) {
  border-color: rgba(255, 255, 255, 0.7);
  color: #fff;
}

.asset-hero-visual {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  min-height: 200px;
  height: 240px;
  max-height: 260px;
  width: 100%;
  max-width: 420px;
  justify-self: end;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.26);
}

.asset-hero-visual img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center left;
}

.hero-seal {
  position: absolute;
  right: 14px;
  bottom: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  display: grid;
  gap: 4px;
  backdrop-filter: blur(6px);
}

.hero-seal span {
  font-size: 11px;
  letter-spacing: 0.6px;
  opacity: 0.8;
}

.hero-seal strong {
  font-size: 14px;
  letter-spacing: 0.6px;
}

.hero-seal small {
  font-size: 11px;
  opacity: 0.7;
}

.mb-4 {
  margin-bottom: 16px;
}

.metric-card {
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(160deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
  overflow: hidden;
  position: relative;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: -20px;
  right: -20px;
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, rgba(14, 116, 144, 0.18), transparent 70%);
}

.metric-card :deep(.ant-statistic-title) {
  font-weight: 600;
  color: rgba(15, 23, 42, 0.7);
}

.metric-card :deep(.ant-statistic-content-value) {
  font-weight: 800;
  letter-spacing: 0.4px;
}

.chain-timeline {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.94));
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
}

.chain-timeline::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 24px;
  right: 24px;
  height: 1px;
  background: linear-gradient(90deg, rgba(14, 116, 144, 0.2), rgba(15, 118, 110, 0.5), rgba(14, 116, 144, 0.2));
  opacity: 0.7;
}

.chain-step {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 8px;
  padding: 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  min-height: 120px;
}

.chain-index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5a8, #f59e0b);
  color: #fff;
  font-weight: 700;
  display: grid;
  place-items: center;
  font-size: 13px;
  box-shadow: 0 8px 18px rgba(14, 116, 144, 0.32);
}

.chain-body h4 {
  margin: 0 0 4px;
  font-size: 14px;
  color: #0f172a;
}

.chain-body p {
  margin: 0;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.65);
  line-height: 1.6;
}

.chain-tag {
  align-self: flex-start;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  color: #0f766e;
  background: rgba(14, 116, 144, 0.12);
  border: 1px solid rgba(14, 116, 144, 0.2);
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.scene-card {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  min-height: 200px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  box-shadow: 0 18px 35px rgba(15, 23, 42, 0.12);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.scene-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 24px 40px rgba(15, 23, 42, 0.2);
}

.scene-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scene-mask {
  position: absolute;
  inset: auto 0 0 0;
  padding: 14px;
  color: #fff;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.75) 100%);
}

.scene-mask h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.scene-mask p {
  margin: 0;
  font-size: 12px;
  opacity: 0.92;
}

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  display: block;
}

.filter-card {
  border-radius: 16px;
  border: 1px dashed rgba(15, 23, 42, 0.18);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 252, 0.92));
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.asset-table-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  box-shadow: 0 18px 35px rgba(15, 23, 42, 0.1);
}

.asset-table-card :deep(.ant-card-head) {
  background: linear-gradient(90deg, #0f172a 0%, #0b4f6c 60%, #1f7a8c 100%);
  border-bottom: none;
}

.asset-table-card :deep(.ant-card-head-title) {
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.6px;
}

.asset-table-card :deep(.ant-table) {
  font-size: 13px;
}

.asset-table-card :deep(.ant-table-thead > tr > th) {
  background: #f1f5f9;
  color: rgba(15, 23, 42, 0.75);
  font-weight: 600;
}

.asset-table-card :deep(.ant-table-tbody > tr > td) {
  white-space: nowrap;
}

.asset-table-card :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(14, 116, 144, 0.08);
}

.likes-cell {
  color: #ff4d4f;
  font-weight: 600;
}

.usage-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.usage-total {
  font-weight: 600;
  color: #102a43;
}

.favorite-card {
  min-height: 240px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.1);
  background: #fff;
}

.insight-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.1);
  background: #fff;
}

.insight-item {
  margin-bottom: 12px;
}

.insight-item:last-child {
  margin-bottom: 0;
}

.insight-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  color: rgba(0, 0, 0, 0.7);
}

.insight-row strong {
  color: #102a43;
}

.quick-card {
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.2);
  background: linear-gradient(140deg, #0b1f2a 0%, #0b4f6c 60%, #155e75 100%);
  color: #fff;
  box-shadow: 0 18px 36px rgba(2, 6, 23, 0.35);
}

.quick-card :deep(.ant-card-head-title) {
  color: #fff;
}

.quick-card :deep(.ant-btn) {
  border-color: rgba(255, 255, 255, 0.55);
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
}

.quick-card :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #f59e0b, #22c1c3);
  border: none;
}

.mb-2 {
  margin-bottom: 8px;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.favorite-item {
  display: flex;
  gap: 10px;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px;
  background: #f8fafc;
}

.favorite-item img {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
}

.favorite-item h4 {
  margin: 0 0 4px;
  font-size: 14px;
}

.favorite-item p {
  margin: 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.tx-flow {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tx-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.1);
}

.tx-card :deep(.ant-card-head) {
  background: #f8fafc;
}

.tx-item {
  display: flex;
  gap: 14px;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
}

.tx-item:hover {
  border-color: #91caff;
  box-shadow: 0 8px 20px rgba(5, 145, 255, 0.12);
}

.tx-item img {
  width: 88px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
}

.tx-content {
  flex: 1;
}

.tx-content h4 {
  margin: 0 0 6px;
}

.tx-content p {
  margin: 0 0 8px;
  color: rgba(0, 0, 0, 0.65);
}

.tx-bottom {
  display: flex;
  gap: 12px;
  align-items: center;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.tx-drawer-cover {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
}

.tx-drawer-cover img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.detail-cover {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.detail-cover img {
  width: 100%;
  max-height: 340px;
  object-fit: cover;
}

.text-muted {
  color: rgba(0, 0, 0, 0.45);
}

@media (max-width: 991px) {
  .asset-hero {
    grid-template-columns: 1fr;
  }

  .asset-hero-visual {
    height: 220px;
    max-width: none;
    justify-self: stretch;
  }

  .scene-grid {
    grid-template-columns: 1fr;
  }

  .chain-timeline {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .favorite-card {
    min-height: unset;
  }
}

@media (max-width: 767px) {
  .asset-page {
    padding: 12px;
  }

  .asset-hero {
    padding: 18px;
  }

  .asset-hero-copy h1 {
    font-size: 24px;
  }

  .chain-timeline {
    grid-template-columns: 1fr;
  }

  .tx-item {
    align-items: flex-start;
  }
}
</style>
