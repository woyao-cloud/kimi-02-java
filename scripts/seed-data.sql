-- =============================================
-- Database Seed Data Script
-- 用户管理系统 - 初始数据
-- =============================================

-- 注意: 这个脚本在 Flyway 迁移之后运行，用于添加额外的测试/演示数据
-- 核心表结构由 Flyway 在 V1__create_tables.sql 中创建

-- =============================================
-- 1. 创建权限 (如果 Flyway 未创建)
-- =============================================
INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'user:read', 'user', 'read', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'user:read');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'user:create', 'user', 'create', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'user:create');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'user:update', 'user', 'update', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'user:update');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'user:delete', 'user', 'delete', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'user:delete');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'role:read', 'role', 'read', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'role:read');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'role:create', 'role', 'create', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'role:create');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'role:update', 'role', 'update', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'role:update');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'role:delete', 'role', 'delete', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'role:delete');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'permission:read', 'permission', 'read', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'permission:read');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'permission:create', 'permission', 'create', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'permission:create');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'permission:update', 'permission', 'update', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'permission:update');

INSERT INTO permissions (id, name, resource, action, created_at)
SELECT uuid_generate_v4(), 'permission:delete', 'permission', 'delete', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'permission:delete');

-- =============================================
-- 2. 创建角色 (如果 Flyway 未创建)
-- =============================================
INSERT INTO roles (id, name, description, is_default, created_at)
SELECT uuid_generate_v4(), 'ADMIN', '系统管理员，拥有所有权限', false, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (id, name, description, is_default, created_at)
SELECT uuid_generate_v4(), 'USER', '普通用户，拥有基本权限', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

INSERT INTO roles (id, name, description, is_default, created_at)
SELECT uuid_generate_v4(), 'MANAGER', '经理，拥有管理权限', false, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'MANAGER');

INSERT INTO roles (id, name, description, is_default, created_at)
SELECT uuid_generate_v4(), 'GUEST', '访客，只读权限', false, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'GUEST');

-- =============================================
-- 3. 分配权限给角色
-- =============================================

-- ADMIN 拥有所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- USER 拥有基本读权限和个人信息更新权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER'
AND p.name IN ('user:read', 'user:update', 'role:read')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- MANAGER 拥有用户管理权限和角色读取权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MANAGER'
AND p.name IN ('user:read', 'user:create', 'user:update', 'role:read')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- GUEST 只有读取权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'GUEST'
AND p.name IN ('user:read', 'role:read')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- =============================================
-- 4. 创建管理员用户
-- =============================================
DO $$
DECLARE
    admin_role_id UUID;
    admin_user_id UUID := 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
BEGIN
    -- 获取 ADMIN 角色ID
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN';

    -- 创建管理员用户 (密码: admin123)
    INSERT INTO users (id, username, email, password_hash, first_name, last_name,
                       is_active, is_verified, last_login_at, created_at, updated_at)
    VALUES (
        admin_user_id,
        'admin',
        'admin@example.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- bcrypt: admin123
        'System',
        'Administrator',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (username) DO NOTHING;

    -- 分配 ADMIN 角色给管理员用户
    IF admin_role_id IS NOT NULL THEN
        INSERT INTO user_roles (user_id, role_id)
        VALUES (admin_user_id, admin_role_id)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- =============================================
-- 5. 创建测试用户
-- =============================================
DO $$
DECLARE
    user_role_id UUID;
    manager_role_id UUID;
    test_user_id UUID := 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22';
    manager_user_id UUID := 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33';
BEGIN
    -- 获取角色ID
    SELECT id INTO user_role_id FROM roles WHERE name = 'USER';
    SELECT id INTO manager_role_id FROM roles WHERE name = 'MANAGER';

    -- 创建普通测试用户 (密码: user123)
    INSERT INTO users (id, username, email, password_hash, first_name, last_name,
                       is_active, is_verified, created_at, updated_at)
    VALUES (
        test_user_id,
        'testuser',
        'testuser@example.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- bcrypt: user123
        'Test',
        'User',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (username) DO NOTHING;

    -- 创建经理测试用户 (密码: manager123)
    INSERT INTO users (id, username, email, password_hash, first_name, last_name,
                       is_active, is_verified, created_at, updated_at)
    VALUES (
        manager_user_id,
        'manager',
        'manager@example.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- bcrypt: manager123
        'Test',
        'Manager',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (username) DO NOTHING;

    -- 分配角色
    IF user_role_id IS NOT NULL THEN
        INSERT INTO user_roles (user_id, role_id)
        VALUES (test_user_id, user_role_id)
        ON CONFLICT DO NOTHING;
    END IF;

    IF manager_role_id IS NOT NULL THEN
        INSERT INTO user_roles (user_id, role_id)
        VALUES (manager_user_id, manager_role_id)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- =============================================
-- 6. 创建演示数据 (可选)
-- =============================================
DO $$
DECLARE
    user_role_id UUID;
    i INT;
BEGIN
    SELECT id INTO user_role_id FROM roles WHERE name = 'USER';

    -- 创建 5 个随机用户用于测试
    FOR i IN 1..5 LOOP
        INSERT INTO users (id, username, email, password_hash, first_name, last_name,
                           is_active, is_verified, created_at, updated_at)
        VALUES (
            uuid_generate_v4(),
            'demo_user_' || i,
            'demo' || i || '@example.com',
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',
            'Demo',
            'User ' || i,
            true,
            true,
            CURRENT_TIMESTAMP - (random() * interval '30 days'),
            CURRENT_TIMESTAMP
        )
        ON CONFLICT (username) DO NOTHING;
    END LOOP;
END $$;

-- =============================================
-- 7. 验证数据
-- =============================================
SELECT 'Seed data inserted successfully' AS status;

-- 显示统计信息
SELECT 'Users count: ' || COUNT(*)::text AS info FROM users
UNION ALL
SELECT 'Roles count: ' || COUNT(*)::text FROM roles
UNION ALL
SELECT 'Permissions count: ' || COUNT(*)::text FROM permissions
UNION ALL
SELECT 'User-Role assignments: ' || COUNT(*)::text FROM user_roles
UNION ALL
SELECT 'Role-Permission assignments: ' || COUNT(*)::text FROM role_permissions;
