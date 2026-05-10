---
name: bz-lottery-dev-skill
version: "1.0.0"
description: 面向 bz-lottery 项目的研发技能。用于后端开发、前端联调、数据库迁移、接口设计、业务规则落地、代码规范执行、提交规范执行等场景。触发后先按目录顺序读取 .agents 规范与 reference 文档并执行。
license: internal
---

# bz-lottery 项目技能

## 摘要

本技能用于统一项目研发流程和交付质量。执行任何任务前，先读取规范，再实施改动，再执行验证，最后按统一格式交付结果。

## 规则

1. 所有新增或修改的文档文件统一使用 UTF-8 编码（建议 UTF-8 无 BOM）。
2. 按目录顺序读取 `.agents` 下规范文档：
   - `architecture` 目录：[architecture-rules]
   - `business` 目录：[business-rules]
   - `api` 目录：[api-spec]
   - `code` 目录：[coding-standards]、[java-dev]
   - `data` 目录：[data-baseline]、[schema-index]、`ddl/*.sql`
   - `commit` 目录：[git-commit-guidelines]
3. 若目录下存在多个 reference 文件，按文件名升序读取。
4. 先读后改，先评估影响范围再修改代码。
5. 只做与当前需求直接相关的最小改动。
6. 涉及数据库变更必须先写 Flyway 脚本。
7. 涉及接口变更必须同步更新接口文档和错误码说明。
8. 涉及业务规则变更必须补充回归测试或最小验证方案。
9. 交付输出必须包含：变更范围、验证结果、风险说明。

## 检查清单

1. 是否确认本次改动文档均为 UTF-8 编码。
2. 是否已按目录顺序读取 reference 文档。
3. 是否确认改动边界和受影响模块。
4. 是否保持返回体、异常、日志风格一致。
5. 是否执行最小可行验证命令。
6. 是否在交付中说明未验证项和风险项。

## 示例

```text
请先读取 .agents/SKILL.md，并按目录顺序读取 reference：
1) architecture: [architecture-rules]
2) business: [business-rules]
3) api: [api-spec]
4) code: [coding-standards], [java-dev]
5) data: [data-baseline], [schema-index], ddl/*.sql
6) commit: [git-commit-guidelines]
然后再开始实现。
```

## Reference

| 引用标记 | 路径 |
|----------|------|
| [architecture-rules] | `.agents/architecture/architecture-rules.md` |
| [business-rules] | `.agents/business/business-rules.md` |
| [api-spec] | `.agents/api/api-spec.md` |
| [coding-standards] | `.agents/code/coding-standards.md` |
| [java-dev] | `.agents/code/java-dev.md` |
| [data-baseline] | `.agents/data/data-baseline.md` |
| [schema-index] | `.agents/data/schema-index.md` |
| [git-commit-guidelines] | `.agents/commit/git-commit-guidelines.md` |
