# prod 目录草图说明

`deploy/prod` 用于放置未来生产部署相关的草图文件。它现在仍然是“生产部署设计区”，不是可以直接原样上线的最终方案。

## 当前定位

这一层更接近未来独立 `lottery-platform-deploy` 仓库的雏形，适合放这些内容：

- 面向公网访问的 Nginx 配置
- 生产环境 Compose 编排文件
- 生产环境变量模板
- 部署与回滚脚本
- 监控、备份、恢复等运行资产

## 当前结构

```text
prod
├─ compose
│  ├─ compose.prod.yml
│  └─ compose.obs.yml
├─ conf
│  └─ nginx
│     ├─ conf.d
│     │  └─ lottery-platform.conf.example
│     └─ html
│        └─ index.html
├─ data
│  ├─ grafana
│  ├─ nginx
│  │  └─ logs
│  ├─ postgres
│  ├─ prometheus
│  └─ redis
├─ env
│  └─ prod.env.example
└─ scripts
   ├─ deploy-prod.ps1
   └─ rollback-prod.ps1
```

## 分层说明

### 1. compose

`compose/` 只负责编排：

- `compose.prod.yml`：公网业务栈草图
- `compose.obs.yml`：监控观测草图

两个文件现在都已经显式声明名称：

- `name: lottery-platform-prod`

这样后续网络名、卷名和项目名不会依赖目录名推导。

### 2. conf

`conf/` 只放运行配置：

- `conf/nginx/conf.d/`：Nginx 站点配置
- `conf/nginx/html/`：默认静态页面或占位页

### 3. data

`data/` 只放运行期持久化数据：

- PostgreSQL 数据目录
- Redis 数据目录
- Nginx 日志目录
- Prometheus / Grafana 数据目录

相比原来的 `storage/`，`data/` 这个名字更直接，也更容易和 `conf/` 形成清晰对照。

### 4. env

`env/` 放生产环境变量模板与实例文件。默认使用：

```text
prod/env/prod.env
```

如果不存在，可从：

```text
prod/env/prod.env.example
```

复制生成。

### 5. scripts

`scripts/` 是生产部署草图的入口层。目前仍是占位版脚本，后续可以逐步收敛为真正可执行的上线和回滚流程。

## 当前建议

第一版生产环境建议尽量收敛最小上线范围，不要把本地联调用到的所有服务一次性搬上公网。

更适合首版公网运行的通常是：

- Nginx
- 前端静态资源
- Gateway
- 真正需要对外提供服务的业务模块

需要谨慎引入的通常是：

- Kafka
- RocketMQ
- Nacos
- MinIO
- 工程化工具如 Jenkins、Nexus

## 我建议的下一步

如果你愿意继续整理，我建议后面优先补这几块：

- 给 `prod/scripts` 增加 `common.ps1`，统一处理 env 文件和 compose 调用。
- 给 `compose.prod.yml` 增加 healthcheck、日志策略和更明确的依赖条件。
- 把 `prod/env/prod.env.example` 再按数据库、缓存、镜像、域名、TLS 分组。
- 把监控与主业务栈是否分开部署的边界在文档里写得更明确。
