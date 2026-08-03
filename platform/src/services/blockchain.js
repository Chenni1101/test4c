// 浏览器不得连接链节点或持有密钥。链上操作统一经后端 API 完成。
export default {
  async storeFile() { throw new Error('前端已禁用直接链节点调用，请使用后端资产发行 API') },
  async queryFile() { throw new Error('前端已禁用直接链节点调用，请使用后端溯源 API') }
}
