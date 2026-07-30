# LLDCard

LLDCard 是一个基于 Vue 3、Vite、Spring Boot、MySQL 和 Redis 的卡密管理系统。

> 二次开发声明：本项目基于 [xiaoxiaoguai-yyds/xxgkami-pro](https://github.com/xiaoxiaoguai-yyds/xxgkami-pro) 进行二次开发，感谢原作者与上游贡献者公开源代码。LLDCard 的维护仓库为 [lmqvq/LLDCard](https://github.com/lmqvq/LLDCard)。

## 主要功能

- 管理员、普通用户与登录验证
- 卡密创建、核销、机器码绑定和状态管理
- API Key、订单、定价、通知与维护管理
- MySQL 持久化、Redis 分布式锁与 Token 状态管理
- Docker Compose 一键启动前端、后端、MySQL 和 Redis

## Docker 部署

### 1. 准备配置

```bash
cp .env.example .env
```

编辑 `.env`，至少替换以下 3 个密码：

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `LLDCARD_ADMIN_PASSWORD`

密码不得使用示例值。首次管理员密码至少需要 12 个字符。

### 2. 构建并启动

```bash
docker compose up -d --build
```

默认访问地址：`http://localhost:8080`。前端通过内部网络访问后端，后端端口不会直接暴露到宿主机。

### 3. 检查状态

```bash
docker compose ps
docker compose logs -f backend
```

后端健康检查地址为 `http://localhost:8080/api/health`。

### 4. 停止或清理

```bash
docker compose down
```

数据库、Redis 数据、运行时密钥、日志和备份保存在 Docker named volumes 中。仅在确定不再需要数据时执行：

```bash
docker compose down -v
```

更多说明见 [deployment/README.md](deployment/README.md)。

## 本地开发

前置环境：Node.js 20.19+ 或 22.12+、JDK 21、Maven 3.8+、MySQL 8.0+、Redis 7+。

```bash
npm ci
npm run dev
```

```bash
cd backend
mvn spring-boot:run
```

后端配置支持环境变量覆盖，关键变量包括：

| 变量 | 用途 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC 连接地址 |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 |
| `SPRING_DATA_REDIS_HOST` | Redis 主机 |
| `LLDCARD_BOOTSTRAP_ADMIN_USERNAME` | 首次管理员用户名 |
| `LLDCARD_BOOTSTRAP_ADMIN_PASSWORD` | 首次管理员密码 |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源，多个值用逗号分隔 |

## 安全说明

- 仓库不会提交 `.env`、运行时密钥、数据库备份或原始 SQL 导出。
- `backend/keys` 中的密钥会在首次启动时自动生成，请持久化并妥善备份。
- Docker 初始化脚本只包含表结构和通用默认设置，不包含上游导出中的管理员、用户、卡密、Session 或支付凭据。
- 生产环境应配置 HTTPS、限制管理端访问来源，并定期备份 MySQL 与 `backend-keys` volume。

## 来源与许可证状态

本仓库保留上游来源与原作者版权信息，详见 [NOTICE.md](NOTICE.md)。截至 2026-07-31，上游仓库未随附可被 GitHub 识别的 `LICENSE` 文件，历史文档中还存在 MIT、Apache-2.0 与 `All rights reserved` 等互相冲突的表述。本仓库不替上游代码重新授权；使用、修改或再分发前，请向上游确认授权条件。