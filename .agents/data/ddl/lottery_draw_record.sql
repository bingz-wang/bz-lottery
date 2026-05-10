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
