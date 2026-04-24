# deploy/docker 目录说明

`deploy/docker` 现在只承担 Docker 相关资产的归档职责，不再把“构建文件”“挂载配置”“运行数据”混放在同一层。

当前结构：

```text
deploy/docker
├─ build
│  ├─ jenkins
│  │  ├─ Dockerfile
│  │  └─ plugins.txt
│  └─ nginx
│     └─ Dockerfile
├─ conf
│  ├─ jenkins
│  │  └─ init.groovy.d
│  ├─ nginx
│  │  ├─ docker-entrypoint.d
│  │  └─ html
│  └─ rocketmq
│     └─ broker.conf
└─ data
   ├─ jenkins
   ├─ kafka
   ├─ minio
   ├─ nacos
   ├─ nexus
   ├─ postgres
   ├─ redis
   └─ rocketmq
```

约定如下：

- `build/`：镜像构建上下文，只放 `Dockerfile` 和构建期依赖文件。
- `conf/`：容器运行时挂载的配置、初始化脚本和静态资源。
- `data/`：本地开发或联调时产生的持久化数据。

如果只是启动或停止环境，请统一使用上层入口脚本，而不是直接在这里执行命令：

```powershell
.\deploy\scripts\up-dev.ps1
.\deploy\scripts\up-tools.ps1
.\deploy\scripts\up-edge.ps1
.\deploy\scripts\up-full.ps1
.\deploy\scripts\down.ps1
```
