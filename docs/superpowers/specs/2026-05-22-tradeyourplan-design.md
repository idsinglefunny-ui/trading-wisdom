# TradeYourPlan Android App - 设计文档

**日期：** 2026-05-22
**作者：** 投资者提醒应用设计规范
**状态：** 已批准

## 项目概述

TradeYourPlan 是一款面向投资者（股票、期货）的 Android 应用，提供交易智慧和操作提醒，而非实时市场信息。应用通过"计划你的交易，交易你的计划"等经典语录，帮助交易者保持纪律。

**核心理念：** 对高级用户高度可定制，同时对普通用户保持开箱即用的易用性。

---

## 需求

### 功能需求

1. **多种提醒模式**
   - 固定时间提醒（类似闹钟）
   - 时间范围内的随机提醒
   - 事件触发提醒（如打开交易应用后）

2. **可自定义的通知样式**
   - 静默通知
   - 带声音的标准通知
   - 需要交互的全屏提醒

3. **语录管理**
   - 系统预置语录（50-100条）
   - 用户自添加语录
   - 多维度分类（按主题 + 市场类型）
   - 收藏/点赞功能
   - 分享到社交平台

4. **展示偏好**
   - 仅显示用户语录
   - 仅显示系统语录
   - 混合展示（可配置权重）

5. **主题切换**
   - 多套预置主题
   - 设置中自由选择

6. **首次使用引导**
   - 懒人模式：全部默认，立即可用
   - 自定义模式：引导设置偏好

### 非功能需求

- **目标系统：** Android 7.0 (API 24) 及以上
- **性能：** 流畅的UI，最小化电池消耗
- **隐私：** 所有数据本地存储，无需账号
- **开发语言：** Kotlin 原生开发

---

## 架构

### 整体架构：MVVM 单体模块

```
┌─────────────────────────────────────────────────────┐
│                    UI 层                            │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │主界面  │ │语录列表│ │闹钟设置│ │设置   │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
└─────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────┐
│                   ViewModel 层                      │
│  ┌───────────┐ ┌───────────┐ ┌──────────────────┐  │
│  │语录VM  │ │闹钟VM  │ │设置VM    │ │
│  └───────────┘ └───────────┘ └──────────────────┘  │
└─────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────┐
│                    数据层                           │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌────────┐   │
│  │语录仓库│ │闹钟仓库│ │主题仓库│ │分享仓库│ │Room数据库│   │
│  └──────┘ └──────┘ └──────┘ └──────┘ └────────┘   │
└─────────────────────────────────────────────────────┘
```

### 项目结构

```
app/src/main/java/com/tradeyourplan/
├── data/
│   ├── local/           # Room 数据库实体、DAO
│   ├── repository/      # Repository 实现
│   └── model/           # 数据模型
├── domain/
│   ├── usecase/         # 业务逻辑用例
│   └── model/           # 领域模型
├── ui/
│   ├── main/            # 主屏幕
│   ├── quote/           # 语录管理 UI
│   ├── alarm/           # 闹钟设置 UI
│   ├── theme/           # 主题选择 UI
│   └── components/      # 可复用 UI 组件
└── di/                  # Hilt 依赖注入模块
```

---

## 核心模块

### 1. 语录模块 (Quote)

**职责：** 语录增删改查、分类管理、收藏

**数据模型：**
```kotlin
data class Quote(
    id: Long,
    content: String,           // 语录内容
    category: Category,        // 主题分类
    marketType: MarketType,    // 市场类型
    source: QuoteSource,       // 系统/用户
    isFavorite: Boolean,       // 是否收藏
    createTime: Long           // 创建时间戳
)

enum class Category {
    RISK_MGMT,      // 风险管理
    MINDSET,        // 交易心态
    DISCIPLINE,     // 交易纪律
    TECHNICAL       // 技术分析
}

enum class MarketType {
    STOCK,          // 股票专用
    FUTURES,        // 期货专用
    GENERAL         // 通用交易智慧
}

enum class QuoteSource {
    SYSTEM,         // 系统预置
    USER            // 用户添加
}
```

### 2. 闹钟模块 (Alarm)

**职责：** 三种提醒模式的调度管理

**数据模型：**
```kotlin
data class Alarm(
    id: Long,
    type: AlarmType,           // 提醒类型
    timeConfig: TimeConfig,    // 时间配置
    repeatMode: RepeatMode,    // 重复模式
    quoteFilter: QuoteFilter,  // 语录筛选条件
    isEnabled: Boolean,
    notificationLevel: NotificationLevel
)

enum class AlarmType {
    FIXED,              // 固定时间（如 9:30）
    RANDOM,             // 时间范围内随机
    EVENT_TRIGGERED     // 事件触发
}

enum class NotificationLevel {
    SILENT,            // 仅通知栏
    NORMAL,            // 带声音
    FULL_SCREEN        // 全屏提醒
}

data class TimeConfig(
    hour: Int,         // FIXED 类型用
    minute: Int,
    startHour: Int?,   // RANDOM 类型用
    endHour: Int?,
    targetPackage: String?,  // EVENT_TRIGGERED 类型用
    delaySeconds: Int?       // 事件后延迟秒数
)
```

### 3. 主题模块 (Theme)

**职责：** 应用主题和皮肤切换

**预置主题：**
- **专业深色：** 默认主题，深色调，金融终端风格
- **温馨鼓励：** 暖色调，柔和视觉，减轻压力
- **极简浅色：** 大量留白，聚焦内容

### 4. 分享模块 (Share)

**职责：** 分享语录到外部平台

**支持目标：** 微信、朋友圈、系统分享菜单

---

## 用户界面

### 主屏幕布局

```
┌─────────────────────────────────────┐
│         主界面                      │
│  ┌─────────────────────────────┐    │
│  │    今日语录                 │    │
│  │    [换一换]                 │    │
│  └─────────────────────────────┘    │
│  ┌─────────────────────────────┐    │
│  │    [★ 收藏] [分享]          │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐   │
│  │语录 │ │闹钟 │ │主题 │ │设置 │   │
│  │列表 │ │设置 │ │     │ │     │   │
│  └─────┘ └─────┘ └─────┘ └─────┘   │
└─────────────────────────────────────┘
```

### 导航结构

| 界面 | 用途 |
|------|------|
| **MainActivity** | 首页，显示今日语录 |
| **QuoteListActivity** | 浏览、添加、编辑、筛选语录 |
| **AlarmEditActivity** | 创建/编辑提醒设置 |
| **ThemePickerActivity** | 预览和选择主题 |
| **SettingsActivity** | 应用偏好设置 |

### 首次启动流程

```
应用启动
    ↓
欢迎页（简短介绍）
    ↓
选择模式
    ↓
    ┌─────────────────┴─────────────────┐
    │                                   │
懒人模式                          自定义模式
    │                                   │
全部默认应用                      引导设置向导
    │                                   │
    └─────────────────┬─────────────────┘
                      ↓
                   进入主界面
```

---

## 提醒系统实现

### 固定时间提醒

- **API：** `AlarmManager.setExactAndAllowWhileIdle()`
- **场景：** 每天早上 9:30 开盘前提醒
- **配置：** 具体的小时和分钟

### 随机间隔提醒

- **API：** `AlarmManager.setWindow()` + `WorkManager`
- **场景：** 在 9:00-15:00 之间随机提醒
- **配置：** 开始时间、结束时间、频率

### 事件触发提醒

- **API：** `BroadcastReceiver` 监听应用启动
- **场景：** 打开交易应用 5 分钟后提醒
- **配置：** 目标应用包名、延迟秒数

### 通知强度等级

| 等级 | 实现方式 | Android API |
|------|----------|-------------|
| **静默** | 仅通知栏，无声音/震动 | `NotificationCompat` |
| **标准** | 通知栏 + 默认声音 | `NotificationCompat` + sound |
| **全屏** | 全屏 Activity + 声音 + 震动 | `NotificationCompat.FULL_SCREEN` |

### 语录选择逻辑

```
if (用户偏好 == 仅用户语录) {
    从用户添加的语录中选择
} else if (用户偏好 == 仅系统语录) {
    从系统预置语录中选择
} else {
    从混合池中选择
    （可配置系统/用户权重比例）
}
```

---

## 数据存储

### 数据库表结构

```sql
-- 语录表
CREATE TABLE quotes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    content TEXT NOT NULL,
    category TEXT NOT NULL,        -- RISK_MGMT, MINDSET, DISCIPLINE, TECHNICAL
    market_type TEXT NOT NULL,     -- STOCK, FUTURES, GENERAL
    source TEXT NOT NULL,          -- SYSTEM, USER
    is_favorite INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL
);

-- 闹钟表
CREATE TABLE alarms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,            -- FIXED, RANDOM, EVENT_TRIGGERED
    time_config TEXT NOT NULL,     -- JSON 序列化的 TimeConfig
    repeat_mode TEXT NOT NULL,
    quote_filter TEXT NOT NULL,    -- JSON 序列化的筛选条件
    is_enabled INTEGER DEFAULT 1,
    notification_level TEXT NOT NULL
);

-- 设置表
CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL            -- 复杂值使用 JSON
);
```

### 存储技术

- **数据库：** Room (SQLite 封装)
- **轻量配置：** DataStore (替代 SharedPreferences)
- **无云同步：** 全部数据本地存储

---

## 技术栈

| 类别 | 技术 | 用途 |
|------|------|------|
| **语言** | Kotlin | 现代 Android 开发 |
| **最低 SDK** | API 24 (Android 7.0) | 覆盖 98%+ 设备 |
| **目标 SDK** | API 34 | 最新稳定版本 |
| **架构** | MVVM + Repository | 标准 Android 架构 |
| **异步** | Kotlin Coroutines + Flow | 异步编程 |
| **数据库** | Room | 本地持久化 |
| **依赖注入** | Hilt | 依赖注入 |
| **UI** | Jetpack Compose | 现代声明式 UI |
| **导航** | Compose Navigation | 页面导航 |
| **ViewModel** | Jetpack ViewModel | 状态管理 |
| **后台任务** | WorkManager | 定时任务 |

---

## 初始内容

### 预置语录

应用将预置 50-100 条精选交易语录，涵盖：
- 风险管理原则
- 交易心理
- 纪律与耐心
- 技术分析智慧
- 股票专用建议
- 期货专用建议
- 通用交易真理

### 可选更新

应用将检查并从远程源下载新语录（需要服务器维护）。

---

## 功能总结

### 核心功能
1. 三种提醒模式（固定/随机/事件触发）
2. 多维度语录分类
3. 用户创建和管理语录
4. 收藏/点赞系统
5. 分享到社交平台
6. 主题切换
7. 可配置通知强度
8. 懒人模式开箱即用

### 技术亮点
- Kotlin 原生开发
- MVVM + Repository 架构
- Room 本地存储
- Jetpack Compose UI
- 无需账号
- 离线优先设计

---

---

## UI 设计规范

### 设计系统

基于 UI/UX Pro Max 设计系统，TradeYourPlan 采用以下设计语言：

#### 风格定位
- **风格名称：** Flat Design（扁平化设计）
- **关键词：** 2D、极简、大胆配色、无阴影、线条干净、形状简单、聚焦排版
- **适用场景：** 移动应用、跨平台、初创产品、用户友好、SaaS
- **性能：** 优秀 | **无障碍：** WCAG AAA 级别

#### 主题配色

##### 主题 1：专业深色（默认）
```
主色 (Primary):     #1E3A5F  (深海军蓝) - 专业、信任
主色文字:           #FFFFFF
次色 (Secondary):   #2563EB  (蓝色) - 科技感
强调色 (Accent):    #059669  (绿色) - 盈利、成功
背景色:             #0F172A  (深灰蓝)
前景色:             #F8FAFC
卡片背景:           #1E293B
边框色:             #334155
警告色:             #DC2626
```

##### 主题 2：温馨鼓励
```
主色 (Primary):     #0F766E  (深青色) - 稳重、信任
主色文字:           #FFFFFF
次色 (Secondary):   #14B8A6  (浅青色)
强调色 (Accent):    #F59E0B  (金色) - 温暖
背景色:             #FFFBEB  (暖米白)
前景色:             #1E293B
卡片背景:           #FFFFFF
边框色:             #99F6E4
警告色:             #DC2626
```

##### 主题 3：极简浅色
```
主色 (Primary):     #0F172A  (深灰)
主色文字:           #FFFFFF
次色 (Secondary):   #334155  (中灰)
强调色 (Accent):    #0369A1  (亮蓝)
背景色:             #F8FAFC  (浅灰白)
前景色:             #020617
卡片背景:           #FFFFFF
边框色:             #E2E8F0
警告色:             #DC2626
```

#### 字体系统

使用 **Inter** 字体家族，提供现代、专业、高可读性的体验：

| 用途 | 字重 | 大小 (sp) | 行高 |
|------|------|-----------|------|
| 大标题 | Bold (700) | 32 | 1.2 |
| 中标题 | SemiBold (600) | 24 | 1.3 |
| 小标题 | Medium (500) | 18 | 1.4 |
| 正文 | Regular (400) | 16 | 1.5 |
| 辅助文字 | Regular (400) | 14 | 1.5 |
| 标签/按钮 | Medium (500) | 14 | 1.4 |

**字体导入：**
```kotlin
// Compose 中使用
Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
)
```

#### 间距系统

采用 **8dp** 基础间距系统：

| Token | 值 | 用途 |
|-------|-----|------|
| spacing-xs | 4dp | 元素内紧凑间距 |
| spacing-sm | 8dp | 相关元素间最小间距 |
| spacing-md | 16dp | 标准间距、卡片内边距 |
| spacing-lg | 24dp | 区块间距 |
| spacing-xl | 32dp | 大区块间距 |
| spacing-2xl | 48dp | 页面级间距 |

#### 触摸目标规范

遵循 Material Design 和 iOS HIG 标准：

- **最小尺寸：** 48×48dp (Android) / 44×44pt (iOS)
- **图标按钮：** 视觉可较小，但点击区域必须扩展
- **相邻触摸目标间距：** 最少 8dp

```kotlin
// Compose 示例：扩展点击区域
Modifier
    .size(24.dp)           // 视觉尺寸
    .clickable { ... }     // 自动扩展到 48dp 最小点击区
```

#### 动画规范

| 动画类型 | 时长 | 缓动函数 |
|----------|------|----------|
| 按压反馈 | 100ms | easeOutCubic |
| 页面转场 | 300ms | easeInOut |
| 列表项展开 | 250ms | easeOut |
| 淡入淡出 | 150ms | linear |

**尊重用户偏好：**
```kotlin
// 检查减少动画设置
val animationEnabled = ! AnimatedContentTransitionScope::
    class.java.isInstance
```

### 组件规范

#### 按钮

| 类型 | 背景色 | 文字色 | 高度 | 圆角 |
|------|--------|--------|------|------|
| 主要按钮 | 主色 | 白色 | 48dp | 8dp |
| 次要按钮 | 透明/边框 | 主色 | 48dp | 8dp |
| 文字按钮 | 透明 | 主色 | 40dp | 0dp |

**按压状态：** 透明度降低至 0.8

#### 卡片

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(0.dp)  // 扁平设计
)
```

#### 底部导航

- **最多项目：** 5 个
- **每个项目：** 图标 + 文字标签
- **激活状态：** 主色高亮 + 图标填充
- **高度：** 56dp（不含安全区域）

#### 通知栏

| 强度 | 表现 | 图标 | 声音 |
|------|------|------|------|
| 静默 | 小图标 | 仅通知栏 | 无 |
| 标准 | 普通知 | 通知栏 + 可展开 | 系统默认 |
| 强提醒 | 全屏 | 全屏 Activity + 声音震动 | 自定义 |

#### 空状态

```
┌─────────────────────────────────┐
│                                 │
│           [图标 64dp]            │
│                                 │
│         标题 (18sp Medium)       │
│      辅助说明 (14sp Regular)     │
│                                 │
│      [主要按钮]  [次要按钮]      │
│                                 │
└─────────────────────────────────┘
```

**示例文案：**
- 暂无语录："点击下方按钮添加你的第一条交易智慧"
- 暂无提醒："设置一个提醒，让交易智慧常伴左右"

#### 输入框

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("标签") },
    placeholder = { Text("输入标签名称") },
    supportingText = { Text("最多 20 个字符") },
    singleLine = true,
    shape = RoundedCornerShape(8.dp)
)
```

### 图标规范

- **图标库：** Material Icons (Android) 或 FontAwesome
- **尺寸：** 24dp (标准)
- **线宽：** 2dp (描边图标)
- **风格：** 保持一致，不混合填充/描边样式
- **禁止：** 使用 Emoji 代替图标

**常用图标映射：**
| 功能 | 图标名称 |
|------|----------|
| 语录 | format_quote |
| 闹钟 | alarm / schedule |
| 设置 | settings |
| 主题 | palette |
| 收藏 | favorite_border / favorite |
| 分享 | share |
| 添加 | add |
| 编辑 | edit |
| 删除 | delete |
| 关闭 | close |

### 无障碍规范

#### 对比度要求

| 内容类型 | 最低对比度 |
|----------|-----------|
| 正文文字 | 4.5:1 (AA) |
| 大文字 (18sp+) | 3:1 (AA) |
| 图标/图形元素 | 3:1 (AA) |

#### 内容描述

```kotlin
Image(
    painter = painterResource(R.drawable.icon),
    contentDescription = "添加新语录",  // 必须提供
    modifier = Modifier.semantics {
        this.contentDescription = "添加新语录"
        this.role = Role.Button
    }
)
```

#### 动态字体支持

使用 `sp` 单位而非 `dp`，确保用户字体缩放设置生效。

### 响应式布局

#### 断点策略

| 设备类型 | 宽度 | 布局调整 |
|----------|------|----------|
| 小屏手机 | < 360dp | 单列，紧凑间距 |
| 标准手机 | 360-600dp | 单列，标准间距 |
| 大屏手机/小平板 | 600-840dp | 单列，宽松间距 |
| 平板 | > 840dp | 双列（列表 + 详情） |

#### 横屏适配

- 横屏时增加水平内边距，保持内容可读宽度（最大 600dp）
- 底部导航在横屏时保持位置，考虑侧边栏选项（大屏设备）

### 暗色模式

暗色模式使用去饱和、较亮的色调变体，而非简单反转颜色：

| 元素 | 浅色模式 | 暗色模式 |
|------|----------|----------|
| 背景 | #F8FAFC | #0F172A |
| 卡片 | #FFFFFF | #1E293B |
| 主文字 | #0F172A | #F8FAFC |
| 次文字 | #64748B | #94A3B8 |
| 边框 | #E2E8F0 | #334155 |

**重要：** 暗色模式下需独立验证对比度，不能假设浅色模式值适用。

---

## 后续步骤

本设计文档是实施的基础。下一阶段是创建详细的实施计划，将工作分解为可执行的任务。
