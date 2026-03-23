# 数据库脚本说明

## 脚本列表

### 1. init-db.sql
**用途**: 数据库初始化脚本
**执行时机**: PostgreSQL 容器首次启动时自动执行

**包含内容**:
- 创建 UUID 扩展 (uuid-ossp)
- 创建加密扩展 (pgcrypto)
- 创建自定义枚举类型
- 创建更新时间触发器函数
- 创建审计日志触发器函数

### 2. seed-data.sql
**用途**: 初始数据填充
**执行时机**: PostgreSQL 容器首次启动时，在 init-db.sql 之后执行

**包含内容**:
- 创建权限数据 (user:read/create/update/delete, role:read/create/update/delete, permission:read/create/update/delete)
- 创建角色数据 (ADMIN, USER, MANAGER, GUEST)
- 分配权限给角色
- 创建管理员用户 (admin/admin123)
- 创建测试用户 (testuser/user123, manager/manager123)
- 创建演示用户 (5个随机用户)

### 3. redis.conf
**用途**: Redis 配置文件
**包含内容**:
- 网络绑定配置
- 持久化配置 (AOF)
- 内存管理配置
- 日志配置

## 默认用户账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 系统管理员 |
| testuser | user123 | USER | 普通测试用户 |
| manager | manager123 | MANAGER | 经理测试用户 |

## 权限列表

### 用户权限
- `user:read` - 查看用户
- `user:create` - 创建用户
- `user:update` - 更新用户
- `user:delete` - 删除用户

### 角色权限
- `role:read` - 查看角色
- `role:create` - 创建角色
- `role:update` - 更新角色
- `role:delete` - 删除角色

### 权限管理
- `permission:read` - 查看权限
- `permission:create` - 创建权限
- `permission:update` - 更新权限
- `permission:delete` - 删除权限

## 角色权限分配

- **ADMIN**: 所有权限
- **USER**: user:read, user:update, role:read
- **MANAGER**: user:read, user:create, user:update, role:read
- **GUEST**: user:read, role:read
