declare module '@/services/assetIssuanceApi' {
  export const issueAsset: (args: any) => Promise<any>
  export const issueAssetsBatch: (args: any) => Promise<any>
  export const retryIssuance: (args: any) => Promise<any>
  export const getIpfsFile: (args: any) => Promise<any>
  export const getChainTransaction: (args: any) => Promise<any>
}

declare module '@/services/authorizationProvenanceApi' {
  export const createAuthorization: (args: any) => Promise<any>
  export const createAuthorizationsBatch: (args: any) => Promise<any>
  export const revokeAuthorization: (args: any) => Promise<any>
  export const listAuthorizations: (args: any) => Promise<any>
  export const createAssetVersion: (args: any) => Promise<any>
  export const listAssetVersions: (args: any) => Promise<any>
  export const getLifecycleTimeline: (args: any) => Promise<any>
  export const traceAsset: (args: any) => Promise<any>
  export const verifyFileHash: (args: any) => Promise<any>
  export const exportProvenanceReport: (args: any) => Promise<any>
}
