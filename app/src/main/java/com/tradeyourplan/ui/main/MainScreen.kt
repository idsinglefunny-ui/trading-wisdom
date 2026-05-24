// app/src/main/java/com/tradeyourplan/ui/main/MainScreen.kt
package com.tradeyourplan.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.ui.alarm.AlarmViewModel
import com.tradeyourplan.ui.components.*
import com.tradeyourplan.ui.components.hasAllRequiredPermissions
import com.tradeyourplan.ui.components.hasNotificationPermission
import com.tradeyourplan.ui.components.hasOverlayPermission
import com.tradeyourplan.ui.quote.QuoteViewModel
import com.tradeyourplan.ui.settings.SettingsViewModel
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.app.AlarmManager
import android.app.PendingIntent
import android.widget.Toast
import com.tradeyourplan.notification.AlarmReceiver
import com.tradeyourplan.notification.QuoteReminderService
import com.tradeyourplan.ui.theme.ThemeMode
import com.tradeyourplan.utils.ShareHelper
import kotlinx.coroutines.launch

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    quoteViewModel: QuoteViewModel = hiltViewModel(),
    alarmViewModel: AlarmViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // Permission state
    var showPermissionDialog by remember { mutableStateOf(!hasAllRequiredPermissions(context)) }

    // Re-check on resume
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (!hasAllRequiredPermissions(context)) {
                    showPermissionDialog = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val tabs = listOf(
        BottomNavItem("home", Icons.Default.Home, "首页"),
        BottomNavItem("quotes", Icons.Default.FormatQuote, "语录"),
        BottomNavItem("alarms", Icons.Default.Alarm, "闹钟"),
        BottomNavItem("settings", Icons.Default.Settings, "设置")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tabs[selectedTab].label) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = tabs,
                selectedTab = selectedTab,
                onItemClicked = { index -> selectedTab = index }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeTab(
                    viewModel = mainViewModel,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> QuotesTab(
                    viewModel = quoteViewModel,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> AlarmsTab(
                    viewModel = alarmViewModel,
                    modifier = Modifier.fillMaxSize(),
                    onRequestPermission = { showPermissionDialog = true }
                )
                3 -> SettingsTab(
                    viewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Permission guide dialog
    if (showPermissionDialog) {
        PermissionGuideDialog(
            onDismiss = { showPermissionDialog = false }
        )
    }
}

@Composable
private fun HomeTab(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState(initial = ThemeMode.PROFESSIONAL_DARK)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is MainUiState.Loading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is MainUiState.Success -> {
                val quote = (uiState as MainUiState.Success).quote
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuoteCard(
                        quote = quote,
                        onFavoriteClick = { viewModel.toggleFavorite(quote.id) },
                        onShareClick = {
                            coroutineScope.launch {
                                val shareIntent = ShareHelper.shareQuoteAsImage(
                                    context,
                                    quote.content,
                                    themeMode
                                )
                                val chooser = ShareHelper.createShareChooser(shareIntent, "分享语录")
                                context.startActivity(chooser)
                            }
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                    TYPButton(
                        onClick = { viewModel.loadRandomQuote() },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        icon = { Icon(Icons.Default.Refresh, null) },
                        text = "换一换"
                    )
                }
            }
            is MainUiState.Empty -> {
                Text(
                    (uiState as MainUiState.Empty).message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuotesTab(
    viewModel: QuoteViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val quotes by viewModel.quotes.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState(initial = ThemeMode.PROFESSIONAL_DARK)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier) {
        if (quotes.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Default.FormatQuote,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                title = "暂无语录",
                message = "点击下方按钮添加你的第一条交易智慧",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 分类筛选 - 使用简短标签
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf(null) + Category.entries
                        categories.forEach { category ->
                            val label = when (category) {
                                null -> "全部"
                                Category.DISCIPLINE -> "纪律"
                                Category.RISK_MGMT -> "风控"
                                Category.MINDSET -> "心态"
                                Category.TECHNICAL -> "技术"
                            }
                            FilterChip(
                                selected = filterCategory == category,
                                onClick = { viewModel.setFilterCategory(category) },
                                label = {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }

                // 语录列表
                items(quotes) { quote ->
                    QuoteCard(
                        quote = quote,
                        onFavoriteClick = { viewModel.toggleFavorite(quote.id) },
                        onShareClick = {
                            coroutineScope.launch {
                                val shareIntent = ShareHelper.shareQuoteAsImage(
                                    context,
                                    quote.content,
                                    themeMode
                                )
                                val chooser = ShareHelper.createShareChooser(shareIntent, "分享语录")
                                context.startActivity(chooser)
                            }
                        }
                    )
                }
            }
        }

        // 添加按钮
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() }
            ) {
                Icon(Icons.Default.Add, "添加语录")
            }
        }
    }

    // 添加语录对话框
    if (viewModel.showAddDialog) {
        AddQuoteDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { content, category, marketType ->
                viewModel.addQuote(content, category, marketType)
            }
        )
    }
}

@Composable
private fun AlarmsTab(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit = {}
) {
    val alarms by viewModel.alarms.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    val context = LocalContext.current

    Box(modifier = modifier) {
        if (alarms.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                title = "暂无提醒",
                message = "设置一个提醒，让交易智慧常伴左右",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { enabled -> viewModel.toggleAlarm(alarm.id, enabled) },
                        onClick = {
                            editingAlarm = alarm
                            showEditDialog = true
                        }
                    )
                }
            }
        }

        // 添加按钮
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (!hasAllRequiredPermissions(context)) {
                        onRequestPermission()
                    } else {
                        editingAlarm = null
                        showEditDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, "添加提醒")
            }
        }
    }

    // 编辑/添加对话框
    if (showEditDialog) {
        AlarmEditDialog(
            alarm = editingAlarm,
            onDismiss = { showEditDialog = false },
            onSave = { alarm ->
                if (alarm.id == 0L) {
                    viewModel.addAlarm(alarm)
                } else {
                    viewModel.updateAlarm(alarm)
                }
                showEditDialog = false
            },
            onDelete = {
                if (editingAlarm != null) {
                    viewModel.deleteAlarm(editingAlarm!!)
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SettingsTab(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val quoteSource by viewModel.quoteSource.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 主题设置
        item {
            SettingsSection(
                title = "主题设置",
                icon = { Icon(Icons.Default.Palette, null) }
            ) {
                ThemePicker(
                    currentTheme = themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )
            }
        }

        // 语录来源设置
        item {
            SettingsSection(
                title = "语录来源",
                icon = { Icon(Icons.Default.List, null) }
            ) {
                QuoteSourcePicker(
                    currentSource = quoteSource,
                    onSourceSelected = { viewModel.setQuoteSource(it) }
                )
            }
        }

        // 权限检查
        item {
            SettingsPermissionSection(context = context)
        }

        // 提醒样式预览
        item {
            SettingsSection(
                title = "提醒样式",
                icon = { Icon(Icons.Default.Notifications, null) }
            ) {
                NotificationPreview(context = context)
            }
        }

        /*
        // 测试弹窗功能
        item {
            SettingsSection(
                title = "测试弹窗",
                icon = { Icon(Icons.Default.BugReport, null) }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "如果弹窗不显示，请按以下步骤测试：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            // 启动跟随主题的 Activity 进行预览
                            val intent = Intent(context, com.tradeyourplan.notification.QuoteReminderActivity::class.java)
                            intent.putExtra(com.tradeyourplan.notification.QuoteReminderActivity.EXTRA_QUOTE_TEXT, "这是一条主题跟随的弹窗预览")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("立即测试弹窗（跟随主题）")
                    }
                }
            }
        }
        */

        // 关于
        item {
            SettingsSection(
                title = "关于",
                icon = { Icon(Icons.Default.Info, null) }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("交易智慧", style = MaterialTheme.typography.titleMedium)
                    Text("版本 1.0.0", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "让交易智慧常伴左右，帮助投资者建立正确的交易心态和纪律。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsPermissionSection(context: Context) {
    var hasNotif by remember { mutableStateOf(hasNotificationPermission(context)) }
    var hasOverlay by remember { mutableStateOf(hasOverlayPermission(context)) }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasNotif = hasNotificationPermission(context)
                hasOverlay = hasOverlayPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allGranted = hasNotif && hasOverlay

    SettingsSection(
        title = "权限状态",
        icon = { Icon(Icons.Default.Security, null) }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Notification status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (hasNotif) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error
                )
                Text(
                    if (hasNotif) "通知权限：已授权" else "通知权限：未授权",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasNotif) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.error
                )
            }

            // Overlay status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (hasOverlay) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error
                )
                Text(
                    if (hasOverlay) "悬浮窗权限：已授权" else "悬浮窗权限：未授权",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasOverlay) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.error
                )
            }

            if (!allGranted) {
                TYPButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    },
                    text = "去设置权限"
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    selectedTab: Int,
    onItemClicked: (Int) -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = selectedTab == index,
                onClick = { onItemClicked(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
