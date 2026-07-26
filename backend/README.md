# 文旅数字资产版权存证平台后端

这是答辩展示用的 Spring Boot 后端原型，负责承接前端存证请求、保存业务数据、生成审计日志，并通过 `BlockchainGateway` 对接百度超级链/XuperChain 合约。

## 核心接口

- `POST /api/evidence`：提交存证，保存资产、存证记录、链上交易与审计日志。
- `GET /api/evidence/{hash}`：按文件哈希查询存证结果。
- `GET /api/assets`：查看资产列表。
- `GET /api/health`：健康检查。

## 本地运行

```bash
mvn spring-boot:run
```

默认使用 H2 内存数据库，方便现场演示；生产环境可切换到 MySQL 或 PostgreSQL，并复用 `src/main/resources/schema.sql`。

## 答辩讲解口径

这部分建议讲成“后端服务层”，不要说成只有页面演示：

1. Controller 接收前端上传后的元数据和哈希。
2. Service 完成幂等校验、业务入库、合约调用、异常审计。
3. Repository 持久化用户、资产、存证、交易和审计日志。
4. BlockchainGateway 隔离链节点细节，便于把模拟网关替换为百度超级链 SDK/HTTP 网关。
