# Docker 部署说明

LLDCard 的 Compose 拓扑包含 4 个服务：

| 服务 | 作用 | 对外端口 |
| --- | --- | --- |
| frontend | Nginx 托管前端并反向代理 /api | FRONTEND_PORT，默认 8080 |
| backend | Spring Boot API | 仅容器网络 |
| mysql | 业务数据 | 仅容器网络 |
| redis | 分布式锁和 Token 状态 | 仅容器网络 |

## 首次部署

~~~bash
cp .env.example .env
# 编辑 .env 并设置强密码
docker compose up -d --build
docker compose ps
~~~

MySQL 初始化脚本仅在 mysql-data volume 首次创建时执行。修改初始化 SQL 不会自动影响已有数据库。

## 更新

~~~bash
git pull --ff-only
docker compose up -d --build
~~~

## 备份

应用内备份写入 backend-backups volume。建议同时使用数据库级备份，并在升级前验证恢复流程。

## 回滚

回滚代码后重新构建即可，数据 volume 不会被删除：

~~~bash
git checkout <previous-commit>
docker compose up -d --build
~~~

数据库结构发生变化时，应在回滚前确认向后兼容性。不要在未备份时执行 docker compose down -v。