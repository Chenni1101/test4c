import client from './apiClient'

export type EvidenceAsset = {
  assetCode?: string
  assetName: string
  assetType: string
  creator: string
  organization?: string
  fileHash: string
  cid?: string
  uniqueId: string
  txId: string
  blockHeight: number
  chainNetwork: string
  contractName: string
  certifyTime: string
  status: string
  mode: 'DEMO' | 'REAL'
}

export const listMyAssets = () => client.get<EvidenceAsset[]>('/assets')
export const getEvidenceByHash = (hash: string) => client.get<EvidenceAsset>(`/evidence/${encodeURIComponent(hash)}`)
