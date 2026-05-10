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
