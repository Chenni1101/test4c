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

默认使用 H2 内存数据库，方便现场演示；生产环境可切换到 MySQL 或 PostgreSQL。数据库结构由 Flyway 迁移维护。

## 公网答辩部署

Netlify 仅部署 Vue 前端，不能运行本项目的 Java 后端。若答辩现场只打开 Netlify 链接，必须将 Spring Boot 部署到公网并连接 PostgreSQL；否则浏览器会请求答辩电脑自己的 `localhost:8080`，登录和资产发行会失败。

项目已提供 `application-local.yml`（默认 H2）和 `application-prod.yml`（PostgreSQL），以及 Dockerfile。完整的环境变量、Netlify 配置与验收步骤见 [`../docs/deployment.md`](../docs/deployment.md)。生产密钥和数据库密码只允许通过云平台环境变量配置，不能提交到 GitHub。

## 当前安全与演示模式

后端使用 BCrypt 存储密码哈希，并通过 Bearer JWT 进行无状态认证。角色为 `GUEST`、`CREATOR`、`MUSEUM_ADMIN`、`SUPER_ADMIN`：创作者仅能访问本人资产，文博管理员仅能访问本机构资产，超级管理员可访问全平台数据。`/api/health`、`POST /api/auth/register` 和 `POST /api/auth/login` 保持公开。

认证接口：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/auth/me`。登录后将响应中的 `accessToken` 作为 `Authorization: Bearer <token>` 发送。用户列表仅对超级管理员开放，邮箱在列表响应中会脱敏，任何响应均不包含密码哈希。

## 数据库迁移

应用启动时由 Flyway 执行 `src/main/resources/db/migration`。新数据库执行 V1 原型结构、V2 生命周期基础扩展、V3 安全角色种子、V4 兼容修复和 V5 发行追踪；已有原型库会以 V1 为基线，仅执行后续非破坏性迁移。`schema.sql` 已停用但保留为历史参考，不能手动与 Flyway 同时执行。

当前 V2 已新增 `roles`、`user_roles`、`asset_versions` 和 `ipfs_files`，并为 `users`、`assets` 增加后续迁移所需的字段。V3 预置四种系统角色，V4 为 `user_roles.created_at` 补充默认值以兼容安全角色写入。现有存证接口仍使用旧资产字段，版本创建与 IPFS 上传 API 会在下一阶段接入。

本地演示环境会按 `EVIDENCE_BOOTSTRAP_*` 配置创建 `demo-super-admin`；密码仅经 BCrypt 哈希后入库。生产部署必须设置随机的 `EVIDENCE_JWT_SECRET`，并将 `evidence.bootstrap.enabled` 设为 `false`，禁止使用默认演示密码。

## 资产发行（Demo 模式）

`POST /api/assets/issue` 接收资产元数据和 SHA-256 摘要，依次创建资产版本、调用存储网关、调用链网关、保存交易记录并返回资产凭证。请求必须携带 `Idempotency-Key` 与 JWT；`POST /api/assets/issue/batch` 一次最多可处理 10 件。

当前默认使用 `DEMO_LOCAL` 存储和 `DEMO_XUPERCHAIN` 链网关：返回的 `demo-cid-*` 并不是真实 IPFS CID，`demo_tx_*` 也不是真实链上交易。将 `evidence.ipfs.mode` 或 `evidence.chain.mode` 改为 `REAL` 不会自动伪造结果；必须先接入已验证的真实适配器和服务端凭据。

## 答辩讲解口径

这部分建议讲成“后端服务层”，不要说成只有页面演示：

1. Controller 接收前端上传后的元数据和哈希。
2. Service 完成幂等校验、业务入库、合约调用、异常审计。
3. Repository 持久化用户、资产、存证、交易和审计日志。
4. BlockchainGateway 隔离链节点细节，便于把模拟网关替换为百度超级链 SDK/HTTP 网关。
