# 答辩公网部署指南

## 目标架构

```text
答辩电脑浏览器
      │
      ▼
Netlify（Vue 前端） ── HTTPS ──> Spring Boot 公网服务 ──> PostgreSQL
                                      │
                                      └── Demo IPFS / Demo 链网关（明确标识）
```

Netlify 只托管前端静态文件，不能运行 Java 后端，也不能保存业务数据。若前端仍使用默认的 `http://localhost:8080/api`，答辩电脑会请求它自己的本机服务，注册、登录和发行资产都会失败。

## 1. 准备 PostgreSQL 数据库

在你选择的云数据库服务创建一个 PostgreSQL 数据库，并保存：主机、端口、数据库名、用户名和密码。项目使用 Flyway；后端首次连上空数据库时会自动执行 `backend/src/main/resources/db/migration` 中的建表迁移。

将连接信息转换为 JDBC 格式：

```text
jdbc:postgresql://HOST:5432/DATABASE?sslmode=require
```

不要把数据库密码写入 GitHub、`application-prod.yml` 或前端环境变量。

## 2. 部署 Spring Boot 后端

将仓库推送到 GitHub 后，在任意支持 Docker 的 Java 云服务创建一个服务，部署目录选择 `backend`。仓库已提供 `backend/Dockerfile`。

在云服务的 Environment Variables 中按 `backend/.env.production.example` 设置：

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`
- `EVIDENCE_JWT_SECRET`：至少 32 字节的随机密钥
- `EVIDENCE_CORS_ALLOWED_ORIGIN`：Netlify 的准确 HTTPS 站点地址，例如 `https://xxx.netlify.app`
- 首次需要创建管理员时，临时设 `EVIDENCE_BOOTSTRAP_ENABLED=true`，并设置强密码；首次登录验证后改回 `false` 并重新部署。

后端启动成功后，访问：

```text
https://你的后端域名/api/health
```

应得到 HTTP 200。不要把 `DEMO` 链/IPFS 结果讲成真实 IPFS 或真实链上交易。

## 3. 配置并重新部署 Netlify 前端

在 Netlify 的 **Site configuration → Environment variables** 新增：

```text
VITE_API_BASE_URL=https://你的后端域名/api
```

保存后执行 **Deploys → Trigger deploy → Deploy site**。Vite 会在构建时读取该变量，因此只修改变量不会自动改变已部署的前端包。

## 4. 答辩前验收

用手机流量或另一台电脑打开 Netlify 链接，按以下顺序操作：

1. 注册一个创作者账户（密码至少 12 位，且同时含字母和数字）。
2. 登录，确认刷新页面后仍保持登录状态。
3. 发行一件资产，确认返回的模式为 `DEMO` 时页面有明确说明。
4. 打开“我的资产”、授权管理和全链路核验，确认数据仍存在。
5. 在云服务日志中确认没有 CORS、数据库连接或 Flyway 迁移错误。

如果浏览器报 CORS 错误，先核对 `EVIDENCE_CORS_ALLOWED_ORIGIN` 是否与浏览器地址完全一致（协议、域名均要一致），然后重新部署后端。

## 本地继续开发

不设置 `SPRING_PROFILES_ACTIVE` 时会自动采用 `local` 配置，仍使用 H2 内存数据库；后端重启后数据会清空。公网环境必须设置 `SPRING_PROFILES_ACTIVE=prod` 并使用 PostgreSQL。
