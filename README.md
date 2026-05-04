# gpt-plus-service

虚拟权益/CDKey 售卖与兑换系统 v2.0。

当前版本是可运行的 Monorepo 工程，包含：

- 后端服务：Spring Boot 3
- 用户端：Vue 3 + Vite + TypeScript
- 管理后台：Vue 3 + Vite + TypeScript
- 开发数据库：H2 文件库
- 生产数据库：PostgreSQL / Render Postgres 配置

## 目录结构

```text
gpt-plus-service
├─ backend
├─ apps
│  ├─ web
│  └─ admin
├─ packages
│  └─ shared
├─ docs
└─ sql
```

## v2.0 核心功能

用户端：

- 用户注册与登录
- 商品列表与商品详情
- 创建订单
- 支持支付宝、微信支付、Mock 支付渠道入口
- 支付创建后展示待支付信息
- 开发模式下可手动模拟支付成功
- 支付成功后自动发放 CDKey
- 查看我的订单、我的 CDKey、兑换记录
- CDKey 兑换并展示交付账号

管理后台：

- 管理员登录
- 仪表盘统计
- 商品新增、编辑、上架、下架
- 库存批量导入
- 库存列表
- 支付配置状态查看
- 订单、支付、CDKey、兑换记录、操作日志查询

后端：

- 统一响应结构
- 基础 Token 鉴权
- Token 密钥配置化
- 支付渠道适配服务
- 支付回调幂等
- 支付单复用
- CDKey 发放幂等
- CDKey 兑换防重复提交
- 库存密码 AES 加密存储
- 库存分配状态保护
- 未支付订单定时关闭
- OpenAPI 文档
- Docker Compose 部署入口

## 本地启动

### 1. 后端

本机需要 JDK 17+。当前工程使用 Spring Boot 3，已在 JDK 17 下完成测试和打包验证。

```powershell
cd backend
$env:JAVA_HOME="D:\javaEnvironment\jdk-17.0.17_windows-x64_bin\jdk-17.0.17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -s maven-settings.xml spring-boot:run
```

访问：

- 健康检查：http://localhost:8080/api/health
- OpenAPI：http://localhost:8080/swagger-ui.html
- H2 控制台：http://localhost:8080/h2-console

### 2. 前端依赖

```powershell
npm install
```

### 3. 用户端

```powershell
npm run dev:web
```

访问：http://localhost:5173

### 4. 管理后台

```powershell
npm run dev:admin
```

访问：http://localhost:5174

首次部署不再内置默认管理员或演示商品。请在目标数据库中创建管理员账号，并通过后台创建正式商品与库存。

## 构建

```powershell
npm run build:web
npm run build:admin

cd backend
mvn -s maven-settings.xml package
```

## 支付说明

v2.0 已提供支付渠道适配服务和配置状态检查：

- `ALIPAY`
- `WECHAT`
- `MOCK`

当前本地开发使用 Mock 成功支付完成主链路联调。真实支付宝、微信支付需要补充商户配置：

- 支付宝：`ALIPAY_APP_ID`、`ALIPAY_PRIVATE_KEY`
- 微信支付：`WECHAT_MCH_ID`、`WECHAT_APP_ID`、`WECHAT_API_V3_KEY`、`WECHAT_NOTIFY_URL`

后台可通过“支付配置”页面查看渠道就绪状态。

## 数据库

- 开发模式默认使用 H2，启动时自动执行 `backend/src/main/resources/schema.sql`；`data.sql` 默认不插入任何种子数据
- PostgreSQL 建表脚本保留在 `sql/init_schema.sql`，后端生产模式也会自动执行 `backend/src/main/resources/schema-postgresql.sql`
- 生产模式使用 `application-prod.yml` 连接 PostgreSQL；Render 可直接使用托管 Postgres 的 `DATABASE_URL`

## Render 部署要点

- 后端可作为 Docker Web Service 部署，根目录 `Dockerfile` 会在 Render 构建阶段打包 Spring Boot 服务。
- Render Web Service 默认期望服务监听 `$PORT`，本项目已支持 `server.port=${PORT:8080}`。
- 用户端和管理端建议分别部署为 Render Static Site，构建命令分别为 `npm install && npm run build:web`、`npm install && npm run build:admin`，发布目录分别为 `apps/web/dist`、`apps/admin/dist`。
- 前端部署时需要设置 `VITE_API_BASE_URL=https://你的后端服务.onrender.com`。
- 后端部署时需要设置 `SPRING_PROFILES_ACTIVE=prod`、`DATABASE_URL` 或 `POSTGRES_JDBC_URL`、`POSTGRES_USERNAME`、`POSTGRES_PASSWORD`、`GPT_PLUS_TOKEN_SECRET`、`GPT_PLUS_INVENTORY_CRYPTO_KEY`。
- 如果前端和后端分不同域名部署，后端需要设置 `GPT_PLUS_CORS_ALLOWED_ORIGINS`，多个域名用英文逗号分隔。

## v2.0 验证结果

已完成验证：

- 用户端构建通过
- 管理后台构建通过
- 后端 JDK 17 + Maven 测试通过
- 后端 JDK 17 + Maven 打包通过
- 后端健康检查通过
- HTTP 主链路冒烟通过：注册 -> 登录 -> 商品 -> 下单 -> 微信支付创建 -> 支付成功 -> CDKey 发放 -> 兑换 -> 后台支付配置查询
