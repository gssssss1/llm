# LangGraph Web UI 界面说明

## 界面截图说明

由于无法提供实际截图，这里详细描述各个界面的外观和功能。

## 主页 (/)

### 顶部导航栏
- 深色背景 (#343a40)
- 左侧：Logo图标 + "LangGraph Web UI"
- 右侧：导航链接
  - Home（高亮）
  - Executions

### Hero区域
- 紫色渐变背景 (#667eea → #764ba2)
- 白色大标题："LangGraph Dashboard"
- 副标题："Visualize and monitor your workflow graphs in real-time"

### 功能卡片区（3列）

**卡片1: Graph Visualization**
- 蓝色眼睛图标（大）
- 标题：Graph Visualization
- 描述：View your workflow graphs with interactive diagrams

**卡片2: Real-time Execution**
- 绿色播放图标（大）
- 标题：Real-time Execution  
- 描述：Monitor workflow execution with live updates

**卡片3: Execution Logs**
- 青色日志图标（大）
- 标题：Execution Logs
- 描述：Track detailed logs for each node execution

### Graph列表区域
- 蓝色头部："Available Graphs"
- 卡片式列表，每个Graph一张卡片
  - Graph ID
  - "Click to view and execute"文字
  - 蓝色"Graph"标签
  - 右箭头图标
- 悬停效果：卡片上浮，阴影加深

### 页脚
- 深色背景
- 白色文字："LangGraph Web UI © 2024"

---

## Graph详情页 (/graph/{id})

### 左侧面板（70%宽度）

#### Graph可视化卡片
- 蓝色头部："Graph Visualization"
- 白色内容区
- Mermaid图表显示：
  - 绿色圆圈：START节点
  - 粉色圆圈：END节点
  - 方形节点：普通节点
  - 实线箭头：静态边
  - 虚线箭头：条件边
  - 点线箭头：并行边

#### Graph信息卡片
- 青色头部："Graph Information"
- 2列布局显示：
  - Start Node: xxx
  - End Node: xxx
  - Total Nodes: n
  - Total Edges: n

### 右侧面板（30%宽度）

#### 统计卡片
- 紫色渐变背景
- 白色文字
- 6格布局：
  - 上行：Nodes数量 | Edges数量
  - 下行：Static | Conditional | Parallel

#### 执行控制卡片
- 绿色头部："Execute Graph"
- 文本框：输入初始状态JSON
  - 默认值：{}
  - 占4行高度
- 绿色按钮："Execute"（全宽）
- 执行状态显示区（动态）

#### 实时日志卡片
- 深色头部："Execution Log" + "Real-time"标签
- 滚动区域（最大400px）
- 日志条目格式：
  - 蓝色左边框：节点开始
  - 绿色左边框：节点完成
  - 红色左边框：节点失败
  - 每条包含：
    - 图标 + 节点名
    - 时间戳
    - 消息文本

---

## 执行历史页 (/executions)

### 顶部操作栏
- 左侧：标题 "Execution History"
- 右侧：
  - 蓝色"Refresh"按钮
  - 红色"Clear All"按钮

### 执行卡片网格（响应式）
每个执行一张卡片：
- 执行ID（截断显示）
- 开始时间
- 事件数量（彩色标签）
- 最终状态标签
- 悬停效果：卡片上浮

### 点击卡片后弹出Modal

#### Modal标题
- "Execution Logs: {executionId}"

#### Modal内容（滚动）
每个日志事件：
- 编号 + 事件类型 + 节点名
- 时间戳
- 消息内容
- 可展开的数据详情（JSON格式）

#### 颜色编码
- 蓝色背景：STARTED事件
- 绿色背景：COMPLETED事件
- 红色背景：FAILED事件

---

## 交互效果

### 动画效果
1. **卡片悬停**
   - 上移5px
   - 阴影加深
   - 过渡时间：0.3s

2. **日志条目出现**
   - 从左侧滑入
   - 透明度渐变
   - 动画时长：0.3s

3. **加载状态**
   - Bootstrap旋转加载图标
   - "Loading..."文字提示

### 实时更新
1. **WebSocket连接指示**
   - 连接成功：控制台日志
   - 连接失败：红色警告提示

2. **新日志到达**
   - 自动滚动到底部
   - 淡入动画
   - 声音提示（可选）

### 响应式布局
- **大屏幕（>992px）**
  - Graph详情页：左右分栏（8:4）
  - 执行卡片：3列
  
- **中等屏幕（768-992px）**
  - Graph详情页：左右分栏（7:5）
  - 执行卡片：2列
  
- **小屏幕（<768px）**
  - Graph详情页：上下堆叠
  - 执行卡片：1列

---

## 颜色方案

### 主色调
- **Primary**: #0d6efd (蓝色)
- **Success**: #198754 (绿色)
- **Info**: #0dcaf0 (青色)
- **Warning**: #ffc107 (黄色)
- **Danger**: #dc3545 (红色)
- **Dark**: #212529 (深灰)

### 渐变色
- **Hero渐变**: #667eea → #764ba2
- **统计卡片**: 同Hero渐变

### 背景色
- **页面背景**: #f8f9fa (浅灰)
- **卡片背景**: #ffffff (白色)
- **深色元素**: #343a40 (深灰)

---

## 图标系统

使用Bootstrap Icons 1.11.0：
- `bi-diagram-3`: Graph图标
- `bi-play-circle`: 执行/开始
- `bi-check-circle`: 完成/成功
- `bi-x-circle`: 失败/错误
- `bi-clock-history`: 历史记录
- `bi-journal-text`: 日志文本
- `bi-arrow-right-circle`: 导航箭头
- `bi-trash`: 删除操作

---

## 用户体验优化

### 加载状态
1. **初始加载**
   - 旋转加载图标
   - 提示文字
   - 灰色占位符

2. **数据刷新**
   - 按钮禁用
   - 图标旋转
   - 完成提示

### 错误处理
1. **网络错误**
   - 红色警告框
   - 错误图标
   - 错误消息
   - 重试按钮

2. **数据验证**
   - JSON格式检查
   - 必填项提示
   - 实时验证反馈

### 操作反馈
1. **成功操作**
   - 绿色Toast提示
   - 自动消失（3秒）
   - 成功音效（可选）

2. **确认对话框**
   - 删除操作前确认
   - 清空操作前确认
   - 明确的Yes/No按钮

---

## 可访问性

### 键盘导航
- Tab键切换焦点
- Enter键执行操作
- Esc键关闭Modal

### 屏幕阅读器
- 语义化HTML标签
- ARIA标签
- 有意义的alt文本

### 颜色对比
- 符合WCAG 2.1 AA标准
- 文字对比度 >4.5:1
- 大文字对比度 >3:1

---

## 性能优化

### 前端优化
1. **资源加载**
   - CDN加速
   - 压缩资源
   - 延迟加载

2. **渲染优化**
   - 虚拟滚动（大列表）
   - 防抖/节流
   - RequestAnimationFrame

### 后端优化
1. **WebSocket**
   - 心跳保活
   - 断线重连
   - 消息队列

2. **数据传输**
   - JSON压缩
   - 增量更新
   - 分页加载

---

这个Web UI设计注重**实用性**、**美观性**和**响应性**，为用户提供流畅的Graph管理和监控体验。
