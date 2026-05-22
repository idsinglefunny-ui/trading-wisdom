package com.tradeyourplan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onThemePicker: () -> Unit = {}
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val quoteSource by viewModel.quoteSource.collectAsState()
    val notificationLevel by viewModel.notificationLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主题选择
            SettingItem(
                title = "主题",
                description = getThemeName(themeMode),
                onClick = onThemePicker,
                icon = Icons.Default.Palette
            )

            Divider()

            // 语录来源
            SettingDropdown(
                title = "语录来源",
                selectedValue = quoteSource,
                options = mapOf(
                    "SYSTEM" to "仅系统",
                    "USER" to "仅用户",
                    "MIXED" to "混合"
                ),
                onValueChange = { viewModel.setQuoteSource(it) },
                icon = Icons.Default.FormatQuote
            )

            Divider()

            // 通知强度
            SettingDropdown(
                title = "通知强度",
                selectedValue = notificationLevel,
                options = mapOf(
                    "SILENT" to "静默",
                    "NORMAL" to "标准",
                    "FULL_SCREEN" to "强提醒"
                ),
                onValueChange = { viewModel.setNotificationLevel(it) },
                icon = Icons.Default.Notifications
            )

            Divider()

            // 关于
            SettingItem(
                title = "关于",
                description = "TradeYourPlan v1.0.0",
                onClick = { },
                icon = Icons.Default.Info
            )
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingDropdown(
    title: String,
    selectedValue: String,
    options: Map<String, String>,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Divider()
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .menuAnchor(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        options[selectedValue] ?: selectedValue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onValueChange(key)
                                expanded = false
                            },
                            leadingIcon = if (key == selectedValue) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

private fun getThemeName(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.PROFESSIONAL_DARK -> "专业深色"
        ThemeMode.WARM_ENCOURAGING -> "温馨鼓励"
        ThemeMode.MINIMAL_LIGHT -> "极简浅色"
    }
}
