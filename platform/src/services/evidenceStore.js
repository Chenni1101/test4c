const STORAGE_KEY = 'evidence.records.v1'

const clone = (data) => JSON.parse(JSON.stringify(data))

const defaultRecords = [
  {
    assetName: '新艺术葡萄少女花瓶',
    assetType: 'image',
    creator: '博物馆数字化团队',
    organization: '上海对外经贸大学博物馆',
    hash: 'sha256:9a24fe5d1ac6b7efb0f4',
    uniqueId: 'H_9a24fe5d1ac6b7ef',
    certifyTime: '2026-03-21 10:30:45',
    blockHeight: 5234567,
    txId: 'tx_8273612091',
    cid: 'bafybeigdyrztxw-demo-001',
    chainNetwork: '百度超级链开放测试网络',
    contractName: 'eleccert'
  },
  {
    assetName: '韦奇伍德蓝陶双耳瓶',
    assetType: '3d',
    creator: '3D建模团队',
    organization: '上海对外经贸大学博物馆',
    hash: 'sha256:5be90c82df3c7ca0f89b',
    uniqueId: 'H_5be90c82df3c7ca0',
    certifyTime: '2026-03-19 14:20:30',
    blockHeight: 5234123,
    txId: 'tx_9283746510',
    cid: 'bafybeigdyrztxw-demo-002',
    chainNetwork: '百度超级链开放测试网络',
    contractName: 'eleccert'
  }
]

const readRecords = () => {
  if (typeof window === 'undefined' || !window.localStorage) {
    return clone(defaultRecords)
  }
  try {
    const stored = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || '[]')
    return [...clone(defaultRecords), ...stored]
  } catch (error) {
    return clone(defaultRecords)
  }
}

const writeUserRecords = (records) => {
  if (typeof window === 'undefined' || !window.localStorage) return
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(records))
}

const listEvidenceRecords = () => readRecords()

const findEvidenceRecord = (query) => {
  const text = String(query || '').trim().toLowerCase()
  if (!text) return null
  return readRecords().find((record) => {
    return (
      record.hash.toLowerCase() === text ||
      record.txId.toLowerCase() === text ||
      record.hash.toLowerCase().includes(text) ||
      record.txId.toLowerCase().includes(text)
    )
  })
}

const saveEvidenceRecord = (record) => {
  if (typeof window === 'undefined' || !window.localStorage) return record
  const userRecords = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || '[]')
  const nextRecords = [record, ...userRecords.filter((item) => item.hash !== record.hash)]
  writeUserRecords(nextRecords)
  return record
}

export { listEvidenceRecords, findEvidenceRecord, saveEvidenceRecord }
