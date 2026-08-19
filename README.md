# 企业资产全生命周期管理平台

Enterprise Asset Lifecycle Management Platform

面向集团型企业和大型组织的资产管理系统，覆盖资产从申请到退出的完整业务链路：

```text
资产申请 → 审批 → 采购 → 验收入库 → 领用 → 调拨 → 归还 → 维修 → 报废
```

这不是对管理后台的简单换皮。本项目以 RuoYi-Vue-Plus 为企业级基础设施，独立建设资产领域的数据模型、业务规则、审批流程、权限、接口、页面和项目文档。

## 当前进度

- 已建立独立后端业务模块 `ruoyi-asset`
- 已实现资产分类和资产台账 CRUD、参数校验及分层结构
- 已实现采购、领用、调拨、归还、报废五类资产申请
- 已接入 WarmFlow 审批主链路、待办与审批记录
- 已接入租户、角色、菜单、按钮和数据权限体系
- 已实现资产分类、资产台账、我的申请、审批办理等前端页面
- 已提供数据库初始化 SQL、环境变量模板和持续维护文档

## 仓库结构

```text
enterprise-asset-lifecycle-platform/
├── backend/       # Spring Boot 后端（RuoYi-Vue-Plus 二次开发）
├── frontend/      # Vue 3 前端（plus-ui 二次开发）
├── docs/          # 项目统一文档入口
├── LICENSE
├── NOTICE.md
└── README.md
```

## 技术栈

- 后端：Java、Spring Boot、MyBatis-Plus、Sa-Token、WarmFlow、Redis
- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia
- 数据库：MySQL
- 工程：Maven、pnpm

## 本地运行

### 1. 初始化数据库

先完成 RuoYi-Vue-Plus 基础数据库初始化，再执行资产模块 SQL：

```text
backend/script/sql/asset/asset_init.sql
```

### 2. 启动后端

```bash
cd backend
cp .env.example .env
# 编辑 .env，填写本机数据库和 Redis 凭据
./script/start-dev-with-env.sh
```

后端默认地址：`http://localhost:8080`

### 3. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

前端默认地址：`http://localhost:5173`

本项目不会提交 `.env`。所有真实密码、令牌和第三方密钥只应保存在本机环境变量或密钥管理服务中。

## 文档

从 [项目文档索引](docs/README.md) 开始阅读。设计与实施记录目前保存在 `backend/docs/`，便于与对应后端版本同步演进。

## 开源来源

本项目是基于开源框架进行的二次开发，并非从零编写：

- 后端：[Dromara / RuoYi-Vue-Plus](https://github.com/dromara/RuoYi-Vue-Plus)
- 前端：[JavaLionLi / plus-ui](https://github.com/JavaLionLi/plus-ui)

原项目采用 MIT License。仓库保留了上游许可证和版权声明，详细说明见 [NOTICE.md](NOTICE.md)。
