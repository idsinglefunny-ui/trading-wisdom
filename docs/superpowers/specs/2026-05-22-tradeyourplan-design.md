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

## 后续步骤

本设计文档是实施的基础。下一阶段是创建详细的实施计划，将工作分解为可执行的任务。
