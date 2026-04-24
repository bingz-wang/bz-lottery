insert into user_account (id, username, nickname, email, mobile, password, enabled, auth_source, created_at, updated_at,
                          deleted)
values (10001, 'admin', 'System Admin', 'admin@lottery.local', '13800000001',
        '$2a$10$ke080Fjp8LeoaC8FQl7zKebDuBwwRWJez673qv/SYXskqLGmU/TWW',
        true, 'LOCAL', current_timestamp, current_timestamp, false) on conflict (id) do
update
    set username = excluded.username,
    nickname = excluded.nickname,
    email = excluded.email,
    mobile = excluded.mobile,
    password = excluded.password,
    enabled = excluded.enabled,
    auth_source = excluded.auth_source,
    deleted = excluded.deleted,
    updated_at = current_timestamp;

insert into sys_role (id, role_code, role_name, role_type, status, sort, remark, created_at, updated_at, deleted)
values (1, 'LOTTERY_USER', 'Lottery User', 'BUSINESS', 1, 10, 'Default role for all registered users',
        current_timestamp, current_timestamp, false),
       (2, 'LOTTERY_ADMIN', 'Lottery Admin', 'SYSTEM', 1, 20, 'Operations and administration role', current_timestamp,
        current_timestamp, false),
       (3, 'IDP_USER', 'Identity Provider User', 'INTEGRATION', 1, 30, 'User synced from Keycloak', current_timestamp,
        current_timestamp, false) on conflict (id) do
update
    set role_code = excluded.role_code,
    role_name = excluded.role_name,
    role_type = excluded.role_type,
    status = excluded.status,
    sort = excluded.sort,
    remark = excluded.remark,
    deleted = excluded.deleted,
    updated_at = current_timestamp;

insert into sys_menu (id, menu_code, menu_name, parent_id, path, route_name, component, menu_type, icon, sort, status,
                      visible, remark, created_at, updated_at, deleted)
values (1, 'DASHBOARD_OVERVIEW', 'Overview', null, '/workspace/overview', 'overview', 'workspace/OverviewView', 'MENU',
        'layout', 10, 1, true, 'Workspace overview', current_timestamp, current_timestamp, false),
       (2, 'LOTTERY_DRAW', 'Lottery Workspace', null, '/workspace/lottery', 'lottery', 'workspace/LotteryView', 'MENU',
        'spark', 20, 1, true, 'Lottery workbench', current_timestamp, current_timestamp, false),
       (3, 'PRIZE_CENTER', 'Prize Center', null, '/workspace/prizes', 'prizes', 'workspace/PrizeCenterView', 'MENU',
        'gift', 30, 1, true, 'Prize center', current_timestamp, current_timestamp, false),
       (4, 'ACCOUNT_PROFILE', 'Profile', null, '/workspace/profile', 'profile', 'workspace/ProfileView', 'MENU', 'user',
        40, 1, true, 'Current account info', current_timestamp, current_timestamp, false),
       (5, 'OPERATIONS_CENTER', 'Operations Console', null, '/workspace/operations', 'operations',
        'workspace/OperationsView', 'MENU', 'shield', 50, 1, true, 'Admin-only operations page', current_timestamp,
        current_timestamp, false) on conflict (id) do
update
    set menu_code = excluded.menu_code,
    menu_name = excluded.menu_name,
    parent_id = excluded.parent_id,
    path = excluded.path,
    route_name = excluded.route_name,
    component = excluded.component,
    menu_type = excluded.menu_type,
    icon = excluded.icon,
    sort = excluded.sort,
    status = excluded.status,
    visible = excluded.visible,
    remark = excluded.remark,
    deleted = excluded.deleted,
    updated_at = current_timestamp;

insert into user_role_rel (user_id, role_id, created_at)
values (10001, 1, current_timestamp),
       (10001, 2, current_timestamp) on conflict (user_id, role_id) do nothing;

insert into role_menu_rel (role_id, menu_id, created_at)
values (1, 1, current_timestamp),
       (1, 2, current_timestamp),
       (1, 3, current_timestamp),
       (1, 4, current_timestamp),
       (2, 1, current_timestamp),
       (2, 2, current_timestamp),
       (2, 3, current_timestamp),
       (2, 4, current_timestamp),
       (2, 5, current_timestamp),
       (3, 1, current_timestamp),
       (3, 4, current_timestamp) on conflict (role_id, menu_id) do nothing;

insert into lottery_prize (prize_code, prize_name, prize_level, prize_level_sort, probability, total_stock,
                           available_stock, prize_desc, prize_image, status, sort, created_by, updated_by, deleted)
values ('PRIZE_001', 'Huawei MateBook X Pro', 'Special Prize', 1, 0.002000, 1, 1,
        'Top-tier device with extremely low probability', null, 1, 1, 'system', 'system', false),
       ('PRIZE_002', 'iPad Air', 'First Prize', 2, 0.008000, 3, 3, 'Popular digital item with low probability', null, 1,
        2, 'system', 'system', false),
       ('PRIZE_003', 'AirPods Pro', 'Second Prize', 3, 0.030000, 8, 8, 'High-value accessory with medium probability',
        null, 1, 3, 'system', 'system', false),
       ('PRIZE_004', 'Mechanical Keyboard', 'Third Prize', 4, 0.080000, 20, 20, 'Physical gift with medium probability',
        null, 1, 4, 'system', 'system', false),
       ('PRIZE_005', 'Coffee Gift Box', 'Lucky Prize', 5, 0.180000, 50, 50, 'Lightweight gift with higher probability',
        null, 1, 5, 'system', 'system', false),
       ('PRIZE_006', '50 Yuan Voucher', 'Participation Prize', 6, 0.700000, 500, 500,
        'Base prize with high probability', null, 1, 6, 'system', 'system', false) on conflict (prize_code) do
update
    set prize_name = excluded.prize_name,
    prize_level = excluded.prize_level,
    prize_level_sort = excluded.prize_level_sort,
    probability = excluded.probability,
    total_stock = excluded.total_stock,
    available_stock = excluded.available_stock,
    prize_desc = excluded.prize_desc,
    prize_image = excluded.prize_image,
    status = excluded.status,
    sort = excluded.sort,
    updated_by = excluded.updated_by,
    deleted = excluded.deleted,
    updated_at = current_timestamp;
