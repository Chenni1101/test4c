import client from './apiClient'
import { getOfflineEvidenceByHash, isOfflineDemoMode, listOfflineAssets } from './offlineDemo'

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

export const listMyAssets = () => isOfflineDemoMode ? listOfflineAssets() : client.get<EvidenceAsset[]>('/assets')
export const getEvidenceByHash = (hash: string) => isOfflineDemoMode ? getOfflineEvidenceByHash(hash) : client.get<EvidenceAsset>(`/evidence/${encodeURIComponent(hash)}`)
