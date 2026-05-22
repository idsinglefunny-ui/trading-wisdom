# TradeYourPlan 设计系统

## 设计原则

1. **专业可信** - 金融投资者需要专业感，深色调传达信任
2. **鼓励支持** - 温暖的辅助色减轻交易压力
3. **极简高效** - 扁平设计，快速加载，聚焦内容
4. **高度可定制** - 支持多主题，满足不同偏好

---

## 颜色系统

### 主题 1：专业深色（默认）

```
┌────────────────────────────────────────────────────────────┐
│ 颜色Token          │ Hex值      │ 用途                      │
├────────────────────────────────────────────────────────────┤
│ --color-primary    │ #1E3A5F    │ 主色 - 深海军蓝，专业信任  │
│ --color-on-primary │ #FFFFFF    │ 主色上的文字              │
│ --color-secondary  │ #2563EB    │ 次色 - 蓝色，科技感        │
│ --color-accent     │ #059669    │ 强调色 - 绿色，盈利成功    │
│ --color-background │ #0F172A    │ 背景色 - 深灰蓝           │
│ --color-foreground │ #F8FAFC    │ 前景色 - 主要文字          │
│ --color-surface    │ #1E293B    │ 卡片/表面色               │
│ --color-muted      │ #334155    │ 次要文字/边框             │
│ --color-border     │ #334155    │ 边框线                    │
│ --color-destructive│ #DC2626    │ 警告/删除色               │
└────────────────────────────────────────────────────────────┘
```

### 主题 2：温馨鼓励

```
┌────────────────────────────────────────────────────────────┐
│ 颜色Token          │ Hex值      │ 用途                      │
├────────────────────────────────────────────────────────────┤
│ --color-primary    │ #0F766E    │ 主色 - 深青色，稳重        │
│ --color-on-primary │ #FFFFFF    │ 主色上的文字              │
│ --color-secondary  │ #14B8A6    │ 次色 - 浅青色             │
│ --color-accent     │ #F59E0B    │ 强调色 - 金色，温暖        │
│ --color-background │ #FFFBEB    │ 背景色 - 暖米白           │
│ --color-foreground │ #1E293B    │ 前景色 - 主要文字          │
│ --color-surface    │ #FFFFFF    │ 卡片/表面色               │
│ --color-muted      │ #CCD5AE    │ 次要文字/边框             │
│ --color-border     │ #99F6E4    │ 边框线                    │
│ --color-destructive│ #DC2626    │ 警告/删除色               │
└────────────────────────────────────────────────────────────┘
```

### 主题 3：极简浅色

```
┌────────────────────────────────────────────────────────────┐
│ 颜色Token          │ Hex值      │ 用途                      │
├────────────────────────────────────────────────────────────┤
│ --color-primary    │ #0F172A    │ 主色 - 深灰               │
│ --color-on-primary │ #FFFFFF    │ 主色上的文字              │
│ --color-secondary  │ #334155    │ 次色 - 中灰               │
│ --color-accent     │ #0369A1    │ 强调色 - 亮蓝             │
│ --color-background │ #F8FAFC    │ 背景色 - 浅灰白           │
│ --color-foreground │ #020617    │ 前景色 - 主要文字          │
│ --color-surface    │ #FFFFFF    │ 卡片/表面色               │
│ --color-muted      │ #94A3B8    │ 次要文字/边框             │
│ --color-border     │ #E2E8F0    │ 边框线                    │
│ --color-destructive│ #DC2626    │ 警告/删除色               │
└────────────────────────────────────────────────────────────┘
```

### 语义色映射

```kotlin
// Material 3 Color Scheme 映射
primary = Color(0xFF1E3A5F)        // 主按钮、导航激活
onPrimary = Color(0xFFFFFFFF)      // 主色上的文字
primaryContainer = Color(0xFF2563EB)
onPrimaryContainer = Color(0xFFFFFFFF)

secondary = Color(0xFF2563EB)      // 次要操作、标签
onSecondary = Color(0xFFFFFFFF)

tertiary = Color(0xFF059669)       // 强调、成功状态
onTertiary = Color(0xFFFFFFFF)

background = Color(0xFF0F172A)     // 页面背景
onBackground = Color(0xFFF8FAFC)   // 背景上的主要文字

surface = Color(0xFF1E293B)        // 卡片、对话框
onSurface = Color(0xFFF8FAFC)      // 表面上的文字

surfaceVariant = Color(0xFF334155) // 次要表面
onSurfaceVariant = Color(0xFF94A3B8)

error = Color(0xFFDC2626)          // 错误、删除
onError = Color(0xFFFFFFFF)
errorContainer = Color(0xFFFEE2E2)
onErrorContainer = Color(0xFF991B1B)

outline = Color(0xFF334155)        // 边框、分割线
outlineVariant = Color(0xFF475569)
```

---

## 字体系统

### 字体家族

**Inter** - 现代、高可读性无衬线字体

- Google Fonts: https://fonts.google.com/share?selection?family=Inter:wght@300;400;500;600;700
- 字体风格: dark, cinematic, technical, precision, clean, premium

### 字阶

```kotlin
// Compose Typography
val Typography = Typography(
    // 大标题 - 页面主标题
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),

    // 中标题 - 区块标题
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.sp
    ),

    // 小标题 - 卡片标题
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    ),

    // 正文 - 主要内容
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // 辅助文字 - 说明文字
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp
    ),

    // 标签/按钮
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
```

### 字阶使用规范

| 元素 | 使用字阶 | 字重 | 大小 |
|------|----------|------|------|
| 欢迎页大标题 | displayLarge | Bold | 32sp |
| 页面标题 | headlineMedium | SemiBold | 24sp |
| 卡片标题 | titleMedium | Medium | 18sp |
| 语录内容 | bodyLarge | Regular | 16sp |
| 辅助说明 | bodySmall | Regular | 14sp |
| 按钮/标签 | labelMedium | Medium | 14sp |

---

## 间距系统

基于 **8dp** 网格的间距系统：

```kotlin
object Spacing {
    val xs = 4.dp   // 紧凑元素内间距
    val sm = 8.dp   // 相关元素间最小间距
    val md = 16.dp  // 标准间距、卡片内边距
    val lg = 24.dp  // 区块间距
    val xl = 32.dp  // 大区块间距
    val xxl = 48.dp // 页面级间距
}
```

### 使用场景

| 场景 | 间距值 |
|------|--------|
| 图标与文字（按钮内） | xs (4dp) |
| 列表项内边距 | md (16dp) |
| 卡片内边距 | md (16dp) |
| 区块之间 | lg (24dp) |
| 页面边缘 | md (16dp) |
| 页面顶部（含状态栏） | lg+ (16dp + 状态栏) |

---

## 圆角系统

```kotlin
object CornerRadius {
    val sm = 4.dp   // 小元素、标签
    val md = 8.dp   // 按钮、输入框
    val lg = 12dp  // 卡片
    val xl = 16dp  // 对话框、底部表单
    val full = 50.percent // 全圆角（胶囊形）
}
```

---

## 触摸目标规范

### 最小尺寸

- **Android:** 48×48dp 最小
- **iOS:** 44×44pt 最小
- **相邻间距:** 8dp 最小

### 实现

```kotlin
// 视觉尺寸小，点击区域自动扩展
Modifier.size(24.dp)
    .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClickLabel = "添加"
    ) { ... }  // 系统自动扩展到 48dp
```

---

## 动画系统

### 时长标准

| 动画类型 | 时长 | 缓动 |
|----------|------|------|
| 按压反馈 | 100ms | EaseOut |
| 淡入淡出 | 150ms | Linear |
| 展开/收起 | 250ms | EaseOut |
| 页面转场 | 300ms | EaseInOut |

### Compose 动画

```kotlin
// 淡入淡出
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn(tween(150)),
    exit = fadeOut(tween(150))
)

// 展开/收起
AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically(tween(250, easing = EaseOut)),
    exit = shrinkVertically(tween(250, easing = EaseOut))
)

// 页面转场
composable(
    "detail",
    enterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, easing = EaseInOut)
        )
    }
)
```

### 尊重减少动画设置

```kotlin
// 检查系统设置
val reduceAnimations by Settings.System.getFloat(
    context.contentResolver,
    Settings.System.ANIMATOR_DURATION_SCALE,
    1f
).remember { mutableStateOf(1f) }

val animationDuration = if (reduceAnimations == 0f) 0 else 300
```

---

## 组件规范

### 按钮

#### 主要按钮

```kotlin
Button(
    onClick = { ... },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.height(48.dp)
) {
    Text("确定", style = MaterialTheme.typography.labelMedium)
}
```

#### 次要按钮

```kotlin
OutlinedButton(
    onClick = { ... },
    colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    ),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.height(48.dp)
) {
    Text("取消", style = MaterialTheme.typography.labelMedium)
}
```

#### 文字按钮

```kotlin
TextButton(
    onClick = { ... },
    colors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    ),
    modifier = Modifier.height(40.dp)
) {
    Text("了解更多", style = MaterialTheme.typography.labelMedium)
}
```

### 卡片

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(0.dp)  // 扁平设计
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 卡片内容
    }
}
```

### 底部导航

```kotlin
NavigationRail(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
) {
    destinations.forEach { destination ->
        NavigationRailItem(
            selected = selected == destination.route,
            onClick = { ... },
            icon = {
                Icon(
                    painter = painterResource(destination.icon),
                    contentDescription = destination.label
                )
            },
            label = {
                Text(
                    destination.label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        )
    }
}
```

### 输入框

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("语录内容") },
    placeholder = { Text("输入交易智慧...") },
    supportingText = { Text("最多 200 个字符") },
    singleLine = false,
    maxLines = 4,
    shape = RoundedCornerShape(8.dp),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )
)
```

### 空状态

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Icon(
        painter = painterResource(R.drawable.empty_quotes),
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.outlineVariant
    )
    Spacer(Modifier.height(16.dp))
    Text(
        "暂无语录",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "点击下方按钮添加你的第一条交易智慧",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(24.dp))
    Button(onClick = { ... }) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(8.dp)))
        Text("添加语录")
    }
}
```

---

## 图标规范

### 图标库

使用 **Material Icons** (Android) 或 **FontAwesome**

### 常用图标

| 功能 | Material Icons | 备注 |
|------|----------------|------|
| 语录 | format_quote | - |
| 闹钟 | alarm / schedule | - |
| 设置 | settings | - |
| 主题 | palette | - |
| 收藏（空） | favorite_border | - |
| 收藏（实） | favorite | - |
| 分享 | share | - |
| 添加 | add | - |
| 编辑 | edit | - |
| 删除 | delete | - |
| 关闭 | close | - |
| 筛选 | filter_list | - |
| 搜索 | search | - |
| 更多 | more_vert | - |

### 图标规范

- **标准尺寸:** 24dp
- **线宽:** 2dp (描边图标)
- **最小触摸区:** 48×48dp
- **禁止使用 Emoji 代替图标**

```kotlin
Icon(
    painter = painterResource(R.drawable.ic_quote),
    contentDescription = "语录",  // 必须提供
    modifier = Modifier.size(24.dp),
    tint = MaterialTheme.colorScheme.primary
)
```

---

## 无障碍规范

### 对比度要求

| 内容类型 | WCAG AA | WCAG AAA |
|----------|---------|----------|
| 正文文字 | 4.5:1 | 7:1 |
| 大文字 (18sp+) | 3:1 | 4.5:1 |
| 图标/图形 | 3:1 | - |

### 内容描述

```kotlin
// 图片
Image(
    painter = painterResource(R.drawable.logo),
    contentDescription = "TradeYourPlan Logo"
)

// 可点击元素
Modifier.clickable { ... }
    .semantics {
        this.contentDescription = "添加新语录"
        this.role = Role.Button
    }

// 状态元素
Modifier.semantics {
    this.contentDescription = "已收藏"
    this.stateDescription = "已激活"
}
```

### 支持屏幕阅读器

- 所有交互元素必须有 `contentDescription`
- 图标按钮必须有文字标签
- 状态变化需要通过 `LiveRegion` 通知

---

## 响应式布局

### 断点

```kotlin
object Breakpoints {
    val Small = 0.dp      // < 360dp  小屏手机
    val Medium = 360.dp   // 360-600dp 标准手机
    val Large = 600.dp    // 600-840dp 大屏手机/小平板
    val XLarge = 840.dp   // > 840dp 平板
}
```

### 布局适配

```kotlin
val windowSize = calculateWindowSizeSize(activity)

when (windowSize.widthSizeClass) {
    WindowWidthSizeClass.Compact -> {
        // 手机布局：单列
    }
    WindowWidthSizeClass.Medium -> {
        // 折叠屏/小平板：两列
    }
    WindowWidthSizeClass.Expanded -> {
        // 平板：侧边栏 + 内容区
    }
}
```

---

## 安全区域适配

```kotlin
// Compose 使用 WindowInsets
Modifier.windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
)

// 底部导航栏
Modifier.windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
)
```

---

## 反模式（避免）

- ❌ 使用 Emoji 代替图标
- ❌ 阴影过重或使用渐变装饰
- ❌ 过度动画（>300ms）
- ❌ 硬编码颜色值（使用设计 token）
- ❌ 触摸目标小于 48dp
- ❌ 相邻触摸目标间距小于 8dp
- ❌ 混合不同图标风格
- ❌ 使用 dp 而非 sp 定义文字大小
- ❌ 障碍对比度不足
- ❌ 缺少 contentDescription

---

## 检查清单

实施前必须确认：

- [ ] 所有颜色使用语义 token，无硬编码
- [ ] 文字大小使用 sp 单位
- [ ] 间距遵循 8dp 网格
- [ ] 触摸目标 ≥48dp
- [ ] 所有图标有 contentDescription
- [ ] 对比度符合 WCAG AA 标准
- [ ] 动画时长 ≤300ms
- [ ] 支持减少动画设置
- [ ] 测试亮色和暗色主题
- [ ] 测试横竖屏切换
