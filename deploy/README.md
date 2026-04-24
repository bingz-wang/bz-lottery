# deploy 目录说明

`deploy` 目录用于管理当前项目的本地联调环境、开发辅助工具以及生产部署草图。现在的目标是把“启动入口”“Docker 资产”“环境变量”“生产草图”分层放清楚，降低理解成本。

## 目录结构

```text
deploy
├─ compose
│  ├─ compose.dev-infra.yml
│  ├─ compose.dev-tools.yml
│  └─ compose.edge.yml
├─ docker
│  ├─ build
│  ├─ conf
│  └─ data
├─ env
│  └─ .env.example
├─ prod
│  ├─ compose
│  ├─ env
│  ├─ nginx
│  ├─ scripts
│  └─ storage
└─ scripts
   ├─ common.ps1
   ├─ down.ps1
   ├─ init-nexus.ps1
   ├─ set-nginx-dev-mode.ps1
   ├─ set-nginx-static-mode.ps1
   ├─ up-dev.ps1
   ├─ up-edge.ps1
   ├─ up-full.ps1
   ├─ up-jenkins.ps1
   └─ up-tools.ps1
```

## 分层说明

### 1. compose

`compose/` 只放编排文件，按职责分为三层：

- `compose.dev-infra.yml`：本地开发依赖，如 PostgreSQL、Redis、Nacos、Kafka、RocketMQ、MinIO。
- `compose.dev-tools.yml`：工程化工具，如 Jenkins、Nexus。
- `compose.edge.yml`：边缘入口层，目前主要是 Nginx。

### 2. docker

`docker/` 现在按资产类型分层，而不是按服务平铺：

- `docker/build/`：Dockerfile 和构建期文件。
- `docker/conf/`：运行期挂载配置、初始化脚本、静态资源。
- `docker/data/`：运行期持久化数据。

这样比原来更容易回答几个问题：

- 我要改镜像构建逻辑，去哪里找？
- 我要改运行配置，去哪里找？
- 我要清理本地数据，去哪里找？

### 3. env

`env/` 放部署层环境变量模板与实例文件。默认使用：

```text
deploy/env/.env
```

如果不存在，会根据下面模板自动创建：

```text
deploy/env/.env.example
```

### 4. scripts

`scripts/` 是统一入口层。建议始终通过这里启动和停止环境，而不是手动拼 `docker compose` 命令。

常用命令：

```powershell
.\deploy\scripts\up-dev.ps1
.\deploy\scripts\up-tools.ps1
.\deploy\scripts\up-jenkins.ps1
.\deploy\scripts\up-edge.ps1
.\deploy\scripts\up-full.ps1
.\deploy\scripts\down.ps1
```

### 5. prod

`prod/` 继续保留为生产部署草图，与本地联调入口分开演进。后续如果需要拆成独立 deploy 仓库，这一层会是最适合先迁出的部分。

## Nginx 前端模式切换

当前 Nginx 支持两种模式：

- `static`：直接提供 `deploy/docker/conf/nginx/html` 下的静态资源。
- `dev`：反向代理到前端开发服务器。

切换命令：

```powershell
.\deploy\scripts\set-nginx-static-mode.ps1
.\deploy\scripts\set-nginx-dev-mode.ps1
```

## 进一步可优化的方向

如果你愿意继续收一层，我建议下一步考虑这几个方向：

- 把 `compose` 再按场景分成 `dev/`、`tools/`、`edge/` 子目录。
- 给 `docker/data/` 下每个服务补一个 `.gitkeep` 和简短说明，避免目录意义只靠名字猜。
- 为 `scripts/` 增加一个 `clean-data.ps1`，专门清理可安全重建的本地数据目录。
- 把 `prod/` 下的内容和当前开发态文档彻底拆开，减少“本地联调”和“生产部署”信息混杂。
