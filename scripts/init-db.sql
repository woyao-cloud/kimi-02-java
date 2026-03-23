-- =============================================
-- Database Initialization Script
-- 用户管理系统 - 数据库初始化
-- =============================================

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 创建自定义类型
DO $$ BEGIN
    CREATE TYPE user_status AS ENUM ('active', 'inactive', 'locked');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 创建更新时间的触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建审计日志触发器函数
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_logs (action, resource_type, resource_id, old_value, created_at)
        VALUES ('DELETE', TG_TABLE_NAME, OLD.id::text, row_to_json(OLD)::text, CURRENT_TIMESTAMP);
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_logs (action, resource_type, resource_id, old_value, new_value, created_at)
        VALUES ('UPDATE', TG_TABLE_NAME, NEW.id::text, row_to_json(OLD)::text, row_to_json(NEW)::text, CURRENT_TIMESTAMP);
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit_logs (action, resource_type, resource_id, new_value, created_at)
        VALUES ('INSERT', TG_TABLE_NAME, NEW.id::text, row_to_json(NEW)::text, CURRENT_TIMESTAMP);
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 打印初始化完成信息
SELECT 'Database extensions and functions created successfully' AS status;
