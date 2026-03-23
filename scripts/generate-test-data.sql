-- =============================================
-- Generate Test Data Script
-- 生成大量测试数据用于性能测试和演示
-- =============================================

-- 设置随机种子
SELECT setseed(0.5);

DO $$
DECLARE
    user_role_id UUID;
    manager_role_id UUID;
    admin_role_id UUID;
    guest_role_id UUID;
    i INT;
    batch_size INT := 1000;
    total_users INT := 10000;
    first_names TEXT[] := ARRAY['James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda', 'William', 'Elizabeth',
                                'David', 'Barbara', 'Richard', 'Susan', 'Joseph', 'Jessica', 'Thomas', 'Sarah', 'Charles', 'Karen',
                                'Christopher', 'Nancy', 'Daniel', 'Lisa', 'Matthew', 'Betty', 'Anthony', 'Margaret', 'Mark', 'Sandra'];
    last_names TEXT[] := ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez',
                               'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin',
                               'Lee', 'Perez', 'Thompson', 'White', 'Harris', 'Sanchez', 'Clark', 'Ramirez', 'Lewis', 'Robinson'];
    domains TEXT[] := ARRAY['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'example.com', 'company.com'];
    random_first TEXT;
    random_last TEXT;
    random_username TEXT;
    random_email TEXT;
    random_domain TEXT;
    user_id UUID;
BEGIN
    -- 获取角色ID
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN';
    SELECT id INTO user_role_id FROM roles WHERE name = 'USER';
    SELECT id INTO manager_role_id FROM roles WHERE name = 'MANAGER';
    SELECT id INTO guest_role_id FROM roles WHERE name = 'GUEST';

    RAISE NOTICE 'Starting to generate % users...', total_users;

    -- 批量生成用户
    FOR i IN 1..total_users LOOP
        random_first := first_names[1 + (random() * 29)::INT];
        random_last := last_names[1 + (random() * 29)::INT];
        random_domain := domains[1 + (random() * 5)::INT];
        random_username := lower(random_first) || '_' || lower(random_last) || '_' || i;
        random_email := lower(random_first) || '.' || lower(random_last) || i || '@' || random_domain;
        user_id := uuid_generate_v4();

        -- 插入用户
        INSERT INTO users (id, username, email, password_hash, first_name, last_name,
                           is_active, is_verified, created_at, updated_at)
        VALUES (
            user_id,
            random_username,
            random_email,
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', -- 默认密码: password
            random_first,
            random_last,
            random() > 0.1,  -- 90% 活跃用户
            random() > 0.2,  -- 80% 已验证用户
            CURRENT_TIMESTAMP - (random() * interval '365 days'),
            CURRENT_TIMESTAMP
        )
        ON CONFLICT (username) DO NOTHING;

        -- 分配随机角色
        IF random() < 0.7 AND user_role_id IS NOT NULL THEN
            INSERT INTO user_roles (user_id, role_id)
            VALUES (user_id, user_role_id)
            ON CONFLICT DO NOTHING;
        ELSIF random() < 0.2 AND manager_role_id IS NOT NULL THEN
            INSERT INTO user_roles (user_id, role_id)
            VALUES (user_id, manager_role_id)
            ON CONFLICT DO NOTHING;
        ELSIF random() < 0.05 AND admin_role_id IS NOT NULL THEN
            INSERT INTO user_roles (user_id, role_id)
            VALUES (user_id, admin_role_id)
            ON CONFLICT DO NOTHING;
        ELSIF guest_role_id IS NOT NULL THEN
            INSERT INTO user_roles (user_id, role_id)
            VALUES (user_id, guest_role_id)
            ON CONFLICT DO NOTHING;
        END IF;

        -- 每100条记录输出一次进度
        IF i % batch_size = 0 THEN
            RAISE NOTICE 'Generated % users...', i;
        END IF;
    END LOOP;

    RAISE NOTICE 'User generation complete!';

    -- 生成登录尝试记录
    RAISE NOTICE 'Generating login attempt records...';

    INSERT INTO login_attempts (id, username, ip_address, attempt_status, failure_reason, created_at)
    SELECT
        uuid_generate_v4(),
        u.username,
        '192.168.1.' || (1 + (random() * 254)::INT),
        CASE WHEN random() < 0.85 THEN 'SUCCESS'::attempt_status ELSE 'FAILED'::attempt_status END,
        CASE WHEN random() >= 0.85 THEN 'Invalid password' ELSE NULL END,
        CURRENT_TIMESTAMP - (random() * interval '30 days')
    FROM users u
    WHERE u.created_at > CURRENT_TIMESTAMP - interval '30 days'
    LIMIT 5000;

    RAISE NOTICE 'Login attempt records generated!';

    -- 生成审计日志
    RAISE NOTICE 'Generating audit logs...';

    INSERT INTO audit_logs (id, action, resource_type, resource_id, old_value, new_value, ip_address, created_at)
    SELECT
        uuid_generate_v4(),
        CASE (random() * 3)::INT
            WHEN 0 THEN 'CREATE'
            WHEN 1 THEN 'UPDATE'
            WHEN 2 THEN 'DELETE'
            ELSE 'VIEW'
        END,
        'user',
        u.id::text,
        NULL,
        json_build_object('username', u.username, 'email', u.email)::text,
        '192.168.1.' || (1 + (random() * 254)::INT),
        CURRENT_TIMESTAMP - (random() * interval '30 days')
    FROM users u
    LIMIT 3000;

    RAISE NOTICE 'Audit logs generated!';

END $$;

-- =============================================
-- 统计信息
-- =============================================
SELECT 'Test data generation complete!' AS status;

SELECT
    'Total Users: ' || COUNT(*)::text AS stats
FROM users
UNION ALL
SELECT
    'Active Users: ' || COUNT(*)::text
FROM users WHERE is_active = true
UNION ALL
SELECT
    'Verified Users: ' || COUNT(*)::text
FROM users WHERE is_verified = true
UNION ALL
SELECT
    'Login Attempts (30 days): ' || COUNT(*)::text
FROM login_attempts WHERE created_at > CURRENT_TIMESTAMP - interval '30 days'
UNION ALL
SELECT
    'Audit Logs: ' || COUNT(*)::text
FROM audit_logs;
