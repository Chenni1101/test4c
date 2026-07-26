import { XuperClient } from '@xuperchain/xuper-sdk';

class BlockchainService {
  constructor() {
    // 连接百度超级链开放测试网络（如图8所示）
    this.client = new XuperClient({
      node: 'http://localhost:5173',  // 本地节点地址
      chain: 'xuper'
    });
    this.contractName = 'eleccert';  // 存证合约名称
  }

  // 文件存证
  async storeFile(fileHash, owner, timestamp) {
    try {
      const args = {
        owner: owner,
        filehash: fileHash,
        timestamp: timestamp.toString()
      };
      
      // 调用智能合约的save方法
      const result = await this.client.invoke({
        contractName: this.contractName,
        methodName: 'save',
        args: args
      });
      
      return result;
    } catch (error) {
      console.error('存证失败:', error);
      throw error;
    }
  }

  // 查询存证
  async queryFile(owner, fileHash) {
    try {
      const args = {
        owner: owner,
        filehash: fileHash
      };
      
      const result = await this.client.query({
        contractName: this.contractName,
        methodName: 'query',
        args: args
      });
      
      return result;
    } catch (error) {
      console.error('查询失败:', error);
      return null;
    }
  }
}

export default new BlockchainService();