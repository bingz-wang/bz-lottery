---
category: api
version: "1.0.0"
last_updated: "2026-05-09"
---

# API 规范

## 摘要
统一接口路径、请求语义、响应体、错误码、鉴权和文档要求，保证前后端协作稳定。

## 规则
1. 接口统一经网关暴露，对外路径使用 `/api/...`。
2. `GET` 仅查询，`POST` 创建/动作，`PUT` 全量更新，`PATCH` 局部更新，`DELETE` 删除。
3. 返回体统一包含：`code`、`message`、`data`、`timestamp`、`traceId`。
4. 错误码分层：参数错误、认证鉴权错误、业务冲突、系统错误。
5. 鉴权默认开启，白名单仅限登录、健康检查、必要文档接口。
6. 分页统一参数：`pageNum`、`pageSize`，并设置 `pageSize` 上限。
7. 对外动作型接口建议支持幂等键（如 `requestNo`）。
8. 接口变更必须同步更新 Swagger/OpenAPI。

## 检查清单
1. 是否遵循 HTTP 方法语义。
2. 是否统一返回体结构。
3. 是否覆盖主要错误码和错误文案。
4. 是否进行参数校验和鉴权校验。
5. 是否更新接口文档和示例。

## 示例
```json
{
  "code": "0000",
  "message": "success",
  "data": {
    "recordNo": "DRAW202604240001"
  },
  "timestamp": "2026-04-24T23:30:00+08:00",
  "traceId": "TRACE202604240001"
}
```
