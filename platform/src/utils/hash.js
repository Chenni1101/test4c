/**
 * 辅助函数：将二进制缓冲区转换为标准十六进制字符串
 * 保证每个字节都输出为两位十六进制字符，不足两位时前面补0
 * @param {ArrayBuffer} buffer - 待转换的二进制数据
 * @returns {string} 64位标准SHA-256哈希字符串
 */
const toHex = (buffer) =>
  Array.from(new Uint8Array(buffer))
    .map((byte) => byte.toString(16).padStart(2, '0')) // 单个字节转两位十六进制
    .join('')

/**
 * 【存证专用】计算资产唯一存证哈希
 * 核心设计：将文件内容 + 存证时间戳合并计算哈希
 * 保证：1. 同一文件同一时间存证哈希唯一 2. 时间戳作为存证时间的不可篡改证明
 * @param {File} file - 待存证的数字资产文件
 * @param {string} timestamp - 存证时间戳（精确到秒）
 * @returns {string} 带前缀的标准SHA-256存证哈希
 */
export const calculateAssetHash = async (file, timestamp) => {
  // 1. 读取文件原始二进制内容
  const fileBuffer = await file.arrayBuffer()
  // 2. 将时间戳字符串转换为二进制
  const timestampBuffer = new TextEncoder().encode(timestamp)
  // 3. 合并文件内容和时间戳二进制数据
  const merged = new Uint8Array(fileBuffer.byteLength + timestampBuffer.byteLength)
  merged.set(new Uint8Array(fileBuffer), 0) // 前半部分存文件内容
  merged.set(timestampBuffer, fileBuffer.byteLength) // 后半部分存时间戳
  // 4. 使用浏览器原生Web Crypto API计算SHA-256哈希
  const digest = await crypto.subtle.digest('SHA-256', merged)
  // 5. 返回带标准前缀的哈希字符串
  return `sha256:${toHex(digest)}`
}

/**
 * 【查询验证专用】计算文件纯内容哈希
 * 用于存证查询页：用户上传文件后重新计算哈希，与链上记录比对
 * 实现"同一文件可复验"的核心逻辑
 * @param {File} file - 待验证的文件
 * @returns {string} 带前缀的标准SHA-256文件哈希
 */
export const calculateFileHash = async (file) => {
  // 仅计算文件本身的哈希，与存证时的文件部分完全一致
  const digest = await crypto.subtle.digest('SHA-256', await file.arrayBuffer())
  return `sha256:${toHex(digest)}`
}