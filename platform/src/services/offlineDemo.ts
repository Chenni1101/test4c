const STORAGE_KEY = 'evidence.offline-demo-state.v1'
const RESPONSE_DELAY_MS = 120

export const isOfflineDemoMode = import.meta.env.VITE_OFFLINE_DEMO !== 'false'

type TimelineEvent = {
  type: string
  status: string
  reference: string
  detail: string
  occurredAt: string
}

type DemoAsset = {
  assetCode: string
  assetName: string
  assetType: string
  creator: string
  organization?: string
  description?: string
  keywords?: string[]
  contentSha256: string
  originalFilename: string
  mimeType: string
  fileSizeBytes: number
  cid: string
  chainTxId: string
  chainTransactionId: number
  blockHeight: number
  createdAt: string
  status: string
  mode: 'DEMO'
  versions: any[]
  timeline: TimelineEvent[]
  authorizations: any[]
}

type DemoState = { sequence: number; assets: DemoAsset[] }

function defaultState(): DemoState {
  return { sequence: 1, assets: [] }
}

function loadState(): DemoState {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved ? JSON.parse(saved) : defaultState()
  } catch {
    return defaultState()
  }
}

function saveState(state: DemoState) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function response<T>(data: T, delay = RESPONSE_DELAY_MS): Promise<{ data: T }> {
  return new Promise((resolve) => window.setTimeout(() => resolve({ data }), delay))
}

function makeCode(prefix: string, sequence: number) {
  return `${prefix}-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(sequence).padStart(4, '0')}`
}

function findAsset(state: DemoState, assetCode: string) {
  const asset = state.assets.find((item) => item.assetCode === assetCode)
  if (!asset) throw new Error('未找到对应的演示资产')
  return asset
}

function publicAsset(asset: DemoAsset) {
  return {
    assetCode: asset.assetCode,
    assetName: asset.assetName,
    assetType: asset.assetType,
    creator: asset.creator,
    organization: asset.organization,
    fileHash: asset.contentSha256,
    contentSha256: asset.contentSha256,
    cid: asset.cid,
    uniqueId: asset.assetCode,
    txId: asset.chainTxId,
    chainTxId: asset.chainTxId,
    chainTransactionId: asset.chainTransactionId,
    chainStatus: 'CONFIRMED',
    blockHeight: asset.blockHeight,
    chainNetwork: '现场离线演示网络',
    contractName: 'evidence-demo-adapter',
    certifyTime: asset.createdAt,
    status: asset.status,
    mode: asset.mode
  }
}

export function issueOfflineAsset(payload: any) {
  const state = loadState()
  const sequence = state.sequence++
  const now = new Date().toISOString()
  const assetCode = makeCode('AST-DEMO', sequence)
  const cid = `demo-cid-${payload.contentSha256.replace(/^sha256:/, '').slice(0, 24)}`
  const chainTxId = `demo_tx_${payload.contentSha256.replace(/^sha256:/, '').slice(0, 16)}_${sequence}`
  const chainTransactionId = 100000 + sequence
  const blockHeight = 5200000 + sequence
  const timeline: TimelineEvent[] = [
    { type: 'ASSET_CREATED', status: 'SUCCESS', reference: assetCode, detail: '资产元数据已登记，操作写入本地演示审计记录。', occurredAt: now },
    { type: 'VERSION_CREATED', status: 'CERTIFIED', reference: 'VERSION-1', detail: `版本 1 已固化，内容摘要为 ${payload.contentSha256}。`, occurredAt: now },
    { type: 'STORAGE_RECORDED', status: 'PINNED', reference: cid, detail: '离线演示存储适配器已返回确定性定位标识。', occurredAt: now },
    { type: 'CHAIN_CONFIRMED', status: 'CONFIRMED', reference: chainTxId, detail: '现场演示网关已生成流程回执；该回执不代表真实公链交易。', occurredAt: now }
  ]
  const asset: DemoAsset = {
    ...payload,
    assetCode,
    cid,
    chainTxId,
    chainTransactionId,
    blockHeight,
    createdAt: now,
    status: 'CERTIFIED',
    mode: 'DEMO',
    versions: [{ id: `${assetCode}-v1`, versionNo: 1, contentSha256: payload.contentSha256, cid, status: 'CERTIFIED' }],
    timeline,
    authorizations: []
  }
  state.assets.unshift(asset)
  saveState(state)
  return response(publicAsset(asset), 260)
}

export function listOfflineAssets() {
  return response(loadState().assets.map(publicAsset))
}

export function getOfflineEvidenceByHash(hash: string) {
  const normalized = hash.toLowerCase().replace(/^sha256:/, '')
  const asset = loadState().assets.find((item) => item.contentSha256.toLowerCase().replace(/^sha256:/, '') === normalized)
  if (!asset) return Promise.reject(Object.assign(new Error('未找到存证记录'), { status: 404 }))
  return response(publicAsset(asset))
}

export function getOfflineChainTransaction(transactionId: number) {
  const asset = loadState().assets.find((item) => item.chainTransactionId === Number(transactionId))
  if (!asset) return Promise.reject(new Error('未找到演示交易记录'))
  return response({ id: asset.chainTransactionId, txHash: asset.chainTxId, blockHeight: asset.blockHeight, status: 'CONFIRMED', mode: 'DEMO' })
}

export function getOfflineIpfsFile(cid: string) {
  const asset = loadState().assets.find((item) => item.cid === cid)
  if (!asset) return Promise.reject(new Error('未找到演示存储记录'))
  return response({ cid, provider: 'OFFLINE_DEMO_ADAPTER', pinStatus: 'PINNED', mode: 'DEMO', contentSha256: asset.contentSha256 })
}

export function listOfflineVersions(assetCode: string) {
  return response(findAsset(loadState(), assetCode).versions)
}

export function getOfflineTimeline(assetCode: string) {
  const asset = findAsset(loadState(), assetCode)
  return response({ assetCode, timeline: asset.timeline })
}

export function listOfflineAuthorizations(assetCode: string) {
  return response(findAsset(loadState(), assetCode).authorizations)
}

export function createOfflineAuthorization(payload: any) {
  const state = loadState()
  const asset = findAsset(state, payload.assetCode)
  const now = new Date().toISOString()
  const authorization = {
    id: `${asset.assetCode}-auth-${asset.authorizations.length + 1}`,
    authorizationNo: makeCode('AUTH-DEMO', state.sequence++),
    ...payload,
    status: 'ACTIVE',
    createdAt: now
  }
  asset.authorizations.unshift(authorization)
  asset.status = 'AUTHORIZED'
  asset.timeline.push({
    type: 'AUTHORIZATION_CREATED',
    status: 'ACTIVE',
    reference: authorization.authorizationNo,
    detail: `已向“${payload.licenseeName}”签发${payload.usageType === 'NON_COMMERCIAL' ? '非商业' : '商业'}演示授权。`,
    occurredAt: now
  })
  saveState(state)
  return response(authorization, 220)
}

export function revokeOfflineAuthorization(authorizationId: string, reason: string) {
  const state = loadState()
  const asset = state.assets.find((item) => item.authorizations.some((authorization) => authorization.id === authorizationId))
  if (!asset) return Promise.reject(new Error('未找到演示授权记录'))
  const authorization = asset.authorizations.find((item) => item.id === authorizationId)
  authorization.status = 'REVOKED'
  authorization.revokedAt = new Date().toISOString()
  asset.status = asset.authorizations.some((item) => item.status === 'ACTIVE') ? 'AUTHORIZED' : 'CERTIFIED'
  asset.timeline.push({ type: 'AUTHORIZATION_REVOKED', status: 'REVOKED', reference: authorization.authorizationNo, detail: reason, occurredAt: authorization.revokedAt })
  saveState(state)
  return response(authorization)
}

export function traceOfflineAsset(params: any) {
  const state = loadState()
  const hash = String(params.hash || '').toLowerCase().replace(/^sha256:/, '')
  const asset = state.assets.find((item) => {
    if (params.assetCode && item.assetCode !== params.assetCode) return false
    if (hash && item.contentSha256.toLowerCase().replace(/^sha256:/, '') !== hash) return false
    if (params.cid && item.cid !== params.cid) return false
    if (params.transactionId && item.chainTransactionId !== Number(params.transactionId)) return false
    return Boolean(params.assetCode || hash || params.cid || params.transactionId)
  })
  if (!asset) return Promise.reject(new Error('未找到满足全部条件的演示资产'))
  return response({ assetCode: asset.assetCode, mode: 'DEMO', timeline: asset.timeline })
}

export async function verifyOfflineFile(file: File, expectedHash: string) {
  const digest = await crypto.subtle.digest('SHA-256', await file.arrayBuffer())
  const computed = Array.from(new Uint8Array(digest)).map((byte) => byte.toString(16).padStart(2, '0')).join('')
  const expected = expectedHash.toLowerCase().replace(/^sha256:/, '')
  return response({ matches: computed === expected, computedSha256: `sha256:${computed}`, expectedSha256: `sha256:${expected}`, mode: 'DEMO' })
}

export function exportOfflineReport(assetCode: string) {
  const asset = findAsset(loadState(), assetCode)
  return response(new Blob([JSON.stringify({ ...publicAsset(asset), timeline: asset.timeline, authorizations: asset.authorizations }, null, 2)], { type: 'application/json' }))
}
