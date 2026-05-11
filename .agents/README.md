# .agents — bz-lottery 项目 AI 研发规范

本目录存放面向 AI 编程助手的项目研发规范与参考文档。所有文档按领域拆分，由 `SKILL.md` 统一编排加载顺序。

## 目录结构

```
.agents/
├── SKILL.md                           # 技能入口：加载顺序、全局规则、交付标准
├── README.md                          # 本文件：目录导航与维护说明
├── .gitignore                         # Obsidian 工作区元数据忽略
├── .obsidian/                         # Obsidian 工作区配置（仅用于文档浏览，不参与 AI 加载）
├── architecture/
│   └── architecture-rules.md          # 架构规范：模块边界、分层、跨服务调用
├── business/
│   └── business-rules.md              # 业务规则：抽奖链路核心口径
├── api/
│   └── api-spec.md                    # API 规范：路径、请求/响应体、错误码、鉴权
├── code/
│   ├── coding-standards.md            # 编码规范：命名、分层、异常、日志、测试
│   └── java-dev.md                    # Java 开发规范：Lombok、并发、N+1、校验、SQL 等
├── data/
│   ├── data-baseline.md               # 数据基线：Flyway 迁移、约束、幂等规则
│   ├── ddl-all-tables.md              # 全量表结构 DDL：所有建表语句（可直接执行）
│   ├── schema-index.md                # 表结构索引：所有表的用途、关系、索引概览
└── commit/
    └── git-commit-guidelines.md       # 提交规范：分支、信息格式、PR 要求
```

## 文档分类

| 领域 | 文档 | 用途 |
|------|------|------|
| 入口 | `SKILL.md` | AI 技能定义、加载顺序、全局规则、交付标准 |
| 架构 | `architecture/architecture-rules.md` | 模块边界、分层约束、跨服务通信 |
| 业务 | `business/business-rules.md` | 抽奖/用户/奖品/发奖/支付核心规则 |
| 接口 | `api/api-spec.md` | RESTful 规范、响应体、错误码、分页 |
| 编码 | `code/coding-standards.md` | 命名、分层、异常、日志、性能、测试 |
| 编码 | `code/java-dev.md` | Lombok、并发安全、N+1、输入校验、SQL 规范 |
| 数据 | `data/data-baseline.md` | Flyway 迁移策略、幂等、约束 |
| 数据 | `data/schema-index.md` | 表结构索引与关系说明 |
| 数据 | `data/ddl-all-tables.md` | 全量表结构 DDL，所有建表语句合并，可直接执行 |
| 提交 | `commit/git-commit-guidelines.md` | 分支命名、提交信息、PR 模板 |

## 维护规范

1. **新增规范文档**：在对应领域目录下新建 `.md` 文件，使用统一的 YAML frontmatter 模板，同步更新 `SKILL.md` 的 Reference 章节和本 README。
2. **新增 DDL**：在 `data/ddl-all-tables.md` 中追加新的建表章节，同步更新 `data/schema-index.md`。
3. **修改现有规范**：先评估影响范围，更新 `last_updated` 字段，必要时在文档内标注变更记录。
4. **编码**：所有文档使用 UTF-8 无 BOM 编码，换行符 LF。

## 文档模板

所有规范文档（SKILL.md 除外）使用统一的 YAML frontmatter：

```yaml
---
category: <architecture|business|api|code|data|commit>
version: "1.0.0"
last_updated: "YYYY-MM-DD"
---
```