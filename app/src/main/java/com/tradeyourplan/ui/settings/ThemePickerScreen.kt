package com.tradeyourplan.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val currentTheme by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择主题") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "选择你喜欢的主题风格",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ThemeCard(
                name = "专业深色",
                description = "深色调，专业金融风格",
                previewColors = listOf(Primary, Secondary, Accent),
                isSelected = currentTheme == ThemeMode.PROFESSIONAL_DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.PROFESSIONAL_DARK) }
            )

            ThemeCard(
                name = "温馨鼓励",
                description = "暖色调，温暖鼓励",
                previewColors = listOf(WarmPrimary, WarmSecondary, WarmAccent),
                isSelected = currentTheme == ThemeMode.WARM_ENCOURAGING,
                onClick = { viewModel.setThemeMode(ThemeMode.WARM_ENCOURAGING) }
            )

            ThemeCard(
                name = "极简浅色",
                description = "浅色调，清爽简约",
                previewColors = listOf(LightPrimary, LightSecondary, LightAccent),
                isSelected = currentTheme == ThemeMode.MINIMAL_LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.MINIMAL_LIGHT) }
            )
        }
    }
}

@Composable
private fun ThemeCard(
    name: String,
    description: String,
    previewColors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                previewColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color, RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(2f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
