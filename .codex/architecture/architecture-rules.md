# 架构规范

## 摘要
定义系统模块边界、依赖方向和关键非功能约束，避免架构漂移和隐性耦合。

## 规则
1. 模块边界固定：`gateway/common/user/activity/lottery/award/pay/workflow/file/monitor/ai`。
2. 分层固定：`controller -> service -> repository/mapper`，禁止跨层直连。
3. 网关统一入口，禁止前端绕过网关访问内部服务。
4. 跨服务调用使用既定协议，禁止私有临时协议。
5. 公共能力沉淀到 `lottery-common`，禁止重复实现。
6. 关键链路必须带 `traceId`，并可关联业务主键。
7. 关键异步流程（MQ）必须具备幂等消费与失败重试。
8. 跨服务一致性采用最终一致性，不引入隐式分布式事务。

## 检查清单
1. 是否新增了不必要的跨模块依赖。
2. 是否存在 `controller` 直接访问数据库。
3. 是否所有外部入口都经过网关。
4. 是否关键日志可通过 `traceId` 串联。
5. 是否异步消息具备幂等和重试保护。

## 示例
```text
正确：
controller 调用 service，service 调用 repository。

错误：
controller 直接调用 mapper；
frontend 直接访问 lottery-user 的内部端口。
```
