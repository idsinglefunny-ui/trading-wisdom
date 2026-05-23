// app/src/main/java/com/tradeyourplan/ui/components/SettingsComponents.kt
package com.tradeyourplan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
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
