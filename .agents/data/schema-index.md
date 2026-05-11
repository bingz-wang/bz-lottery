---
category: data
version: "1.0.0"
last_updated: "2026-05-11"
---

# 表结构索引

## 摘要

本文档列出项目中所有数据库表的结构概览、用途说明和关系索引。完整的 DDL 语句已合并至 `data/ddl-all-tables.md`。

## 检查清单

1. 是否新增、修改或删除表后同步更新了 `data/ddl-all-tables.md` 文件。
2. 是否更新了本索引文档的表列表和关系说明。
3. 是否评估了索引变更对查询性能的影响。
4. 是否保持 Flyway 迁移脚本与 DDL 文档一致。

## 表列表

| 表名 | 用途 | 关联 |
|------|------|------|
| `lottery_draw_record` | 抽奖记录，每次抽奖请求的完整快照 | → `lottery_prize.prize_id`，→ `user_account.user_id` |
| `lottery_prize` | 奖品定义，奖池中所有奖品的属性与库存 | ← `lottery_draw_record.prize_id` |
| `lottery_system_config` | 系统配置（KV），全局抽奖策略参数 | — |
| `sys_menu` | 系统菜单，前端路由与导航结构 | ← `role_menu_rel.menu_id` |
| `sys_role` | 系统角色 | ← `role_menu_rel.role_id`，← `user_role_rel.role_id` |
| `role_menu_rel` | 角色-菜单关联（多对多） | → `sys_role.role_id`，→ `sys_menu.menu_id` |
| `user_account` | 用户账户，基本信息与认证凭据 | ← `user_role_rel.user_id` |
| `user_role_rel` | 用户-角色关联（多对多） | → `user_account.user_id`，→ `sys_role.role_id` |

## 实体关系

```
user_account ──< user_role_rel >── sys_role ──< role_menu_rel >── sys_menu
     │
     └──< lottery_draw_record >── lottery_prize

lottery_system_config (独立 KV 配置表)
```

## 通用字段约定

- `id`：自增主键（`int8`），使用独立序列
- `created_at` / `updated_at`：创建/更新时间戳，默认 `CURRENT_TIMESTAMP`
- `deleted`：软删除标记（`bool`，默认 `false`）
- 所有 `varchar` 长度已在前端/接口层校验，DDL 中标注上限