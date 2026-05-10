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
