# 自动化测试与质量门禁

## 目标

资产平台的质量验证聚焦于个人二次开发范围，保证权限边界、业务规则和前端资产页面可以重复验证。通用后台能力仍来自 RuoYi-Vue-Plus 与 plus-ui，上游历史代码不计入个人测试成果。

## 后端测试

资产模块包含 3 个测试类、21 个测试用例：

- `AssetApplicationServiceImplTest`：本人读取、他人拒绝、审批任务授权、伪造任务拒绝、管理员审计读取、金额重算、重复资产、状态边界。
- `AssetCategoryServiceImplTest`：编码唯一、自身父节点、上级存在性、分类环路、下级分类及已用分类删除限制。
- `AssetInfoServiceImplTest`：编码唯一、分类有效性、停用分类、保修日期、库存删除边界。

执行方式：

```bash
cd backend
mvn -pl ruoyi-modules/ruoyi-asset -am -DskipTests=false test
```

测试报告生成在：

```text
ruoyi-modules/ruoyi-asset/target/surefire-reports/
```

## 前端验证

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm exec eslint src/views/asset src/api/asset
pnpm typecheck:asset
pnpm build:prod
```

仓库提交 `pnpm-lock.yaml`，并通过 `.nvmrc` 和 `packageManager` 固定 Node.js 22.23.2、pnpm 11.19.0。

`typecheck:asset` 检查自研资产页面、接口以及它们实际依赖的公共组件。全仓其他模块仍保留 plus-ui 上游类型兼容问题，不将其包装为个人已完成工作；生产构建继续覆盖整个前端工程。

## GitHub Actions

`.github/workflows/quality-gate.yml` 在推送和 PR 时自动执行：

1. Java 17 全模块编译；
2. 资产领域测试；
3. 前端锁文件安装；
4. 资产域 ESLint 与类型检查；
5. 全量 production build。

任一环节失败都会阻止质量门禁通过，确保主分支上的演示版本可重复构建。
