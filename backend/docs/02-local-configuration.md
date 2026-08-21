# 本地配置与密钥管理

## 1. 原则

- 数据库密码、Redis 密码、短信密钥和第三方登录密钥只保存在仓库根目录 `.env`。
- `.env` 已加入 `.gitignore`，不得提交；公开仓库只提交 `.env.example`。
- `application-dev.yml` 和 `application-prod.yml` 只引用环境变量，不保存真实凭据。
- `VITE_*` 变量会进入浏览器构建产物，不能保存 API 密钥、数据库密码或服务端私钥。

## 2. 初始化

```bash
cp .env.example .env
```

至少填写：

```text
MYSQL_HOST（本机一般为 localhost）
MYSQL_PORT（标准端口为 3306，本项目现有本地实例为 3307）
MYSQL_DATABASE（默认 ry-vue）
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_PASSWORD
SA_TOKEN_JWT_SECRET
```

短信和第三方登录变量只在启用对应集成时填写。

## 3. 启动

先完成后端打包，再使用加载环境变量的启动脚本：

```bash
mvn -pl ruoyi-admin -am -DskipTests package
./script/start-dev-with-env.sh
```

生产环境不要上传 `.env`，应通过部署平台、容器 Secret 或系统环境变量注入同名变量。

`SA_TOKEN_JWT_SECRET` 不提供默认值。请使用密码管理器生成足够长的随机值；缺失时后端应拒绝启动，避免误用公开默认密钥。

## 4. 前端说明

前端现有 RSA 配置来自上游框架，并会随 JavaScript 下载到浏览器，不能视为秘密。它只能用于请求传输层的兼容逻辑，真正的第三方 API 调用和密钥必须放在后端。
