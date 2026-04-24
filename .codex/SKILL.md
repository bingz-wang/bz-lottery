### 抽奖平台项目技能（Skill）梳理与落地指南
结合你的抽奖平台（前后端分离+微服务架构），以下从**核心技能拆解、技术落地实践、进阶优化方向**三个维度整理技能体系，既适配项目现状，也能支撑后续迭代：

---

## 一、核心技能拆解（按模块/场景划分）
### 1. 前端技能（lottery-platform-frontend）
| 技能点                | 落地场景                          | 关键实践                                                                 |
|-----------------------|-----------------------------------|--------------------------------------------------------------------------|
| Vite 工程化           | 前端启动/构建/代理配置            | 1. 基于`.env`/`.env.example`管理环境变量<br>2. 配置Vite代理转发`/lottery-*`到后端网关<br>3. 自定义`VITE_ALLOWED_HOSTS`适配内网穿透（natapp） |
| Docker + Nginx 前端部署 | 前端容器化 & 流量转发             | 1. 通过`Dockerfile.dev`构建前端容器<br>2. Nginx代理前端端口（9510/9511），避免绕过网关 |
| 前端工程规范          | 依赖管理/多环境适配               | 1. 接入私有Nexus npm源（`http://localhost:8081/repository/npm-public/`）<br>2. 区分dev/preview/prod环境端口 |

### 2. 后端技能（lottery-platform-backend）
#### （1）微服务基础能力
| 技能点                | 落地场景                          | 关键实践                                                                 |
|-----------------------|-----------------------------------|--------------------------------------------------------------------------|
| Spring Cloud 微服务架构 | 网关/服务拆分/通信                | 1. 基于Spring Cloud Gateway（lottery-gateway）做统一入口（端口9008）<br>2. 按业务拆分9大模块（用户/活动/抽奖/奖品等）<br>3. 每个服务独立端口+`ping`接口做联通验证 |
| 基础设施集成          | 中间件/存储接入                   | 1. 适配Redis/Redisson、Kafka/RocketMQ、MinIO、PostgreSQL<br>2. 本地Docker一键启动所有依赖中间件 |
| 工程化构建            | Maven多模块管理                   | 1. `mvn -pl 指定模块 -am`启动单个服务（如`lottery-user`）<br>2. 跳过测试编译：`mvn -q -DskipTests compile` |

#### （2）核心技术栈落地
| 技术栈                | 核心应用场景                      | 关键配置/实践                                                           |
|-----------------------|-----------------------------------|--------------------------------------------------------------------------|
| Spring Boot 3.x       | 微服务基础骨架                    | 1. 启用虚拟线程：`spring.threads.virtual.enabled=true`<br>2. Actuator暴露健康检查/监控指标 |
| Spring Cloud Alibaba  | 服务治理（预留）                  | Nacos Discovery/Config（默认关闭，可外置化配置开启）<br>Sentinel限流（依赖已接入） |
| Dubbo 3.3.6           | 微服务远程调用（预留）            | 注册中心默认关闭，可配置Nacos作为Dubbo注册中心                           |
| Spring Security/Sa-Token | 用户认证授权                      | 1. `lottery-user`模块集成Security骨架<br>2. 放行Swagger/`/ping`接口，其余接口需认证<br>3. 预留JWT/Sa-Token闭环扩展 |
| 可观测性              | 链路追踪/监控/文档                | 1. `X-Trace-Id`透传+SkyWalking链路追踪<br>2. Swagger/OpenAPI聚合（网关统一入口`/swagger-ui.html`）<br>3. Prometheus指标暴露 |

#### （3）业务模块能力
| 模块                | 核心技能落地                          | 进阶优化方向                                                           |
|---------------------|---------------------------------------|------------------------------------------------------------------------|
| lottery-user        | Spring Security认证骨架、用户态管理    | 1. 完善JWT登录鉴权闭环<br>2. 接入用户注册/登录/权限管理核心接口         |
| lottery-activity    | 活动配置/上下线/参与规则              | 1. 设计活动生命周期（创建/上线/下架）<br>2. 结合Redis做活动缓存         |
| lottery-lottery     | 抽奖算法/概率控制/库存限制            | 1. 实现随机抽奖/固定概率抽奖算法<br>2. 基于Redis做抽奖次数限流          |
| lottery-award       | 奖品管理/发放/补发                   | 1. 奖品分类（实物/虚拟）<br>2. 结合RocketMQ做奖品发放异步通知          |
| lottery-pay         | 支付回调/订单管理                    | 1. 对接支付网关（如支付宝/微信）<br>2. 基于Kafka做支付结果异步通知      |
| lottery-workflow    | 活动审批/奖品发放流程                | 基于Activiti 7设计工作流（如活动上线审批、奖品补发审批）                |
| lottery-file        | 奖品图片/活动海报存储                | 基于MinIO实现文件上传/下载/签名访问                                    |
| lottery-monitor     | 系统监控/告警                        | 基于Prometheus+Grafana实现服务指标监控，配置链路追踪告警                |

---

## 二、快速落地的Skill实践（从0到1验证）
### 1. 本地环境一键启动（基础技能）
#### （1）前端启动
```bash
# 1. 安装依赖（走私有Nexus源）
npm install
# 2. 启动开发服务（默认端口9510）
npm run dev
# 3. （可选）构建生产包
npm run build
```

#### （2）后端启动
```powershell
# 1. 编译整个后端
cd lottery-platform-backend
mvn -q -DskipTests compile

# 2. 启动网关（核心入口）
mvn -pl lottery-gateway spring-boot:run

# 3. 启动用户服务（带依赖模块）
mvn -pl lottery-user -am spring-boot:run

# 4. 验证服务联通
curl http://localhost:9101/api/user/ping
# 预期返回：{"code":"0000","message":"success","data":{"service":"lottery-user","status":"UP"},"timestamp":...}
```

### 2. 核心功能最小化实现（进阶技能）
以“用户参与抽奖”为例，落地核心链路：
#### 步骤1：用户认证（lottery-user）
```java
// 补充JWT登录接口（lottery-user模块）
@RestController
@RequestMapping("/api/user")
public class UserController {
    @PostMapping("/login")
    public ResponseResult<JwtToken> login(@RequestBody LoginDTO loginDTO) {
        // 1. 验证用户名密码（模拟）
        // 2. 生成JWT Token（基于Sa-Token/JJWT）
        // 3. 返回Token给前端
        return ResponseResult.success(new JwtToken("xxx-token-xxx"));
    }
}
```

#### 步骤2：抽奖接口（lottery-lottery）
```java
// 抽奖核心接口（lottery-lottery模块）
@RestController
@RequestMapping("/api/lottery")
public class LotteryController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @PostMapping("/draw")
    @PreAuthorize("isAuthenticated()") // 需认证
    public ResponseResult<DrawResult> draw(@RequestParam Long activityId) {
        // 1. 校验用户抽奖次数（Redis计数）
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String countKey = "lottery:count:" + userId + ":" + activityId;
        Integer count = redisTemplate.opsForValue().get(countKey) != null ? 
            Integer.parseInt(redisTemplate.opsForValue().get(countKey)) : 0;
        if (count >= 3) { // 限制每日3次
            return ResponseResult.fail("今日抽奖次数已用完");
        }
        
        // 2. 简单随机抽奖算法
        String[] awards = {"谢谢参与", "10元优惠券", "一等奖（手机）"};
        String result = awards[new Random().nextInt(awards.length)];
        
        // 3. 更新抽奖次数
        redisTemplate.opsForValue().increment(countKey, 1);
        redisTemplate.expire(countKey, 1, TimeUnit.DAYS);
        
        return ResponseResult.success(new DrawResult(activityId, result));
    }
}
```

#### 步骤3：前端调用（核心交互）
```javascript
// 前端抽奖请求（axios示例）
async function drawLottery(activityId) {
  const token = localStorage.getItem('jwt-token');
  try {
    const res = await axios.post('/lottery/api/lottery/draw', { activityId }, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (res.data.code === '0000') {
      alert(`抽奖结果：${res.data.data.result}`);
    } else {
      alert(res.data.message);
    }
  } catch (err) {
    console.error('抽奖失败：', err);
  }
}
```

---

## 三、进阶优化Skill（项目迭代方向）
### 1. 工程化能力升级
- **CI/CD流水线**：基于Jenkins完善“构建→测试→部署”闭环（参考`Jenkins使用说明.md`）
- **配置外置化**：将数据库/中间件/Nacos配置抽离到配置中心，支持多环境切换
- **容器化部署**：编写统一的`docker-compose.yml`，一键启动前后端+所有中间件

### 2. 业务能力完善
- **核心链路闭环**：打通“用户登录→活动参与→抽奖→奖品发放→支付（可选）→日志监控”全流程
- **高并发优化**：
  - 抽奖接口：Redis分布式锁防重复抽奖、RocketMQ异步发放奖品
  - 活动接口：本地缓存+Redis二级缓存减轻数据库压力
- **异常处理**：基于`lottery-common`的统一异常处理，补充业务异常码（如抽奖次数用尽、活动已下架）

### 3. 可观测性增强
- **链路追踪**：接入SkyWalking，实现全链路追踪（前端→网关→微服务→中间件）
- **监控告警**：基于Prometheus+Grafana配置核心指标告警（如接口响应时间、抽奖QPS、Redis缓存命中率）
- **日志治理**：统一日志格式（包含traceId），接入ELK实现日志检索

---

## 四、技能落地优先级建议
1. **基础层**：先跑通前后端本地环境（前端dev服务+后端网关+1个业务服务），验证`ping`接口和Swagger文档
2. **核心业务层**：优先实现“用户登录→抽奖→奖品查询”最小闭环
3. **工程化层**：接入Docker+Nginx部署、Jenkins流水线
4. **优化层**：高并发/可观测性/配置中心等进阶能力

