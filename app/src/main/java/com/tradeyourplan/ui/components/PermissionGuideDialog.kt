package com.tradeyourplan.ui.components

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tradeyourplan.notification.QuoteReminderService

/**
 * Check if notification permission is granted.
 * On Android 12 and below, no runtime permission is needed.
 */
fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    return true
}

/**
 * Check if USE_FULL_SCREEN_INTENT permission is granted (Android 14+).
 * On Android 14+, this is a special permission that must be granted via Settings.
 */
fun hasFullScreenIntentPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        // Android 14+: Check if the permission was granted via Settings
        // Since there's no direct API, we check if notification policy allows it
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        return notificationManager.areNotificationsEnabled()
    }
    return true
}

/**
 * Check if overlay (SYSTEM_ALERT_WINDOW) permission is granted.
 */
fun hasOverlayPermission(context: Context): Boolean {
    return QuoteReminderService.canDrawOverlays(context)
}

/**
 * Check if all required permissions are granted.
 */
fun hasAllRequiredPermissions(context: Context): Boolean {
    return hasNotificationPermission(context) && hasOverlayPermission(context)
}

private fun isHuaweiDevice(): Boolean {
    val manufacturer = Build.MANUFACTURER.lowercase()
    return manufacturer.contains("huawei") || manufacturer.contains("honor")
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    return true
}

@Composable
fun PermissionGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isHuawei = isHuaweiDevice()

    var hasNotif by remember { mutableStateOf(hasNotificationPermission(context)) }
    var hasOverlay by remember { mutableStateOf(hasOverlayPermission(context)) }
    var ignoringBatteryOpt by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }

    // Refresh on resume
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasNotif = hasNotificationPermission(context)
                hasOverlay = hasOverlayPermission(context)
                ignoringBatteryOpt = isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Notification permission launcher (Android 13+)
    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotif = granted
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text("权限设置")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "提醒功能需要以下权限才能正常工作：",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Notification permission row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (hasNotif) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outlineVariant
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("发送通知", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (hasNotif) "已授权" else "未授权 — 无法收到提醒",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasNotif) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.error
                        )
                    }
                    if (!hasNotif) {
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) {
                            Text("授权")
                        }
                    }
                }

                Divider()

                // Overlay permission row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (hasOverlay) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outlineVariant
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("悬浮窗显示", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (hasOverlay) "已授权" else "未授权 — 无法弹出提醒窗口",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasOverlay) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.error
                        )
                    }
                    if (!hasOverlay) {
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        }) {
                            Text("授权")
                        }
                    }
                }

                // Huawei-specific: background popup permission
                if (isHuawei) {
                    Divider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("后台弹窗权限", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "华为设备需要额外开启「后台弹窗」权限。\n" +
                                "路径：设置 → 应用 → 交易智慧 → 权限 → 后台弹窗 → 允许",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = android.net.Uri.parse("package:${context.packageName}")
                                context.startActivity(intent)
                                Toast.makeText(context, "请在权限列表中开启「后台弹窗」", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }) {
                            Text("去设置")
                        }
                    }
                }

                // Battery optimization (Android 6+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Divider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (ignoringBatteryOpt) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (ignoringBatteryOpt) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("电池优化", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (ignoringBatteryOpt) "已关闭电池优化"
                                else "需要关闭电池优化，否则后台弹窗可能被阻止",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (ignoringBatteryOpt) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.error
                            )
                        }
                        if (!ignoringBatteryOpt) {
                            TextButton(onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = android.net.Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to app settings
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = android.net.Uri.parse("package:${context.packageName}")
                                    context.startActivity(intent)
                                    Toast.makeText(context, "请在电池选项中关闭优化", Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Text("关闭优化")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (hasNotif && hasOverlay) {
                TextButton(onClick = onDismiss) {
                    Text("完成")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("稍后再说")
                }
            }
        },
        dismissButton = {},
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}
