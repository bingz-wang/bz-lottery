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
