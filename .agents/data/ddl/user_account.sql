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
