package com.hotbell.radio.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.data.AlarmEntity
import com.hotbell.radio.ui.theme.ElectricBlue
import com.hotbell.radio.ui.theme.NeonRed
import com.hotbell.radio.ui.theme.HotBellOrange
import com.hotbell.radio.ui.theme.PitchBlack
import com.hotbell.radio.ui.theme.DarkGray
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.text.style.TextOverflow

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddAlarm: () -> Unit,
    onEditAlarm: (String) -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()
    val countdowns by viewModel.alarmCountdowns.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showPermissionsDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PitchBlack
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "HotBell Menu",
                    color = NeonRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("Setup Permissions", color = Color.White) },
                    selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showPermissionsDialog = true
                        },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        if (showPermissionsDialog) {
            PermissionsDialog(onDismiss = { showPermissionsDialog = false })
        }

        Scaffold(
            containerColor = PitchBlack,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddAlarm,
                    containerColor = HotBellOrange,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm", tint = Color.White)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Custom Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row {
                            Text(
                                text = "HotBell",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = ".",
                                color = HotBellOrange,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        val nextAlarmStr = countdowns.values.firstOrNull() ?: "No active alarms"
                        Text(
                            text = if (nextAlarmStr == "No active alarms") nextAlarmStr else "Next alarm in $nextAlarmStr",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    
                    // TEST WAKE BUTTON
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeonRed.copy(alpha = 0.1f))
                            .clickable {
                                viewModel.testWake(context)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("TEST WAKE", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                if (alarms.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No alarms yet.\nTap + to add one.",
                            color = DarkGray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(alarms, key = { it.id }) { alarm ->
                            AlarmCard(
                                alarm = alarm,
                                countdown = countdowns[alarm.id],
                                onToggle = { viewModel.toggleAlarm(alarm) },
                                onEdit = { onEditAlarm(alarm.id) },
                                onDelete = { viewModel.deleteAlarm(alarm) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    countdown: String?,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (alarm.isEnabled) DarkGray.copy(alpha = 0.5f) else DarkGray.copy(alpha = 0.2f),
        label = "cardBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%02d:%02d", alarm.timeHour, alarm.timeMin),
                        color = if (alarm.isEnabled) Color.White else Color.Gray,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alarm.timeHour < 12) "AM" else "PM",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alignByBaseline()
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = HotBellOrange,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = DarkGray.copy(alpha = 0.5f)
                    )
                )
            }

            if (alarm.isEnabled && countdown != null) {
                Text(
                    text = "Rings in $countdown",
                    color = HotBellOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of the week row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEachIndexed { index, dayStr ->
                    val isSelected = alarm.daysOfWeek and (1 shl index) != 0
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) HotBellOrange.copy(alpha = 0.2f) else DarkGray.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayStr,
                            color = if (isSelected) HotBellOrange else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.HorizontalDivider(color = DarkGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (alarm.isEnabled) HotBellOrange else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alarm.stationName ?: "Default Ringtone",
                        color = if (alarm.isEnabled) ElectricBlue else Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Wake Up Routine",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun daysToString(bitmask: Int): String {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return days.filterIndexed { index, _ -> bitmask and (1 shl index) != 0 }.joinToString(", ")
}

@Composable
fun PermissionsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager
    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    val hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() == true
    } else true

    val hasFullScreenIntentPermission = if (Build.VERSION.SDK_INT >= 34) {
        notificationManager?.canUseFullScreenIntent() == true
    } else true
    
    // Auto-dismiss if all permissions are granted
    if (hasNotificationPermission && hasExactAlarmPermission && hasFullScreenIntentPermission) {
       onDismiss()
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PitchBlack)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "App Permissions",
                    color = ElectricBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "HotBell needs the following permissions to ensure your alarms ring on time.",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionItem(
                        title = "Notifications",
                        description = "Needed to show alarms and controls.",
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                if (!hasExactAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PermissionItem(
                        title = "Exact Alarms",
                        description = "Needed to trigger the alarm precisely on time.",
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                if (!hasFullScreenIntentPermission && Build.VERSION.SDK_INT >= 34) {
                    PermissionItem(
                        title = "Full Screen Intent",
                        description = "Needed to wake up the screen when the alarm rings.",
                        onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close", color = NeonRed)
                }
            }
        }
    }
}

@Composable
fun PermissionItem(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = description, color = Color.Gray, fontSize = 12.sp)
            Text(text = "TAP TO GRANT", color = NeonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
