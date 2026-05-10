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
