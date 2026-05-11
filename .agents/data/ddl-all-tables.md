---
category: data
version: "1.0.0"
last_updated: "2026-05-11"
---

# 全量表结构 DDL

本文档合并了项目中所有数据库表的 DDL 语句（共 8 张表），可直接在 PostgreSQL 中执行。

## 通用约定

- `id`：自增主键（`int8`），使用独立序列
- `created_at` / `updated_at`：创建/更新时间戳，默认 `CURRENT_TIMESTAMP`
- `deleted`：软删除标记（`bool`，默认 `false`）
- 所有 `varchar` 长度已在前端/接口层校验，DDL 中标注上限

## 表列表

| 序号 | 表名 | 用途 |
|------|------|------|
| 1 | `lottery_draw_record` | 抽奖记录，每次抽奖请求的完整快照 |
| 2 | `lottery_prize` | 奖品定义，奖池中所有奖品的属性与库存 |
| 3 | `lottery_system_config` | 系统配置（KV），全局抽奖策略参数 |
| 4 | `sys_menu` | 系统菜单，前端路由与导航结构 |
| 5 | `sys_role` | 系统角色 |
| 6 | `role_menu_rel` | 角色-菜单关联（多对多） |
| 7 | `user_account` | 用户账户，基本信息与认证凭据 |
| 8 | `user_role_rel` | 用户-角色关联（多对多） |

## 实体关系

```
user_account ──< user_role_rel >── sys_role ──< role_menu_rel >── sys_menu
     │
     └──< lottery_draw_record >── lottery_prize

lottery_system_config (独立 KV 配置表)
```

---

## 1. lottery_draw_record

**说明**：抽奖记录表，记录每次抽奖请求的完整快照。

```sql
-- ============================================================
-- Table: lottery_draw_record
-- Description: 抽奖记录表，记录每次抽奖请求的完整快照
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS lottery_draw_record_id_seq;

CREATE TABLE IF NOT EXISTS lottery_draw_record (
    id              int8            NOT NULL DEFAULT nextval('lottery_draw_record_id_seq'::regclass),
    record_no       varchar(64)     NOT NULL,
    user_id         int8            NOT NULL,
    prize_id        int8,
    prize_code      varchar(64),
    prize_name      varchar(128),
    prize_level     varchar(32),
    prize_level_sort int4,
    hit_probability numeric(10,6),
    draw_status     int2            NOT NULL,
    draw_remark     varchar(255),
    request_no      varchar(64),
    trace_id        varchar(64),
    created_at      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         bool            NOT NULL DEFAULT false,

    CONSTRAINT lottery_draw_record_pkey PRIMARY KEY (id),
    CONSTRAINT lottery_draw_record_record_no_key UNIQUE (record_no)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_lottery_draw_record_user_id    ON lottery_draw_record (user_id);
CREATE INDEX IF NOT EXISTS idx_lottery_draw_record_prize_id    ON lottery_draw_record (prize_id);
CREATE INDEX IF NOT EXISTS idx_lottery_draw_record_draw_status ON lottery_draw_record (draw_status);
CREATE INDEX IF NOT EXISTS idx_lottery_draw_record_created_at  ON lottery_draw_record (created_at);
CREATE INDEX IF NOT EXISTS idx_lottery_draw_record_request_no  ON lottery_draw_record (request_no);
CREATE INDEX IF NOT EXISTS idx_lottery_draw_record_deleted     ON lottery_draw_record (deleted);
```

---

## 2. lottery_prize

**说明**：奖品表，定义奖池中所有奖品的属性与库存。

```sql
-- ============================================================
-- Table: lottery_prize
-- Description: 奖品表，定义奖池中所有奖品的属性与库存
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS lottery_prize_id_seq;

CREATE TABLE IF NOT EXISTS lottery_prize (
    id              int8            NOT NULL DEFAULT nextval('lottery_prize_id_seq'::regclass),
    prize_code      varchar(64)     NOT NULL,
    prize_name      varchar(128)    NOT NULL,
    prize_level     varchar(32)     NOT NULL,
    prize_level_sort int4           NOT NULL DEFAULT 999,
    probability     numeric(10,6)   NOT NULL,
    total_stock     int4            NOT NULL DEFAULT 0,
    available_stock int4            NOT NULL DEFAULT 0,
    prize_desc      varchar(255),
    prize_image     varchar(255),
    status          int2            NOT NULL DEFAULT 1,
    sort            int4            NOT NULL DEFAULT 0,
    created_by      varchar(64)     DEFAULT 'system'::varchar,
    updated_by      varchar(64)     DEFAULT 'system'::varchar,
    created_at      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         bool            NOT NULL DEFAULT false,

    CONSTRAINT lottery_prize_pkey PRIMARY KEY (id),
    CONSTRAINT lottery_prize_prize_code_key UNIQUE (prize_code)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_lottery_prize_status     ON lottery_prize (status);
CREATE INDEX IF NOT EXISTS idx_lottery_prize_level_sort ON lottery_prize (prize_level_sort);
CREATE INDEX IF NOT EXISTS idx_lottery_prize_sort        ON lottery_prize (sort);
CREATE INDEX IF NOT EXISTS idx_lottery_prize_deleted     ON lottery_prize (deleted);
```

---

## 3. lottery_system_config

**说明**：系统配置表，存储全局抽奖策略与运行时参数（KV 结构）。

```sql
-- ============================================================
-- Table: lottery_system_config
-- Description: 系统配置表，存储全局抽奖策略与运行时参数（KV 结构）
-- ============================================================

CREATE TABLE IF NOT EXISTS lottery_system_config (
    config_key   varchar(64)   NOT NULL,
    config_value varchar(128)  NOT NULL,
    config_desc  varchar(255),
    updated_at   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   varchar(64)   DEFAULT 'system'::varchar,

    CONSTRAINT lottery_system_config_pkey PRIMARY KEY (config_key)
);
```

---

## 4. sys_menu

**说明**：系统菜单表，定义前端路由与导航菜单结构。

```sql
-- ============================================================
-- Table: sys_menu
-- Description: 系统菜单表，定义前端路由与导航菜单结构
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS sys_menu_id_seq;

CREATE TABLE IF NOT EXISTS sys_menu (
    id          int8            NOT NULL DEFAULT nextval('sys_menu_id_seq'::regclass),
    menu_code   varchar(64)     NOT NULL,
    menu_name   varchar(64)     NOT NULL,
    parent_id   int8,
    path        varchar(128)    NOT NULL,
    route_name  varchar(64)     NOT NULL,
    component   varchar(128)    NOT NULL,
    menu_type   varchar(32)     NOT NULL DEFAULT 'MENU'::varchar,
    icon        varchar(64),
    sort        int4            NOT NULL DEFAULT 0,
    status      int2            NOT NULL DEFAULT 1,
    visible     bool            NOT NULL DEFAULT true,
    remark      varchar(255),
    created_at  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     bool            NOT NULL DEFAULT false,

    CONSTRAINT sys_menu_pkey PRIMARY KEY (id),
    CONSTRAINT sys_menu_menu_code_key UNIQUE (menu_code)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent_id ON sys_menu (parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_menu_status    ON sys_menu (status);
CREATE INDEX IF NOT EXISTS idx_sys_menu_deleted   ON sys_menu (deleted);
```

---

## 5. sys_role

**说明**：系统角色表，定义角色编码、类型与状态。

```sql
-- ============================================================
-- Table: sys_role
-- Description: 系统角色表，定义角色编码、类型与状态
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS sys_role_id_seq;

CREATE TABLE IF NOT EXISTS sys_role (
    id          int8            NOT NULL DEFAULT nextval('sys_role_id_seq'::regclass),
    role_code   varchar(64)     NOT NULL,
    role_name   varchar(64)     NOT NULL,
    role_type   varchar(32)     NOT NULL DEFAULT 'BUSINESS'::varchar,
    status      int2            NOT NULL DEFAULT 1,
    sort        int4            NOT NULL DEFAULT 0,
    remark      varchar(255),
    created_at  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     bool            NOT NULL DEFAULT false,

    CONSTRAINT sys_role_pkey PRIMARY KEY (id),
    CONSTRAINT sys_role_role_code_key UNIQUE (role_code)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_sys_role_status  ON sys_role (status);
CREATE INDEX IF NOT EXISTS idx_sys_role_deleted ON sys_role (deleted);
```

---

## 6. role_menu_rel

**说明**：角色-菜单关联表，定义角色拥有的菜单权限。

```sql
-- ============================================================
-- Table: role_menu_rel
-- Description: 角色-菜单关联表，定义角色拥有的菜单权限
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS role_menu_rel_id_seq;

CREATE TABLE IF NOT EXISTS role_menu_rel (
    id         int8       NOT NULL DEFAULT nextval('role_menu_rel_id_seq'::regclass),
    role_id    int8       NOT NULL,
    menu_id    int8       NOT NULL,
    created_at timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT role_menu_rel_pkey PRIMARY KEY (id),
    CONSTRAINT role_menu_rel_role_id_menu_id_key UNIQUE (role_id, menu_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_role_menu_rel_role_id ON role_menu_rel (role_id);
CREATE INDEX IF NOT EXISTS idx_role_menu_rel_menu_id ON role_menu_rel (menu_id);
```

---

## 7. user_account

**说明**：用户账户表，存储用户基本信息与认证凭据。

```sql
-- ============================================================
-- Table: user_account
-- Description: 用户账户表，存储用户基本信息与认证凭据
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS user_account_id_seq;

CREATE TABLE IF NOT EXISTS user_account (
    id                int8            NOT NULL DEFAULT nextval('user_account_id_seq'::regclass),
    username          varchar(32)     NOT NULL,
    nickname          varchar(64)     NOT NULL,
    email             varchar(128)    NOT NULL,
    mobile            varchar(16),
    password          varchar(255),
    enabled           bool            NOT NULL DEFAULT true,
    auth_source       varchar(32)     NOT NULL DEFAULT 'LOCAL'::varchar,
    keycloak_subject  varchar(128),
    keycloak_username varchar(64),
    created_at        timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           bool            NOT NULL DEFAULT false,

    CONSTRAINT user_account_pkey PRIMARY KEY (id),
    CONSTRAINT user_account_username_key UNIQUE (username),
    CONSTRAINT user_account_email_key    UNIQUE (email)
);

-- Unique constraints
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_account_mobile           ON user_account (mobile)           WHERE mobile IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_account_keycloak_subject ON user_account (keycloak_subject) WHERE keycloak_subject IS NOT NULL;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_user_account_enabled ON user_account (enabled);
CREATE INDEX IF NOT EXISTS idx_user_account_deleted ON user_account (deleted);
```

---

## 8. user_role_rel

**说明**：用户-角色关联表，定义用户拥有的角色。

```sql
-- ============================================================
-- Table: user_role_rel
-- Description: 用户-角色关联表，定义用户拥有的角色
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS user_role_rel_id_seq;

CREATE TABLE IF NOT EXISTS user_role_rel (
    id         int8       NOT NULL DEFAULT nextval('user_role_rel_id_seq'::regclass),
    user_id    int8       NOT NULL,
    role_id    int8       NOT NULL,
    created_at timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT user_role_rel_pkey PRIMARY KEY (id),
    CONSTRAINT user_role_rel_user_id_role_id_key UNIQUE (user_id, role_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_user_role_rel_user_id ON user_role_rel (user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_rel_role_id ON user_role_rel (role_id);
```
