export const BORROW_STORAGE_KEY = 'starlink_borrow_records'

export const BORROW_PURPOSES = ['城市巡展', '学术研究', '文创授权', '沉浸展柜', '其他'] as const
export type BorrowPurpose = (typeof BORROW_PURPOSES)[number]
export type BorrowDirection = 'OUT' | 'IN'
export type BorrowStatus = 'ONGOING' | 'COMPLETED' | 'OVERDUE'

export type BorrowRecord = {
  id: string
  recordNo: string
  direction: BorrowDirection
  artifactName: string
  counterparty: string
  purpose: BorrowPurpose
  startDate: string
  endDate: string
  remark?: string
  operatedAt: string
  returnedAt?: string
}

export type BorrowRecordInput = Omit<BorrowRecord, 'id' | 'recordNo' | 'operatedAt' | 'returnedAt'>

const pad = (value: number) => String(value).padStart(2, '0')
const localDate = (date = new Date()) => `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}`
const toMinute = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`

const seedRecords: BorrowRecord[] = [
  { id: 'seed-1', recordNo: 'BRW-20260811-001', direction: 'OUT', artifactName: '新艺术葡萄少女花瓶', counterparty: '国家博物馆', purpose: '城市巡展', startDate: '2026-08-08', endDate: '2026-09-08', remark: '恒温恒湿展柜运输', operatedAt: '2026-08-11 09:30' },
  { id: 'seed-2', recordNo: 'BRW-20260810-001', direction: 'IN', artifactName: '唐三彩马复制数字模型', counterparty: '洛阳博物馆', purpose: '沉浸展柜', startDate: '2026-08-10', endDate: '2026-09-20', remark: '仅用于数字沉浸展示', operatedAt: '2026-08-10 14:20' },
  { id: 'seed-3', recordNo: 'BRW-20260805-001', direction: 'OUT', artifactName: '韦奇伍德蓝陶双耳瓶', counterparty: '故宫博物院', purpose: '学术研究', startDate: '2026-08-05', endDate: '2026-08-09', remark: '研究拍摄已完成', operatedAt: '2026-08-05 10:05', returnedAt: '2026-08-09 16:40' },
  { id: 'seed-4', recordNo: 'BRW-20260801-001', direction: 'IN', artifactName: '青铜礼器数字孪生件', counterparty: '上海博物馆', purpose: '文创授权', startDate: '2026-08-01', endDate: '2026-08-07', operatedAt: '2026-08-01 11:12', returnedAt: '2026-08-07 15:26' },
  { id: 'seed-5', recordNo: 'BRW-20260722-001', direction: 'OUT', artifactName: '彩绘巴洛克执壶', counterparty: '苏州博物馆', purpose: '城市巡展', startDate: '2026-07-22', endDate: '2026-08-05', remark: '待对方办理归还交接', operatedAt: '2026-07-22 09:18' },
  { id: 'seed-6', recordNo: 'BRW-20260718-001', direction: 'IN', artifactName: '宋代花鸟画高精复制件', counterparty: '浙江省博物馆', purpose: '学术研究', startDate: '2026-07-18', endDate: '2026-08-01', operatedAt: '2026-07-18 13:45' }
]

function sortRecords(records: BorrowRecord[]) {
  return [...records].sort((a, b) => b.operatedAt.localeCompare(a.operatedAt))
}

function save(records: BorrowRecord[]) {
  localStorage.setItem(BORROW_STORAGE_KEY, JSON.stringify(sortRecords(records)))
}

export function getBorrowRecords(): BorrowRecord[] {
  try {
    const cached = localStorage.getItem(BORROW_STORAGE_KEY)
    if (cached) {
      const parsed = JSON.parse(cached)
      if (Array.isArray(parsed)) return sortRecords(parsed as BorrowRecord[])
    }
  } catch {
    // 损坏的浏览器缓存将以预置示例记录重新初始化。
  }
  save(seedRecords)
  return sortRecords(seedRecords)
}

export function getBorrowStatus(record: BorrowRecord, now = new Date()): BorrowStatus {
  if (record.returnedAt) return 'COMPLETED'
  const today = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
  return record.endDate < today ? 'OVERDUE' : 'ONGOING'
}

export function createBorrowRecord(input: BorrowRecordInput): BorrowRecord {
  const records = getBorrowRecords()
  const today = localDate()
  const sequence = records
    .filter((record) => record.recordNo.startsWith(`BRW-${today}-`))
    .map((record) => Number(record.recordNo.slice(-3)))
    .filter(Number.isFinite)
    .reduce((max, current) => Math.max(max, current), 0) + 1
  const now = new Date()
  const record: BorrowRecord = {
    ...input,
    id: crypto.randomUUID(),
    recordNo: `BRW-${today}-${String(sequence).padStart(3, '0')}`,
    operatedAt: toMinute(now)
  }
  save([record, ...records])
  return record
}

export function returnBorrowRecord(id: string): BorrowRecord[] {
  const returnedAt = toMinute(new Date())
  const records = getBorrowRecords().map((record) => record.id === id ? { ...record, returnedAt } : record)
  save(records)
  return sortRecords(records)
}

export function isArtifactOnLoan(artifactName: string) {
  return getBorrowRecords().some((record) => record.direction === 'OUT' && record.artifactName === artifactName && getBorrowStatus(record) !== 'COMPLETED')
}
