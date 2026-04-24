# lottery-platform-backend

基于 Spring Boot 3、Spring Cloud、Spring Cloud Alibaba 的抽奖平台微服务骨架项目。

这个仓库更适合个人练手和技术演练，而不是生产环境交付。当前重点是把微服务拆分、网关、基础设施接入、文档、联调方式、Jenkins
流水线这些“工程能力”先搭起来，方便你后续继续补业务。

## 项目定位

- 用来练习微服务拆分、网关接入和基础设施联调
- 用来练习 Spring Boot 3、Spring Cloud、Nacos、消息队列、对象存储等技术栈
- 用来练习本地 Docker 测试环境、Nginx 转发和 Jenkins 流水线
- 当前更偏“可扩展骨架”，不是完整业务系统

## 技术栈

- Java 25
- Maven 3.9+
- Spring Boot 3.5.7
- Spring Cloud 2025.0.1
- Spring Cloud Alibaba 2025.0.0.0
- Spring Cloud Gateway
- Nacos Discovery / Config
- Sentinel
- Dubbo 3.3.6
- MyBatis-Plus 3.5.12
- Redis + Redisson
- RocketMQ
- Kafka
- MinIO
- Activiti 7
- Spring Security
- Springdoc OpenAPI / Swagger UI
- SkyWalking Trace Toolkit

## 模块结构

```text
lottery-platform-backend
├─ lottery-gateway
├─ lottery-common
├─ lottery-user
├─ lottery-activity
├─ lottery-lottery
├─ lottery-award
├─ lottery-pay
├─ lottery-workflow
├─ lottery-file
└─ lottery-monitor
```

## 模块说明

### `lottery-gateway`

统一网关入口，当前默认端口为 `9008`。

已具备：

- 基于 Spring Cloud Gateway 的统一入口
- 响应式处理模型
- 公共路径和受保护路径配置
- Swagger / OpenAPI 入口聚合能力
- `X-Trace-Id` 透传能力

### `lottery-common`

公共基础模块，当前包含：

- 统一响应结构
- 统一异常处理
- API 响应自动包装
- 公共日志与链路追踪基础配置
- OpenAPI 公共配置
- 虚拟线程与结构化并发支持

### 业务服务

当前业务服务都具备独立启动能力，并提供最小化的 `ping` 接口用于联通验证。

| 模块                 | 说明                         |   端口 | 示例接口                 |
|--------------------|----------------------------|-----:|----------------------|
| `lottery-user`     | 用户服务，集成 Spring Security 骨架 | 9101 | `/api/user/ping`     |
| `lottery-activity` | 活动服务                       | 9102 | `/api/activity/ping` |
| `lottery-lottery`  | 抽奖服务                       | 9103 | `/api/lottery/ping`  |
| `lottery-award`    | 奖品服务                       | 9104 | `/api/award/ping`    |
| `lottery-pay`      | 支付服务                       | 9105 | `/api/pay/ping`      |
| `lottery-workflow` | 工作流服务                      | 9106 | `/api/workflow/ping` |
| `lottery-file`     | 文件服务                       | 9107 | `/api/file/ping`     |
| `lottery-monitor`  | 监控服务                       | 9109 | `/api/monitor/ping`  |

## 当前工程状态

当前项目已经具备比较完整的“练手工程骨架”：

- 微服务模块划分已经完成
- 每个服务都有独立启动类和基础配置
- 网关、用户、安全、监控这些基础能力已经接入
- Redis、Kafka、RocketMQ、MinIO、Nacos 等基础设施已经预留联调方式
- 前后端联调、Nginx 测试入口、Jenkins 流水线已经串起来

当前仍然偏骨架状态的部分：

- 领域模型和业务流程还不完整
- 真实业务联调链路还不够丰富
- 自动化测试目前主要是基础构建和 smoke check
- 更适合个人练习和持续演进，而不是直接投产

## 运行环境要求

- JDK 25
- Maven 3.9+
- 推荐配合本仓库的 Docker 测试环境使用：
  - PostgreSQL `localhost:5432`
  - Redis `localhost:6379`
  - Kafka `localhost:9092`
  - RocketMQ NameServer `localhost:9876`
  - Nacos `localhost:8848`
  - MinIO `localhost:9000`

更完整的 Docker、Nginx、Jenkins 用法请看：

- `deploy/README.md`
- 工作区根目录 `Jenkins使用说明.md`
- 工作区根目录 `局域网测试接入说明.md`

## 快速开始

### 1. 编译整个后端

```powershell
cd C:\develop\project\lottery-platform\lottery-platform-backend
mvn -q -DskipTests compile
```

### 2. 启动单个服务

启动网关：

```powershell
mvn -pl lottery-gateway spring-boot:run
```

启动用户服务：

```powershell
mvn -pl lottery-user spring-boot:run
```

如果希望同时带上依赖模块：

```powershell
mvn -pl lottery-user -am spring-boot:run
```

### 3. 验证服务是否正常

例如验证用户服务：

```powershell
curl http://localhost:9101/api/user/ping
```

预期返回类似：

```json
{
  "code": "0000",
  "message": "success",
  "data": {
    "service": "lottery-user",
    "status": "UP"
  },
  "timestamp": 1710000000000
}
```

### 4. 查看接口文档

- 网关：`http://localhost:9008/swagger-ui.html`
- 用户服务：`http://localhost:9101/swagger-ui.html`
- 活动服务：`http://localhost:9102/swagger-ui.html`
- 抽奖服务：`http://localhost:9103/swagger-ui.html`
- 奖品服务：`http://localhost:9104/swagger-ui.html`
- 支付服务：`http://localhost:9105/swagger-ui.html`
- 工作流服务：`http://localhost:9106/swagger-ui.html`
- 文件服务：`http://localhost:9107/swagger-ui.html`
- 监控服务：`http://localhost:9109/swagger-ui.html`

## 配置说明

当前各服务 `application.yml` 基本保持一致的策略：

- `spring.threads.virtual.enabled=true`
- `management.endpoints.web.exposure.include=health,info,prometheus`
- `springdoc.swagger-ui.path=/swagger-ui.html`
- `springdoc.api-docs.path=/v3/api-docs`
- 默认连接本地基础设施地址
- Nacos Discovery / Config 默认关闭
- Dubbo 注册中心默认关闭

如果你后面继续深入演练，建议优先把这些配置做进一步外置化：

- 数据库连接
- Redis / Kafka / RocketMQ 地址
- Nacos 地址和命名空间
- JWT / Sa-Token 配置
- MinIO 凭证
- Sentinel 控制台配置
- SkyWalking Agent / OAP 配置

## 安全说明

`lottery-user` 当前已经接入 Spring Security 骨架。

默认放行的典型路径包括：

- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/actuator/**`
- `/api/user/ping`

其余接口默认需要认证。当前仓库已接入 Sa-Token 和 JWT 相关依赖，但完整认证闭环还可以继续完善。

## 可观测性与治理

当前已经具备的基础治理能力：

- Actuator 健康检查与 Prometheus 暴露
- Swagger / OpenAPI 文档
- `X-Trace-Id` 链路透传
- 日志中携带 `traceId`
- Sentinel 依赖接入
- SkyWalking Toolkit 依赖接入

## 建议的演进方向

如果你想继续把它打磨成一个更完整的个人练手项目，可以按这个顺序往下推进：

1. 完善领域模型和数据库表结构
2. 把用户、活动、抽奖、奖品、支付几个核心链路串起来
3. 补齐登录鉴权闭环
4. 增加更真实的业务 smoke test 和集成测试
5. 把 Jenkins 流水线继续升级成“构建 + 联调 + 验证”模式
6. 增加 Docker 化启动脚本和统一入口
7. 最后再考虑更强的可观测性和更复杂的部署方式

当前 `deploy` 目录已经按“开发依赖 / 工具服务 / 入口层”进行拆分，便于后续在需要正式上服务器时，再把生产部署相关内容平滑迁移为独立
deploy 仓库。

## 常用联通接口

```text
GET /api/user/ping
GET /api/activity/ping
GET /api/lottery/ping
GET /api/award/ping
GET /api/pay/ping
GET /api/workflow/ping
GET /api/file/ping
GET /api/monitor/ping
```

## License

本项目采用 [Apache License 2.0](./LICENSE)。
