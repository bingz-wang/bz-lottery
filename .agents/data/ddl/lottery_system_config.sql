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
