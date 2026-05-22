// app/src/main/java/com/tradeyourplan/ui/alarm/AlarmEditScreen.kt
package com.tradeyourplan.ui.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TimePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.*
import com.tradeyourplan.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long = 0,
    viewModel: AlarmViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var selectedType by remember { mutableStateOf(AlarmType.FIXED) }
    var timePickerState by remember {
        mutableStateOf(TimePickerState(9, 30, is24Hour = true))
    }
    var startHour by remember { mutableIntStateOf(9) }
    var endHour by remember { mutableIntStateOf(15) }
    var selectedRepeatMode by remember { mutableStateOf(RepeatMode.DAILY) }
    var selectedNotificationLevel by remember { mutableStateOf(NotificationLevel.NORMAL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == 0L) "添加提醒" else "编辑提醒") },
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
            // 提醒类型选择
            Text("提醒类型", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlarmType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 根据类型显示不同配置
            when (selectedType) {
                AlarmType.FIXED -> {
                    Text("提醒时间", style = MaterialTheme.typography.titleMedium)
                    // 时间选择器（简化版，实际使用 TimePicker）
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = timePickerState.hour.toString(),
                            onValueChange = { },
                            label = { Text("时") },
                            modifier = Modifier.weight(1f),
                            readOnly = true
                        )
                        Text(":")
                        OutlinedTextField(
                            value = timePickerState.minute.toString().padStart(2, '0'),
                            onValueChange = { },
                            label = { Text("分") },
                            modifier = Modifier.weight(1f),
                            readOnly = true
                        )
                    }
                }
                AlarmType.RANDOM -> {
                    Text("时间范围", style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startHour.toString(),
                            onValueChange = { startHour = it.toIntOrNull() ?: 9 },
                            label = { Text("开始时") },
                            modifier = Modifier.weight(1f)
                        )
                        Text("~")
                        OutlinedTextField(
                            value = endHour.toString(),
                            onValueChange = { endHour = it.toIntOrNull() ?: 15 },
                            label = { Text("结束时") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                AlarmType.EVENT_TRIGGERED -> {
                    Text("事件触发", style = MaterialTheme.typography.titleMedium)
                    Text("检测到交易应用启动后触发提醒", style = MaterialTheme.typography.bodySmall)
                }
            }

            // 重复模式
            Text("重复模式", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedRepeatMode == mode,
                        onClick = { selectedRepeatMode = mode },
                        label = { Text(mode.displayName, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 通知强度
            Text("通知强度", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationLevel.entries.forEach { level ->
                    FilterChip(
                        selected = selectedNotificationLevel == level,
                        onClick = { selectedNotificationLevel = level },
                        label = { Text(level.displayName, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // 保存按钮
            TYPButton(
                onClick = {
                    val alarm = Alarm(
                        id = alarmId,
                        type = selectedType,
                        hour = if (selectedType == AlarmType.FIXED) timePickerState.hour else null,
                        minute = if (selectedType == AlarmType.FIXED) timePickerState.minute else null,
                        startHour = if (selectedType == AlarmType.RANDOM) startHour else null,
                        endHour = if (selectedType == AlarmType.RANDOM) endHour else null,
                        repeatMode = selectedRepeatMode,
                        notificationLevel = selectedNotificationLevel
                    )
                    if (alarmId == 0L) {
                        viewModel.addAlarm(alarm)
                    } else {
                        viewModel.updateAlarm(alarm)
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                text = "保存"
            )
        }
    }
}
