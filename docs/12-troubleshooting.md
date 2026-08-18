# 部署与排障记录

## 1. 2026-08-19 第一阶段实际改动

### 后端

- 新建独立 Maven 模块 `ruoyi-modules/ruoyi-asset`，并由 `ruoyi-admin` 聚合。
- 新增资产分类与资产台账 Entity、BO、VO、Mapper、Service、Controller。
- 加入租户隔离、接口权限、操作日志、重复提交保护和部门/本人数据权限。
- 加入编码唯一、分类循环、分类引用、日期范围和资产删除状态等业务校验。
- 新增 MySQL 初始化脚本 `script/sql/asset/asset_init.sql`。

### 前端

- 前端仓库位于后端同级目录 `../plus-ui`。
- 新增 `src/api/asset/category`、`src/api/asset/info`。
- 新增 `src/views/asset/category/index.vue`、`src/views/asset/info/index.vue`。
- 页面复用现有部门树、用户选择器和按钮权限指令。

### 验证

- `mvn -pl ruoyi-modules/ruoyi-asset -am -DskipTests compile`：通过。
- `mvn -pl ruoyi-admin -am -DskipTests package`：33 个模块完整打包通过。
- `pnpm build:prod`：通过，Vite 共转换 3160 个模块。
- MySQL 初始化结果：2 张业务表、3 个初始分类、11 条菜单/按钮权限。
- 浏览器检查：前端登录页正常渲染；后端和前端地址均返回 HTTP 200。
- 原项目根目录 `LICENSE` 已保留。

## 2. 本次遇到的问题与解决方案

### 问题 A：Maven 报 ruoyi-asset 缺少版本

原因：新模块已加入 reactor，但没有加入根 POM 的 `dependencyManagement`，`ruoyi-admin` 引用时无法取得版本。

解决：在根 POM 中为 `org.dromara:ruoyi-asset` 声明 `${revision}`，保持与其他业务模块一致。

### 问题 B：Maven 首次验证无法写入本机依赖缓存

原因：受限执行环境不能更新用户目录下的 Maven 缓存，不是项目源码错误。

解决：取得授权后允许 Maven 正常访问本机缓存并下载缺失依赖，资产模块随后编译通过。

### 问题 C：前端构建报 Node.js 版本过低

现象：本机默认 Node.js 为 16.14.0，Vite 7 要求 Node.js 20.19+ 或 22.12+，并出现 `node:fs/promises` 导出不兼容错误。

解决：通过本机 NVM 安装 Node.js 22.22.0，并在该版本下执行生产构建，构建通过。

```bash
source ~/.nvm/nvm.sh
nvm use 22.22.0
pnpm build:prod
```

### 问题 D：接手时服务未运行

检查结果：后端 8080、前端常用端口均未监听；代码、依赖和本地 MySQL/Redis 数据目录已存在。后续启动时应先确认 MySQL 和 Redis，再启动后端和前端。

处理结果：已启动项目专用 MySQL（3307）、Redis（6379）、后端（8080）和前端（5173）。MySQL 在受限会话内直接后台化时出现内存映射权限错误，改为授权后启动本机进程解决；应用自身不存在该错误。

## 3. 常用检查

```bash
# 后端编译
mvn -pl ruoyi-admin -am -DskipTests package

# 前端构建
source ~/.nvm/nvm.sh
nvm use 22.22.0
pnpm build:prod
```

若登录后看不到“企业资产管理”菜单，先确认已执行资产初始化 SQL，并确认当前角色拥有对应菜单；超级管理员重新登录即可刷新动态路由。

若新增资产提示分类已停用，请在“资产分类”中启用对应分类。若资产不能删除，请确认其状态为“库存”；其他状态应通过后续生命周期业务单据流转。
