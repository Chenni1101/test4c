import client, { requestIdempotencyKey } from './apiClient'

const bearerHeaders = (_accessToken, extra = {}) => extra

const issueAsset = ({ accessToken, idempotencyKey, asset }) =>
  client.post('/assets/issue', asset, {
    headers: bearerHeaders(accessToken, { 'Idempotency-Key': idempotencyKey || requestIdempotencyKey('issue') })
  })

const issueAssetsBatch = ({ accessToken, idempotencyKey, assets }) =>
  client.post('/assets/issue/batch', { assets }, {
    headers: bearerHeaders(accessToken, { 'Idempotency-Key': idempotencyKey })
  })

const retryIssuance = ({ accessToken, transactionId }) =>
  client.post(`/assets/issuances/${transactionId}/retry`, null, {
    headers: bearerHeaders(accessToken)
  })

const getIpfsFile = ({ accessToken, cid }) =>
  client.get(`/assets/ipfs/${encodeURIComponent(cid)}`, { headers: bearerHeaders(accessToken) })

const getChainTransaction = ({ accessToken, transactionId }) =>
  client.get(`/assets/chain-transactions/${transactionId}`, { headers: bearerHeaders(accessToken) })

export { issueAsset, issueAssetsBatch, retryIssuance, getIpfsFile, getChainTransaction }
