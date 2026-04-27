# 数据基线

## 摘要
定义数据库对象、状态语义、迁移策略和发布检查项，作为 SQL 改动与排障依据。

## 规则
1. 权威来源：
- `V1__create_core_tables.sql`
- `V2__seed_initial_data.sql`
- `V3__add_lottery_strategy_config.sql`
- `R__sync_reference_data.sql`
- `R__sync_system_config.sql`
2. 迁移规则：
- 优先增量迁移；
- 禁止无评估的破坏式 DDL。
3. 幂等规则：
- 同步脚本必须可重复执行（`on conflict`）。
4. 约束规则：
- 核心约束不得删除（概率区间、库存关系、状态值范围）。
5. 追踪规则：
- 关键记录需保留请求号和 traceId，便于审计与回放。

## 检查清单
1. 是否新增了对应 Flyway 脚本。
2. 是否评估了索引与性能影响。
3. 是否保证脚本可重复执行。
4. 是否校验关键种子数据兼容性。
5. 是否提供了回滚或修复预案。

## 示例
```sql
-- 查询奖池库存
select prize_code, available_stock, total_stock
from lottery_prize
where deleted = false;

-- 查询全局抽奖策略
select config_key, config_value
from lottery_system_config
where config_key = 'LOTTERY_DRAW_STRATEGY';
```
