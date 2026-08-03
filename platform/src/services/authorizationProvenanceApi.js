import client from './apiClient'

const headers = () => ({})

export const createAuthorization = ({ accessToken, authorization }) =>
  client.post('/authorizations', authorization, { headers: headers(accessToken) })

export const createAuthorizationsBatch = ({ accessToken, authorizations }) =>
  client.post('/authorizations/batch', { authorizations }, { headers: headers(accessToken) })

export const revokeAuthorization = ({ accessToken, authorizationId, reason }) =>
  client.post(`/authorizations/${encodeURIComponent(authorizationId)}/revoke`, { reason }, { headers: headers(accessToken) })

export const listAuthorizations = ({ accessToken, assetCode }) =>
  client.get('/authorizations', { params: { assetCode }, headers: headers(accessToken) })

export const createAssetVersion = ({ accessToken, assetCode, version }) =>
  client.post(`/assets/${encodeURIComponent(assetCode)}/versions`, version, { headers: headers(accessToken) })

export const listAssetVersions = ({ accessToken, assetCode }) =>
  client.get(`/assets/${encodeURIComponent(assetCode)}/versions`, { headers: headers(accessToken) })

export const getLifecycleTimeline = ({ accessToken, assetCode }) =>
  client.get(`/assets/${encodeURIComponent(assetCode)}/timeline`, { headers: headers(accessToken) })

export const traceAsset = ({ accessToken, ...params }) =>
  client.get('/provenance/trace', { params, headers: headers(accessToken) })

export const verifyFileHash = ({ accessToken, file, expectedHash }) => {
  const form = new FormData()
  form.append('file', file)
  form.append('expectedHash', expectedHash)
  return client.post('/provenance/verify-file', form, { headers: headers(accessToken) })
}

export const exportProvenanceReport = ({ assetCode }) =>
  client.get(`/assets/${encodeURIComponent(assetCode)}/provenance-report`, { responseType: 'blob' })
