---
name: bz-lottery-dev-skill
description: 面向 bz-lottery 项目的研发技能。用于后端开发、前端联调、数据库迁移、接口设计、业务规则落地、代码规范执行、提交规范执行等场景。触发后先读取 .codex 下规范文件并按顺序执行。
---

# bz-lottery 项目技能

## 摘要
本技能用于统一项目研发流程和交付质量。执行任何任务前，先读取规范，再实施改动，再执行验证，最后按统一格式交付结果。

## 规则
1. 按顺序读取以下文件：
- `.codex/architecture/architecture-rules.md`
- `.codex/business/business-rules.md`
- `.codex/api/api-spec.md`
- `.codex/code/code-style.md`
- `.codex/data/data-baseline.md`
- `.codex/commit/git-commit-guidelines.md`
2. 先读后改，先评估影响范围再修改代码。
3. 只做与当前需求直接相关的最小改动。
4. 涉及数据库变更必须先补 Flyway 脚本。
5. 涉及接口变更必须同步更新接口文档和错误码说明。
6. 涉及业务规则变更必须补充回归测试或最小验证方案。
7. 交付输出必须包含：变更范围、验证结果、风险说明。

## 检查清单
1. 是否已按顺序读取 6 份规范文件。
2. 是否确认改动边界和受影响模块。
3. 是否保持返回体、异常、日志风格一致。
4. 是否执行最小可行验证命令。
5. 是否在交付中说明未验证项和风险项。

## 示例
```text
请先读取 .codex/SKILL.md，并按顺序读取：
1) .codex/architecture/architecture-rules.md
2) .codex/business/business-rules.md
3) .codex/api/api-spec.md
4) .codex/code/code-style.md
5) .codex/data/data-baseline.md
6) .codex/commit/git-commit-guidelines.md
然后再开始实现。
```
