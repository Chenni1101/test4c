# REST API 设计（v1）

> 基地址：`/api/v1`。本文为目标接口契约，现有原型接口 `/api/evidence`、`/api/assets` 暂保持兼容，后续以弃用公告迁移。所有写接口由服务端认证主体确定操作人，禁止信任 `ownerName` 等客户端身份字段。

当前实施进度：原型接口已先行加入明确的 `DEMO` HTTP Basic 认证、路由 RBAC、`GET /api/auth/me` 和统一 JSON 错误响应；目标契约中的 JWT/OIDC、`/api/v1` 资源路由和持久化用户/角色尚未实现。

## 1. 通用规则

- 认证：除公开展示、公开核验和登录接口外，均要求 `Authorization: Bearer <JWT>`。
- 授权：接口表中的权限为最低角色；还必须通过资源归属/机构范围判断。
- 校验：请求体采用 Bean Validation；UUID、页码、日期范围和枚举值均严格校验。文件摘要必须匹配 `^[a-f0-9]{64}$`（API 可接受并标准化 `sha256:` 前缀）。
- 幂等：`POST /assets/{id}/evidence`、授权签发、撤销和上传完成接口要求 `Idempotency-Key`（8–128 字符）；重复键返回首个已完成/处理中资源。
- 分页：`page` 从 1 开始，`size` 1–100，默认 20；响应为 `{"items":[],"page":1,"size":20,"total":0}`。
- 模式：涉及链、IPFS、AI 的响应含 `mode: "REAL"|"DEMO"` 与 `provider`。`DEMO` 永远不等同于真实外部服务结果。
- 错误：`400 VALIDATION_ERROR`、`401 UNAUTHENTICATED`、`403 FORBIDDEN`、`404 NOT_FOUND`、`409 STATE_CONFLICT|IDEMPOTENCY_CONFLICT`、`422 BUSINESS_RULE_VIOLATION`、`502 PROVIDER_ERROR`。均返回 `traceId`。

## 2. 认证、用户与角色

| 方法与路径 | 最低权限 | 用途 | 关键请求/响应 |
|---|---|---|---|
| `POST /auth/login` | Public | 登录并获取短期 access token/刷新令牌策略。 | `{username,password}` → `{accessToken, expiresIn, user}`；失败不泄露账户是否存在。 |
| `POST /auth/refresh` | Refresh token | 刷新访问令牌。 | 刷新令牌建议 HttpOnly SameSite cookie。 |
| `POST /auth/logout` | 登录用户 | 失效刷新会话并审计。 | 无敏感响应。 |
| `GET /me` | 登录用户 | 当前用户、角色及能力集。 | `{id,displayName,roles,permissions}`。 |
| `GET /users` | ADMIN | 分页查用户。 | 支持 `status`,`organization`,`q`。 |
| `POST /users` | ADMIN | 创建/邀请用户。 | 密码不回显；审计 `USER_CREATE`。 |
| `PATCH /users/{userId}` | ADMIN/本人受限 | 更新展示信息、状态。 | 禁止通过本接口升级角色。 |
| `PUT /users/{userId}/roles` | ADMIN | 替换角色集合。 | `{roleCodes:["CURATOR"]}`。 |
| `GET /roles` | ADMIN | 读取角色与权限字典。 | 系统角色只读。 |

## 3. 资产与版本

| 方法与路径 | 最低权限 | 用途 | 关键规则 |
|---|---|---|---|
| `GET /assets` | CURATOR/RIGHTS_OWNER/REVIEWER | 分页资产列表。 | 资源范围过滤；支持 `status,type,ownerId,visibility,q,page,size,sort`。 |
| `POST /assets` | CURATOR/RIGHTS_OWNER | 创建逻辑资产。 | `{title,assetType,creatorName,organizationName?,description?,metadata?,visibility}`；初始 `DRAFT`。 |
| `GET /assets/{assetId}` | 有资源查看权 | 资产详情、当前版本摘要与授权摘要。 | 受限字段按权限脱敏。 |
| `PATCH /assets/{assetId}` | 所有者/CURATOR | 更新 DRAFT 元数据。 | 已存证版本的元数据不得被追溯覆盖；重要修改应创建版本。 |
| `DELETE /assets/{assetId}` | 所有者/ADMIN | 软删除未存证草稿。 | 已有证据记录返回 `409`，不能删除历史。 |
| `GET /assets/{assetId}/versions` | 有资源查看权 | 版本列表。 | 可按 `status` 过滤。 |
| `POST /assets/{assetId}/versions` | 所有者/CURATOR | 创建新草稿版本元数据并申请上传。 | `{originalFilename,mimeType,fileSizeBytes,changeNote?,metadataSnapshot?}`。 |
| `GET /assets/{assetId}/versions/{versionId}` | 有资源查看权 | 版本详情、存储和存证摘要。 | 不直接返回私有下载地址。 |
| `POST /assets/{assetId}/versions/{versionId}/upload-sessions` | 所有者/CURATOR | 申请受限直传/分片上传会话。 | 校验文件大小、MIME、配额；返回临时上传策略。 |
| `POST /upload-sessions/{sessionId}/complete` | 会话所有者 | 完成上传并触发服务端校验、IPFS 固定任务。 | 请求 `{contentSha256}`；服务端二次验证大小/摘要。 |

上传、固定和哈希计算均应提供页面加载态与失败重试；`storageStatus=FAILED` 不允许提交存证。

## 4. 存证、链交易与核验

| 方法与路径 | 最低权限 | 用途 | 关键规则 |
|---|---|---|---|
| `POST /assets/{assetId}/versions/{versionId}/evidence` | 所有者/CURATOR | 提交存证。 | Header `Idempotency-Key`；请求 `{ownerDeclaration?, expiresAt?}`；创建后返回 `202` 与 `PENDING_CHAIN`。 |
| `GET /evidence/{evidenceId}` | 有资源查看权 | 查询存证详情。 | 返回证据摘要、模式、交易状态与撤销信息。 |
| `GET /assets/{assetId}/evidence` | 有资源查看权 | 查看该资产历史存证。 | 按版本/状态分页。 |
| `GET /chain-transactions/{transactionId}` | 所有者/REVIEWER/ADMIN | 查询单次提交/确认状态。 | 错误消息脱敏，不返回请求秘密。 |
| `POST /chain-transactions/{transactionId}/retry` | 所有者/CURATOR/ADMIN | 对可重试失败项发起新尝试。 | 仅 `FAILED`/`TIMEOUT`；产生新记录并关联 parent。 |
| `GET /public/verify` | Public | 按 `contentSha256`、`evidenceNo` 或 `txHash` 核验。 | 只返回公开/可披露字段，并含 `mode` 与 `verificationResult`。 |
| `POST /public/verify/file` | Public（限流） | 上传文件进行哈希后核验。 | 不长期留存文件；大小/MIME/病毒扫描限制；返回摘要与核验结果。 |
| `POST /evidence/{evidenceId}/revoke` | 权利人/ADMIN | 撤销证据对外可用性。 | Header 幂等键、`{reason, basisRef?}`；保留所有历史。 |

示例：提交存证成功受理（不代表链已确认）。

```json
{
  "id": "b5bb7a9e-7cc7-4d0a-92bd-3edba3828116",
  "evidenceNo": "EVD-20260726-0001",
  "assetVersionId": "d7c852e8-ec32-4ca1-9353-4bd37d8c3a6a",
  "status": "PENDING",
  "assetStatus": "PENDING_CHAIN",
  "mode": "DEMO",
  "provider": "demo-chain",
  "transactionId": "962dba79-3bdd-4ed8-a72d-91bc3e3aa681",
  "submittedAt": "2026-07-26T09:20:00Z"
}
```

## 5. IPFS 存储

| 方法与路径 | 最低权限 | 用途 | 关键规则 |
|---|---|---|---|
| `GET /asset-versions/{versionId}/files` | 有资源查看权 | 查看版本存储对象和固定状态。 | CID/网关地址按可见性脱敏。 |
| `POST /asset-versions/{versionId}/files/{fileId}/verify` | 所有者/CURATOR/REVIEWER | 重新读取并核验 CID 内容摘要。 | 真实模式才访问 IPFS；Demo 仅返回模拟校验。 |
| `POST /asset-versions/{versionId}/files/{fileId}/pin-retry` | 所有者/CURATOR/ADMIN | 重试失败固定。 | 限制重试次数并写审计。 |
| `GET /asset-versions/{versionId}/download` | 有资源查看权 | 获取短时下载重定向/流。 | 后端鉴权后签发短期 URL，不暴露长期私有网关。 |

## 6. 授权闭环

| 方法与路径 | 最低权限 | 用途 | 关键规则 |
|---|---|---|---|
| `GET /authorizations` | RIGHTS_OWNER/LICENSEE/REVIEWER | 分页查询授权。 | 服务端按主体范围过滤；支持 `assetId,status,licenseeId,activeAt`。 |
| `POST /authorizations` | RIGHTS_OWNER/CURATOR | 创建授权草稿。 | `{assetId,assetVersionId?,licenseeUserId?,licenseeName,licenseType,usageScope,startsAt,endsAt,contractRef?}`。 |
| `GET /authorizations/{authorizationId}` | 授权双方/REVIEWER/ADMIN | 查询授权详情及适用范围。 | 掩码不必要联系信息。 |
| `POST /authorizations/{authorizationId}/issue` | 权利人/授权签发人 | 签发草稿。 | Header 幂等键；校验证据已认证、授权期和独占冲突。 |
| `POST /authorizations/{authorizationId}/revoke` | 授权方/ADMIN | 撤销有效授权。 | Header 幂等键，`{reason}`；不能删除历史。 |
| `GET /assets/{assetId}/authorization-check` | 有资源查看权 | 以用途检查可用授权。 | 参数 `versionId, purpose, territory, at`；返回最小授权结论。 |

定时任务每天处理 `ends_at < now()` 的授权并标为 `EXPIRED`，同时重新计算资产展示状态；查询接口也需做惰性校验，避免任务延迟造成错误展示。

## 7. 审计、运营与健康

| 方法与路径 | 最低权限 | 用途 | 关键规则 |
|---|---|---|---|
| `GET /audit-logs` | ADMIN/REVIEWER（限范围） | 检索审计事件。 | 支持 `actorId,assetId,action,outcome,from,to,page,size`；不可编辑。 |
| `GET /assets/{assetId}/timeline` | 有资源查看权 | 聚合版本、存证、授权、撤销时间线。 | 仅呈现可见事件。 |
| `GET /system/mode` | 登录用户 | 获取当前服务模式及功能可用性。 | `{mode,chain:{provider,available},storage:{provider,available}}`。 |
| `GET /health` | 受控运维/平台探针 | 存活健康检查。 | 不泄露凭据、数据库 URL、节点地址。 |
| `GET /metrics` | 运维网络/ADMIN | 指标采集。 | 生产环境使用网络隔离与认证。 |

## 8. 前端对接约束

1. 建立单一 `src/services/api/`（axios 实例、鉴权拦截器、错误映射）与按领域的 TypeScript DTO；业务页面不得直接读写 `localStorage` 作为可信数据源。
2. 每一个异步调用至少有 `loading`、成功反馈、错误提示（展示 `traceId`）和空数据占位；路由守卫根据 `/me` 的权限集控制页面访问。
3. Demo 数据只能通过 `GET /system/mode` 或接口响应的 `mode=DEMO` 展示，并在页面显示“演示/模拟数据，非真实链上/IPFS/AI 结果”。
4. 前端不导入链 SDK、不持有节点或私钥配置；核验与下载始终经后端授权。

## 9. 当前已实现：资产发行 API（原型兼容路径）

以下接口当前位于 `/api`，在后续统一迁移到 `/api/v1` 前保持兼容。均要求 JWT Bearer 认证；发行角色为 `CREATOR`、`MUSEUM_ADMIN` 或 `SUPER_ADMIN`，并额外执行资产所有者/机构范围校验。

| 方法与路径 | 用途 | 关键约束 |
|---|---|---|
| `POST /api/assets/issue` | 发行一件资产：接收 SHA-256 与元数据、存储、上链、返回凭证。 | `Idempotency-Key` 必填；请求中的 `contentSha256` 可带 `sha256:` 前缀。 |
| `POST /api/assets/issue/batch` | 批量发行资产。 | 单次最多 10 件；批次幂等键会派生为每件资产的幂等键。 |
| `POST /api/assets/issuances/{transactionId}/retry` | 重试失败的链交易。 | 只允许资产所有者、本机构管理员或超级管理员；不会伪造成功回执。 |
| `GET /api/assets/ipfs/{cid}` | 查询已记录的存储对象。 | Demo 记录会返回 `mode=DEMO` 与“非真实 IPFS CID”提示。 |
| `GET /api/assets/chain-transactions/{transactionId}` | 查询链交易详情。 | 返回网关、模式、状态、错误与回执；Demo 回执明确标记。 |

单件发行的成功响应包含 `assetCode`、版本 ID、CID、存储提供商、交易 ID 和状态。只有 `mode=REAL` 且外部网关返回已确认回执时，结果才可被称为真实 IPFS/真实链上结果；当前默认 `DEMO` 模式返回 `demo-cid-*` 与 `demo_tx_*`，两者都不是外部服务的真实凭据。

## 10. 当前已实现：资产授权、版本与溯源 API（原型兼容路径）

以下接口均位于 `/api`，要求 JWT Bearer 认证。所有资源读取和写入均在服务端执行 CREATOR 所有者、MUSEUM_ADMIN 同机构、SUPER_ADMIN 全平台的范围判断；GUEST 不可操作。

| 方法与路径 | 用途 | 关键约束 |
|---|---|---|
| `POST /authorizations` | 创建并签发一条授权。 | `assetCode`、被授权方、`COMMERCIAL`/`NON_COMMERCIAL`、分润比例、开始/结束时间必填；仅已认证版本可授权。 |
| `POST /authorizations/batch` | 批量签发授权。 | 单次最多 10 条；每条独立校验并产生独立历史。 |
| `POST /authorizations/{authorizationId}/revoke` | 提前撤销有效授权。 | 请求 `{reason}`；仅资产归属方/同机构管理员/超级管理员可撤销；不删除原记录。 |
| `GET /authorizations?assetCode=...` | 查看某资产授权历史。 | 查询时惰性处理到期；每日 00:05 也会自动将过期授权标为 `EXPIRED`。 |
| `POST /assets/{assetCode}/versions` | 创建新版本和存储记录。 | 接收摘要与元数据；不覆盖旧版本哈希/CID；当前 Demo 存储会明确返回 `demo-cid-*`。 |
| `GET /assets/{assetCode}/versions` | 查询资产全部版本。 | 返回版本号、哈希、存储状态和 CID 摘要。 |
| `GET /assets/{assetCode}/timeline` | 资产完整生命周期时间轴。 | 聚合资产、版本、IPFS、存证、链交易、授权与状态。 |
| `GET /provenance/trace` | 按资产编号、哈希、CID 或交易 ID 溯源。 | 至少传一个查询参数；多个参数必须指向同一资产。 |
| `POST /provenance/verify-file` | 重新计算上传文件 SHA-256 并比对。 | `multipart/form-data`：`file`、`expectedHash`；文件只在请求流中处理，不由接口保存。 |
| `GET /assets/{assetCode}/provenance-report` | 下载 JSON 溯源报告。 | 响应带下载文件名；报告会包含 `mode` 与非法律确权声明。 |

授权状态为 `DRAFT`、`ACTIVE`、`EXPIRED`、`REVOKED`。同一资产版本、同一被授权方、相同用途/商用属性且有效期重叠的重复授权会返回 `409 STATE_CONFLICT`。授权记录、撤销原因和过期状态均只更新历史状态，不提供删除接口。

`DEMO` 模式下，授权的链记录会使用 `demo_auth_tx_*`，响应和报告会显式标识 Demo/Mock；它不是可验证的真实链上交易，也不单独构成法律意义上的版权确权。`REAL` 网关尚未接入可验证 SDK/凭据时会明确失败，不会伪造回执。
