# Docker 本地开发环境配置

## 概述

使用 Docker Compose 配置本地开发环境，包含：
- **PostgreSQL 15** - 主数据库
- **PostgreSQL 15** - 测试数据库 (可选)
- **Redis 7** - 缓存与会话存储
- **pgAdmin 4** - 数据库管理工具 (可选)
- **Redis Commander** - Redis 管理界面 (可选)
- **Spring Boot 后端** (可选)
- **Next.js 前端** (可选)

## 快速开始

### 1. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 根据需要修改 .env 文件
```

### 2. 启动基础设施 (推荐)

```bash
# 仅启动数据库和缓存
docker-compose up -d postgres redis
```

### 3. 启动开发环境 (包含管理工具)

```bash
# 启动数据库 + pgAdmin + Redis Commander
docker-compose --profile dev up -d
```

### 4. 启动完整环境 (前后端 + 数据库)

```bash
# 启动所有服务
docker-compose --profile full up -d
```

## 服务访问信息

| 服务 | 地址 | 端口 | 凭证 |
|------|------|------|------|
| PostgreSQL | localhost | 5432 | postgres/postgres |
| PostgreSQL (测试) | localhost | 5433 | postgres/postgres |
| Redis | localhost | 6379 | 无密码 |
| pgAdmin 4 | http://localhost:5050 | 5050 | admin@example.com/admin |
| Redis Commander | http://localhost:8081 | 8081 | - |
| 后端 API | http://localhost:8080/api | 8080 | - |
| 前端应用 | http://localhost:3000 | 3000 | - |

## 数据库初始化

### 自动执行的脚本

首次启动 PostgreSQL 时，会自动执行以下脚本：

1. **`scripts/init-db.sql`** - 数据库初始化
   - 创建 UUID 扩展 (uuid-ossp)
   - 创建加密扩展 (pgcrypto)
   - 创建自定义类型 (user_status)
   - 创建触发器函数 (update_updated_at_column, audit_trigger_func)

2. **`scripts/seed-data.sql`** - 初始数据填充
   - 创建 12 个权限 (user:read/create/update/delete, role:read/create/update/delete, permission:read/create/update/delete)
   - 创建 4 个角色 (ADMIN, USER, MANAGER, GUEST)
   - 分配权限给角色
   - 创建管理员用户 (admin/admin123)
   - 创建测试用户 (testuser/user123, manager/manager123)
   - 创建 5 个演示用户

### 预置用户账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 所有权限 |
| testuser | user123 | USER | user:read, user:update, role:read |
| manager | manager123 | MANAGER | user:read, user:create, user:update, role:read |

## 手动执行脚本

### 生成测试数据

```bash
# 生成 10,000 个随机用户、登录记录和审计日志
docker-compose exec -T postgres psql -U postgres -d usermanagement < scripts/generate-test-data.sql
```

### 重置数据库

```bash
# 清空所有数据（保留表结构）
docker-compose exec -T postgres psql -U postgres -d usermanagement < scripts/reset-database.sql

# 重新填充初始数据
docker-compose exec -T postgres psql -U postgres -d usermanagement < scripts/seed-data.sql
```

### 备份和恢复

```bash
# 备份数据库
docker-compose exec postgres pg_dump -U postgres usermanagement > backup.sql

# 恢复数据库
docker-compose exec -T postgres psql -U postgres -d usermanagement < backup.sql
```

## 常用命令

### 查看服务状态

```bash
# 查看运行状态
docker-compose ps

# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f backend
```

### 停止和重启

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎使用！）
docker-compose down -v

# 重启服务
docker-compose restart

# 重启特定服务
docker-compose restart postgres
```

### 数据库操作

```bash
# 进入 PostgreSQL 命令行
docker-compose exec postgres psql -U postgres -d usermanagement

# 查看表
docker-compose exec postgres psql -U postgres -d usermanagement -c "\dt"

# 查看用户数量
docker-compose exec postgres psql -U postgres -d usermanagement -c "SELECT COUNT(*) FROM users;"
```

### Redis 操作

```bash
# 进入 Redis CLI
docker-compose exec redis redis-cli

# 查看 Redis 信息
docker-compose exec redis redis-cli INFO

# 清除所有数据
docker-compose exec redis redis-cli FLUSHALL

# 查看所有键
docker-compose exec redis redis-cli KEYS '*'
```

## 后端开发配置

### 使用 Docker 基础设施 + 本地后端

1. 启动基础设施：
```bash
docker-compose up -d postgres redis
```

2. 配置后端环境变量 (backend/.env 或 application-dev.yml):
```yaml
DB_HOST=localhost
DB_PORT=5432
DB_NAME=usermanagement
DB_USERNAME=postgres
DB_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
```

3. 启动本地后端：
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 数据持久化

数据通过 Docker volumes 持久化：

| Volume | 说明 |
|--------|------|
| postgres_data | PostgreSQL 主数据库数据 |
| postgres_test_data | PostgreSQL 测试数据库数据 |
| redis_data | Redis 数据 |
| pgadmin_data | pgAdmin 配置数据 |

查看 volumes：
```bash
docker volume ls
docker volume inspect usermanagement_postgres_data
```

## 故障排除

### 端口冲突

如果端口被占用，修改 `.env` 文件：
```bash
POSTGRES_PORT=5433
REDIS_PORT=6380
BACKEND_PORT=8081
FRONTEND_PORT=3001
PGADMIN_PORT=5051
```

### 数据库初始化失败

删除 volume 并重新启动：
```bash
docker-compose down -v
docker-compose up -d postgres redis
```

### 连接问题

检查服务健康状态：
```bash
docker-compose ps
docker-compose logs postgres
```

### 权限问题 (Linux/Mac)

如果遇到权限错误，可能需要修改脚本文件权限：
```bash
chmod +x scripts/*.sql
```

## 生产环境部署注意事项

1. **修改默认密码**: 修改所有默认密码
2. **JWT 密钥**: 生成新的 JWT 密钥
3. **Redis 密码**: 启用 Redis 密码认证
4. **SSL/TLS**: 启用 HTTPS
5. **防火墙**: 限制端口访问
6. **日志**: 配置日志收集和分析
7. **监控**: 添加健康检查和监控

## 多环境配置

### 开发环境
```bash
docker-compose --profile dev up -d
```

### 生产环境
```bash
# 修改 .env 文件
SPRING_PROFILE=prod
POSTGRES_PASSWORD=<strong-password>
REDIS_PASSWORD=<redis-password>
JWT_SECRET=<generate-new-secret>

# 启动
docker-compose --profile full up -d
```
