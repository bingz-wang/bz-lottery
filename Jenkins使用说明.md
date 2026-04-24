# Jenkins 使用说明

这份说明面向你的个人练手项目，目标不是搭一套复杂的企业级 CI，而是让你可以用最小成本反复练习：

- Docker Compose 管理 Jenkins
- Jenkins 跑当前仓库的 `Jenkinsfile`
- 本地命令和 Jenkins 流水线尽量共用同一套脚本

## 1. 启动方式

### 只启动 Jenkins

```powershell
cd C:\develop\project\lottery-platform\lottery-platform-backend\deploy\docker
.\scripts\start-jenkins.ps1
```

### 启动整套 Docker 测试环境

更适合你平时练“构建 + 联调 + Jenkins”整套链路：

```powershell
cd C:\develop\project\lottery-platform\lottery-platform-backend\deploy\docker
.\scripts\start-all.ps1
```

### 停止整套环境

```powershell
cd C:\develop\project\lottery-platform\lottery-platform-backend\deploy\docker
.\scripts\stop-all.ps1
```

## 2. 访问地址

- Jenkins Web：`http://localhost:9200`
- Jenkins Agent：`9201`

当前测试项目默认管理员账号：

```powershell
username: admin
password: 123456
```

如果你想改掉默认密码，可以修改：

- `deploy/docker/.env.example` 中的 `JENKINS_ADMIN_ID`
- `deploy/docker/.env.example` 中的 `JENKINS_ADMIN_PASSWORD`

## 3. 当前 Jenkins 镜像内置内容

为了适配这个项目，Jenkins 镜像已经内置：

- Git
- JDK 25
- Maven 3.9.x
- Node.js 22
- 常用 Pipeline 插件

因此仓库根目录下的 `Jenkinsfile` 可以直接使用。

## 4. 如何创建流水线任务

在 Jenkins 中新建一个 `Pipeline` 类型任务，然后：

1. `Definition` 选择 `Pipeline script from SCM`
2. `SCM` 选择 `Git`
3. 填入你的仓库地址
4. `Script Path` 填 `Jenkinsfile`

如果你当前只是本地练习，也可以先把代码推到自己的 Git 仓库，再让 Jenkins 从远程仓库拉取。

## 5. 当前流水线会做什么

默认会执行这些步骤：

1. Checkout 代码
2. 检查 `java / mvn / node / npm`
3. 执行 `scripts/ci/backend-verify.sh`
4. 执行 `scripts/ci/frontend-build.sh`
5. 按参数执行 `scripts/ci/smoke-check.sh`
6. 归档后端 jar 和前端 dist 产物

## 6. 为什么这样设计更适合个人项目

- Jenkins 跑的命令和你本地手动跑的是同一套脚本
- 出错后排障更容易，不需要去 Jenkins 页面里反推逻辑
- 你可以先练“构建”，再练“联调”
- smoke test 不强依赖 Jenkins 自动拉起整个业务环境，适合本地边开发边验证

## 7. 可用参数

- `PIPELINE_MODE`
  - `quick`：只做构建
  - `full`：构建后额外跑 smoke test
- `SKIP_BACKEND`
  - 跳过后端 Maven 阶段
- `SKIP_FRONTEND`
  - 跳过前端构建阶段
- `RUN_SMOKE_TEST`
  - 单独控制是否运行 smoke test
- `BACKEND_MAVEN_GOALS`
  - 默认 `clean verify`
- `SMOKE_BASE_URL`
  - 默认 `http://localhost:9008`
- `SMOKE_FRONTEND_URL`
  - 默认 `http://localhost:9010`

## 8. 常用命令

启动 Jenkins：

```powershell
cd C:\develop\project\lottery-platform\lottery-platform-backend\deploy\docker
.\scripts\start-jenkins.ps1
```

启动整套环境：

```powershell
cd C:\develop\project\lottery-platform\lottery-platform-backend\deploy\docker
.\scripts\start-all.ps1
```

停止整套环境：

```powershell
cd C:\develop\project\lottery-platform\lottery-platform-backend\deploy\docker
.\scripts\stop-all.ps1
```

查看当前 Jenkins 容器日志：

```powershell
docker logs lottery-jenkins
```

## 9. 建议的后续优化

如果你后面还想继续打磨这套个人项目，建议按这个顺序推进：

1. 接入 Git 凭据
2. 增加多分支流水线
3. 把 smoke test 从 `ping` 升级成真实业务流
4. 补更多自动化测试
5. 再考虑把 `docker compose` 联调环境也纳入流水线
