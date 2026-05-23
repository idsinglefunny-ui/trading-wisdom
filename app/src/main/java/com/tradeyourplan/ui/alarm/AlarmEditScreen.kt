// app/src/main/java/com/tradeyourplan/ui/alarm/AlarmEditScreen.kt
package com.tradeyourplan.ui.alarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.*
import com.tradeyourplan.ui.components.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long = 0,
    viewModel: AlarmViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var selectedType by remember { mutableStateOf(AlarmType.FIXED) }
    var selectedHour by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(15) }
    var endMinute by remember { mutableIntStateOf(0) }
    var selectedRepeatMode by remember { mutableStateOf(RepeatMode.DAILY) }
    var selectedNotificationLevel by remember { mutableStateOf(NotificationLevel.NORMAL) }

    // Time picker dialog states
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

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
                    TimeInputCard(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onClick = { showStartTimePicker = true }
                    )
                }
                AlarmType.RANDOM -> {
                    Text("时间范围", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimeInputCard(
                            hour = startHour,
                            minute = startMinute,
                            label = "开始时间",
                            modifier = Modifier.weight(1f),
                            onClick = { showStartTimePicker = true }
                        )
                        TimeInputCard(
                            hour = endHour,
                            minute = endMinute,
                            label = "结束时间",
                            modifier = Modifier.weight(1f),
                            onClick = { showEndTimePicker = true }
                        )
                    }
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
                        hour = if (selectedType == AlarmType.FIXED) selectedHour else null,
                        minute = if (selectedType == AlarmType.FIXED) selectedMinute else null,
                        startHour = if (selectedType == AlarmType.RANDOM) startHour else null,
                        startMinute = if (selectedType == AlarmType.RANDOM) startMinute else null,
                        endHour = if (selectedType == AlarmType.RANDOM) endHour else null,
                        endMinute = if (selectedType == AlarmType.RANDOM) endMinute else null,
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

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        TimePickerDialog(
            initialHour = if (selectedType == AlarmType.RANDOM) startHour else selectedHour,
            initialMinute = if (selectedType == AlarmType.RANDOM) startMinute else selectedMinute,
            onConfirm = { hour, minute ->
                if (selectedType == AlarmType.RANDOM) {
                    startHour = hour
                    startMinute = minute
                } else {
                    selectedHour = hour
                    selectedMinute = minute
                }
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    // End Time Picker Dialog (for RANDOM type)
    if (showEndTimePicker) {
        TimePickerDialog(
            initialHour = endHour,
            initialMinute = endMinute,
            onConfirm = { hour, minute ->
                endHour = hour
                endMinute = minute
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@Composable
private fun TimeInputCard(
    hour: Int,
    minute: Int,
    label: String = "选择时间",
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "%02d:%02d".format(hour, minute),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "选择时间",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "选择时间",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                TimePicker(state = timePickerState)
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
