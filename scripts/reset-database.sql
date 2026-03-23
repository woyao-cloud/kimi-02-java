-- =============================================
-- Database Reset Script
-- 数据库重置脚本 (谨慎使用!)
-- =============================================

-- 警告: 这个脚本将删除所有数据!
-- 仅在开发环境使用

DO $$
BEGIN
    RAISE NOTICE 'WARNING: This will delete all data! (开发环境专用)';
    RAISE NOTICE 'Press Ctrl+C to cancel, or wait 3 seconds to continue...';
    PERFORM pg_sleep(3);
END $$;

-- 删除所有表数据 (保留表结构)
TRUNCATE TABLE audit_logs CASCADE;
TRUNCATE TABLE login_attempts CASCADE;
TRUNCATE TABLE user_sessions CASCADE;
TRUNCATE TABLE user_roles CASCADE;
TRUNCATE TABLE role_permissions CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE roles CASCADE;
TRUNCATE TABLE permissions CASCADE;

-- 重置序列 (如果有)
-- ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;

SELECT 'All data has been truncated. Run seed-data.sql to repopulate.' AS status;
