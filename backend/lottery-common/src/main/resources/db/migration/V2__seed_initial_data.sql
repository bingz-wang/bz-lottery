insert into user_account (id, username, nickname, email, mobile, password, enabled, auth_source, created_at, updated_at,
                          deleted)
values (10001, 'demo_user', 'Demo Admin', 'demo@lottery.local', '13800000001',
        '$2a$10$xK3chxL8QTiFIkouBZfy2ehzI3Dh1FFx6TQmMVv7lL3Mwd02QzhM.',
        true, 'LOCAL', current_timestamp, current_timestamp, false) on conflict (id) do nothing;

insert into sys_role (id, role_code, role_name, role_type, status, sort, remark)
values (1, 'LOTTERY_USER', 'Lottery User', 'BUSINESS', 1, 10, 'Default role for all registered users'),
       (2, 'LOTTERY_ADMIN', 'Lottery Admin', 'SYSTEM', 1, 20, 'Operations and administration role'),
       (3, 'IDP_USER', 'Identity Provider User', 'INTEGRATION', 1, 30,
        'User synced from Keycloak') on conflict (id) do nothing;

insert into sys_menu (id, menu_code, menu_name, parent_id, path, route_name, component, menu_type, icon, sort, status,
                      visible, remark)
values (1, 'DASHBOARD_OVERVIEW', 'Overview', null, '/workspace/overview', 'overview', 'workspace/OverviewView', 'MENU',
        'layout', 10, 1, true, 'Workspace overview'),
       (2, 'LOTTERY_DRAW', 'Lottery Workspace', null, '/workspace/lottery', 'lottery', 'workspace/LotteryView', 'MENU',
        'spark', 20, 1, true, 'Lottery workbench'),
       (3, 'PRIZE_CENTER', 'Prize Center', null, '/workspace/prizes', 'prizes', 'workspace/PrizeCenterView', 'MENU',
        'gift', 30, 1, true, 'Prize center'),
       (4, 'ACCOUNT_PROFILE', 'Profile', null, '/workspace/profile', 'profile', 'workspace/ProfileView', 'MENU', 'user',
        40, 1, true, 'Current account info'),
       (5, 'OPERATIONS_CENTER', 'Operations Console', null, '/workspace/operations', 'operations',
        'workspace/OperationsView', 'MENU', 'shield', 50, 1, true,
        'Admin-only operations page') on conflict (id) do nothing;

insert into user_role_rel (user_id, role_id)
values (10001, 1),
       (10001, 2) on conflict (user_id, role_id) do nothing;

insert into role_menu_rel (role_id, menu_id)
values (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (3, 1),
       (3, 4) on conflict (role_id, menu_id) do nothing;

insert into lottery_prize (prize_code, prize_name, prize_level, prize_level_sort, probability, total_stock,
                           available_stock,
                           prize_desc, prize_image, status, sort, created_by, updated_by)
values ('PRIZE_001', 'Huawei MateBook X Pro', 'Special Prize', 1, 0.002000, 1, 1,
        'Top-tier device with extremely low probability', null, 1, 1, 'system', 'system'),
       ('PRIZE_002', 'iPad Air', 'First Prize', 2, 0.008000, 3, 3, 'Popular digital item with low probability', null, 1,
        2, 'system', 'system'),
       ('PRIZE_003', 'AirPods Pro', 'Second Prize', 3, 0.030000, 8, 8, 'High-value accessory with medium probability',
        null, 1, 3, 'system', 'system'),
       ('PRIZE_004', 'Mechanical Keyboard', 'Third Prize', 4, 0.080000, 20, 20, 'Physical gift with medium probability',
        null, 1, 4, 'system', 'system'),
       ('PRIZE_005', 'Coffee Gift Box', 'Lucky Prize', 5, 0.180000, 50, 50, 'Lightweight gift with higher probability',
        null, 1, 5, 'system', 'system'),
       ('PRIZE_006', '50 Yuan Voucher', 'Participation Prize', 6, 0.700000, 500, 500,
        'Base prize with high probability', null, 1, 6, 'system', 'system') on conflict (prize_code) do nothing;

insert into lottery_draw_record (record_no, user_id, prize_id, prize_code, prize_name, prize_level, prize_level_sort,
                                 hit_probability, draw_status, draw_remark, request_no, trace_id, created_at,
                                 updated_at, deleted)
values ('DRAW202604120001', 10001, 6, 'PRIZE_006', '50 Yuan Voucher', 'Participation Prize', 6, 0.700000, 1,
        'Won a voucher', 'REQ202604120001',
        'TRACE202604120001', current_timestamp - interval '20 minute', current_timestamp - interval '20 minute', false),
       ('DRAW202604120002', 10001, 5, 'PRIZE_005', 'Coffee Gift Box', 'Lucky Prize', 5, 0.180000, 1, 'Won a coffee box',
        'REQ202604120002',
        'TRACE202604120002', current_timestamp - interval '18 minute', current_timestamp - interval '18 minute', false),
       ('DRAW202604120003', 10001, 4, 'PRIZE_004', 'Mechanical Keyboard', 'Third Prize', 4, 0.080000, 2,
        'Prize locked and pending delivery',
        'REQ202604120003', 'TRACE202604120003', current_timestamp - interval '15 minute',
        current_timestamp - interval '15 minute', false),
       ('DRAW202604120004', 10001, null, null, null, null, null, null, 0, 'No hit this time', 'REQ202604120004',
        'TRACE202604120004',
        current_timestamp - interval '12 minute', current_timestamp - interval '12 minute',
        false) on conflict (record_no) do nothing;
