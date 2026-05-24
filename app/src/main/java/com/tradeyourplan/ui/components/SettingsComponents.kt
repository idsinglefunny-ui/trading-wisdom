// app/src/main/java/com/tradeyourplan/ui/components/SettingsComponents.kt
package com.tradeyourplan.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.notification.NotificationHelper
import com.tradeyourplan.notification.QuoteReminderService
import com.tradeyourplan.ui.theme.ThemeMode

@Composable
fun SettingsSection(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ThemePicker(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            ThemeOption(
                name = mode.displayName,
                selected = currentTheme == mode,
                onClick = { onThemeSelected(mode) }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (selected) it else it.clickable(onClick = onClick) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name)
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选择",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun QuoteSourcePicker(
    currentSource: String,
    onSourceSelected: (String) -> Unit
) {
    val sources = listOf(
        "SYSTEM" to "内置",
        "USER" to "用户",
        "MIXED" to "混合"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sources.forEach { (value, name) ->
            ThemeOption(
                name = name,
                selected = currentSource == value,
                onClick = { onSourceSelected(value) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Category, MarketType) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.DISCIPLINE) }
    var selectedMarketType by remember { mutableStateOf(MarketType.GENERAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加语录") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("语录内容") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Text("分类", style = MaterialTheme.typography.bodySmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Category.entries.forEach { category ->
                        val shortName = when (category) {
                            Category.DISCIPLINE -> "纪律"
                            Category.RISK_MGMT -> "风控"
                            Category.MINDSET -> "心态"
                            Category.TECHNICAL -> "技术"
                        }
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(shortName, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Text("市场类型", style = MaterialTheme.typography.bodySmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MarketType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMarketType == type,
                            onClick = { selectedMarketType = type },
                            label = { Text(type.displayName, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(content, selectedCategory, selectedMarketType)
                    }
                },
                enabled = content.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun NotificationPreview(context: Context) {
    data class LevelInfo(
        val level: NotificationLevel,
        val name: String,
        val description: String,
        val icon: ImageVector
    )

    val levels = listOf(
        LevelInfo(NotificationLevel.SILENT, "静默", "无声音、无震动，仅通知栏提示", Icons.Default.NotificationsOff),
        LevelInfo(NotificationLevel.NORMAL, "标准", "通知铃声 + 短震动", Icons.Default.Notifications),
        LevelInfo(NotificationLevel.FULL_SCREEN, "强提醒", "大声 + 长震动 + 锁屏显示", Icons.Default.VolumeUp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "点击下方按钮预览不同提醒效果",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        levels.forEach { info ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val helper = NotificationHelper(context)
                        helper.cancelPreview()
                        helper.preview(info.level)
                        // 启动跟随主题的 Activity 进行预览
                        val intent = android.content.Intent(context, com.tradeyourplan.notification.QuoteReminderActivity::class.java)
                        intent.putExtra(com.tradeyourplan.notification.QuoteReminderActivity.EXTRA_QUOTE_TEXT, "这是一条${info.name}提醒预览")
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = info.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        info.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = {
                    val helper = NotificationHelper(context)
                    helper.cancelPreview()
                    helper.preview(info.level)
                    // 启动跟随主题的 Activity 进行预览
                    val intent = android.content.Intent(context, com.tradeyourplan.notification.QuoteReminderActivity::class.java)
                    intent.putExtra(com.tradeyourplan.notification.QuoteReminderActivity.EXTRA_QUOTE_TEXT, "这是一条${info.name}提醒预览")
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(intent)
                }) {
                    Text("试一下")
                }
            }
            if (info.level != NotificationLevel.FULL_SCREEN) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
