// app/src/main/java/com/tradeyourplan/ui/components/AlarmEditDialog.kt
package com.tradeyourplan.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    alarm: Alarm?,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: () -> Unit
) {
    var selectedType by remember { mutableStateOf(alarm?.type ?: AlarmType.FIXED) }
    var selectedHour by remember { mutableIntStateOf(alarm?.hour ?: 9) }
    var selectedMinute by remember { mutableIntStateOf(alarm?.minute ?: 0) }
    var startHour by remember { mutableIntStateOf(alarm?.startHour ?: 9) }
    var startMinute by remember { mutableIntStateOf(alarm?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(alarm?.endHour ?: 15) }
    var endMinute by remember { mutableIntStateOf(alarm?.endMinute ?: 0) }
    var selectedRepeatMode by remember { mutableStateOf(alarm?.repeatMode ?: RepeatMode.DAILY) }
    var selectedNotificationLevel by remember { mutableStateOf(alarm?.notificationLevel ?: NotificationLevel.NORMAL) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (alarm == null) "添加提醒" else "编辑提醒")
                if (alarm != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 提醒类型选择
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    }
                }

                // 根据类型显示不同配置
                when (selectedType) {
                    AlarmType.FIXED -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("提醒时间", style = MaterialTheme.typography.titleMedium)
                                TimeInputCard(
                                    hour = selectedHour,
                                    minute = selectedMinute,
                                    onClick = { showStartTimePicker = true }
                                )
                            }
                        }
                    }
                    AlarmType.RANDOM -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    }
                }

                // 重复模式
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    }
                }

                // 通知强度
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newAlarm = Alarm(
                        id = alarm?.id ?: 0,
                        type = selectedType,
                        hour = if (selectedType == AlarmType.FIXED) selectedHour else null,
                        minute = if (selectedType == AlarmType.FIXED) selectedMinute else null,
                        startHour = if (selectedType == AlarmType.RANDOM) startHour else null,
                        startMinute = if (selectedType == AlarmType.RANDOM) startMinute else null,
                        endHour = if (selectedType == AlarmType.RANDOM) endHour else null,
                        endMinute = if (selectedType == AlarmType.RANDOM) endMinute else null,
                        repeatMode = selectedRepeatMode,
                        notificationLevel = selectedNotificationLevel,
                        isEnabled = alarm?.isEnabled ?: true
                    )
                    onSave(newAlarm)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // Time picker dialogs
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
