# LangGraph Web UI 使用指南

## 功能概述

LangGraph Web UI 提供了一个基于Spring Boot的可视化界面，用于：

1. **查看Graph结构** - 使用Mermaid图表可视化工作流
2. **实时执行监控** - 通过WebSocket实时查看执行进度
3. **详细日志记录** - 查看每个节点的执行日志和状态数据
4. **执行历史** - 浏览和管理所有执行记录

## 快速开始

### 1. 启动Web UI

运行WebUIDemo启动类：

```bash
mvn spring-boot:run -Dstart-class=com.langgraph.web.WebUIDemo
```

或者使用IDE直接运行 `WebUIDemo.java` 主类。

### 2. 访问Web界面

启动成功后，访问：

```
http://localhost:8080
```

## 功能说明

### 主页 (/)

主页显示所有可用的Graph列表：

- **查看Graph列表** - 显示所有已注册的工作流图
- **快速导航** - 点击任意Graph卡片进入详细页面

### Graph详情页 (/graph/{id})

Graph详情页提供完整的可视化和执行功能：

#### 左侧面板
- **Graph可视化** - 使用Mermaid显示工作流结构
  - 绿色圆圈：START节点
  - 粉色圆圈：END节点
  - 蓝色箭头：静态边
  - 虚线箭头：条件边
  - 点线箭头：并行边

- **Graph信息** - 显示图的基本信息
  - 起始节点
  - 结束节点
  - 节点总数
  - 边总数

#### 右侧面板
- **统计信息** - 显示Graph统计数据
  - 节点数量
  - 边数量
  - 各类型边的分布

- **执行控制** - 执行Graph
  - 输入初始状态（JSON格式）
  - 点击"Execute"按钮启动
  - 显示执行ID和状态

- **实时日志** - WebSocket实时推送
  - 蓝色：节点开始执行
  - 绿色：节点执行成功
  - 红色：节点执行失败
  - 灰色：其他事件

### 执行历史页 (/executions)

查看所有执行记录：

- **执行列表** - 显示所有执行的摘要
  - 执行ID
  - 开始时间
  - 事件数量
  - 最终状态

- **详细日志** - 点击任意执行查看完整日志
  - 时间戳
  - 事件类型
  - 节点名称
  - 消息内容
  - 状态数据（可展开查看JSON）

- **管理操作**
  - 刷新列表
  - 删除单个执行
  - 清空所有记录

## API接口

Web UI提供REST API供程序化访问：

### Graph管理

```bash
# 获取所有Graph
GET /api/graphs

# 获取Graph详情
GET /api/graphs/{id}

# 获取Mermaid图表
GET /api/graphs/{id}/mermaid

# 执行Graph
POST /api/graphs/{id}/execute
Content-Type: application/json
{
  "key1": "value1",
  "key2": "value2"
}
```

### 执行日志

```bash
# 获取特定执行的日志
GET /api/executions/{executionId}/logs

# 获取所有执行日志
GET /api/executions/logs

# 删除执行日志
DELETE /api/executions/{executionId}/logs
```

## 示例工作流

启动时会自动注册3个示例工作流：

### 1. simple-workflow (简单工作流)

线性执行流程，演示基本的节点链接：
- start → process → decision → process (循环) → end
- 特点：循环执行，计数达到阈值后结束

### 2. conditional-workflow (条件工作流)

基于状态的条件路由：
- analyze → high_value/low_value → finalize
- 特点：根据随机值选择不同的处理路径

### 3. parallel-workflow (并行工作流)

并发执行多个任务：
- prepare → [task_a, task_b, task_c] → merge
- 特点：三个任务并行执行，最后合并结果

## 自定义Graph

### 程序化注册

在代码中注册自定义Graph：

```java
@Autowired
private GraphService graphService;

public void registerCustomGraph() {
    StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
        .addNode("node1", state -> {
            // 节点逻辑
            return new NodeResult<>(state.withData("key", "value"));
        })
        .addNode("node2", state -> {
            // 节点逻辑
            return new NodeResult<>(state);
        })
        .addEdge("node1", "node2")
        .setStartNode("node1")
        .build();
    
    String graphId = graphService.registerGraph("my-graph", graph);
    System.out.println("Graph registered: " + graphId);
}
```

### REST API注册

通过API注册Graph（需要额外实现）：

```java
@PostMapping("/api/graphs")
public ResponseEntity<String> registerGraph(@RequestBody GraphDefinition def) {
    // 构建并注册Graph
    String id = graphService.registerGraph(def.name(), buildGraph(def));
    return ResponseEntity.ok(id);
}
```

## WebSocket连接

前端使用SockJS + STOMP连接WebSocket：

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 订阅特定执行
    stompClient.subscribe('/topic/executions/{executionId}', function(message) {
        const event = JSON.parse(message.body);
        console.log('Event:', event);
    });
    
    // 订阅所有执行
    stompClient.subscribe('/topic/executions/all', function(message) {
        const event = JSON.parse(message.body);
        console.log('Event:', event);
    });
});
```

## 事件类型

执行过程中会触发以下事件：

- **EXECUTION_STARTED** - 执行开始
- **NODE_STARTED** - 节点开始执行
- **NODE_COMPLETED** - 节点执行完成
- **NODE_FAILED** - 节点执行失败
- **EXECUTION_COMPLETED** - 执行成功完成
- **EXECUTION_FAILED** - 执行失败

每个事件包含：
- `executionId` - 执行ID
- `eventType` - 事件类型
- `nodeName` - 节点名称（如适用）
- `timestamp` - 时间戳
- `data` - 状态数据
- `message` - 描述信息

## 配置

### application.yml

```yaml
server:
  port: 8080  # 修改端口

logging:
  level:
    com.langgraph: DEBUG  # 调整日志级别
```

### 自定义配置

创建自定义配置类：

```java
@Configuration
public class CustomWebConfig {
    
    @Bean
    public CheckpointStorage<StateRecord> checkpointStorage() {
        // 使用自定义存储
        return new RedisCheckpointStorage<>();
    }
}
```

## 故障排除

### 问题1：WebSocket连接失败

**症状**：实时日志不更新

**解决**：
- 检查浏览器控制台错误
- 确认WebSocket端点可访问：`ws://localhost:8080/ws`
- 检查防火墙设置

### 问题2：Graph不显示

**症状**：主页显示空列表

**解决**：
- 确认Graph已正确注册
- 检查后台日志
- 访问 `/api/graphs` 检查API响应

### 问题3：执行无响应

**症状**：点击Execute后无反应

**解决**：
- 检查初始状态JSON格式是否正确
- 查看浏览器控制台错误
- 检查后台日志中的异常

## 性能优化

### 日志管理

定期清理旧日志：

```java
@Scheduled(cron = "0 0 * * * *")  // 每小时
public void cleanOldLogs() {
    monitorService.clearAllLogs();
}
```

### 并发限制

限制并发执行数量：

```java
@Bean
public ExecutorService executorService() {
    return Executors.newFixedThreadPool(10);
}
```

## 扩展开发

### 添加自定义事件

```java
public record CustomEvent(
    String executionId,
    String customData
) {}

monitorService.publishEvent(customEvent);
```

### 自定义可视化

修改 `graph-view.html` 中的Mermaid配置：

```javascript
mermaid.initialize({ 
    startOnLoad: true, 
    theme: 'dark',  // 更改主题
    flowchart: {
        curve: 'basis'  // 更改曲线样式
    }
});
```

## 安全建议

### 生产环境

1. **启用认证**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 配置Spring Security
}
```

2. **限制CORS**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://yourdomain.com");
    }
}
```

3. **API限流**
使用Spring Cloud Gateway或自定义拦截器

## 技术栈

- **后端**
  - Spring Boot 3.2.2
  - Spring WebSocket (STOMP)
  - Spring Web MVC
  - Thymeleaf

- **前端**
  - Bootstrap 5.3
  - Mermaid.js 10
  - SockJS + STOMP
  - Vanilla JavaScript

## 下一步

1. **持久化** - 集成数据库存储执行历史
2. **用户管理** - 添加用户认证和授权
3. **更多可视化** - 添加图表和统计面板
4. **导出功能** - 导出日志和报告
5. **通知系统** - 执行完成后发送通知

## 获取帮助

- 查看示例代码：`src/main/java/com/langgraph/examples/`
- 阅读架构文档：`ARCHITECTURE.md`
- 提交Issue：项目GitHub仓库

---

**LangGraph Web UI** - 让工作流可视化和监控变得简单！
