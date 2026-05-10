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
