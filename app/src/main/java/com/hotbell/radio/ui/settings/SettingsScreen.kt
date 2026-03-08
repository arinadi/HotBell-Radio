package com.hotbell.radio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotbell.radio.R
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.HotBellOrange
import com.hotbell.radio.ui.theme.PitchBlack
import com.hotbell.radio.ui.theme.NeonRed
import com.hotbell.radio.ui.theme.ElectricBlue
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.app.AlarmManager
import android.app.NotificationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val vibrateOnWake by viewModel.vibrateOnWake.collectAsState()
    val flashlightOnWake by viewModel.flashlightOnWake.collectAsState()
    val startVolume by viewModel.startVolume.collectAsState()
    val maxBoost by viewModel.maxBoost.collectAsState()
    val crescendoSec by viewModel.crescendoSec.collectAsState()
    val dismissHoldSec by viewModel.dismissHoldSec.collectAsState()
    
    // Bedtime Reminder
    val bedtimeEnabled by viewModel.bedtimeEnabled.collectAsState()
    val bedtimeHour by viewModel.bedtimeHour.collectAsState()
    val bedtimeMinute by viewModel.bedtimeMinute.collectAsState()
    
    // Gemini API Key
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )

        // Alarm Configuration Section
        Text(
            text = "Alarm Configuration",
            color = HotBellOrange,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Vibrate on Wake
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibrate on Wake", color = Color.White, fontSize = 16.sp)
                    Switch(
                        checked = vibrateOnWake,
                        onCheckedChange = { viewModel.setVibrateOnWake(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HotBellOrange,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = PitchBlack
                        )
                    )
                }

                // Flashlight on Wake
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Flashlight on Wake", color = Color.White, fontSize = 16.sp)
                    Switch(
                        checked = flashlightOnWake,
                        onCheckedChange = { viewModel.setFlashlightOnWake(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HotBellOrange,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = PitchBlack
                        )
                    )
                }

                // Bedtime Reminder Section
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    "Bedtime Reminder",
                    color = HotBellOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Remind me to sleep", color = Color.White, fontSize = 16.sp)
                        val timeStr = String.format("%02d:%02d", bedtimeHour, bedtimeMinute)
                        Text(
                            text = if (bedtimeEnabled) "At $timeStr" else "Off",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable {
                                    android.app.TimePickerDialog(
                                        context,
                                        { _, h, m -> viewModel.setBedtimeTime(h, m) },
                                        bedtimeHour,
                                        bedtimeMinute,
                                        true
                                    ).show()
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                    Switch(
                        checked = bedtimeEnabled,
                        onCheckedChange = { viewModel.setBedtimeEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HotBellOrange,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = PitchBlack
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))

                // Gemini API Configuration
                Text(
                    "Gemini AI Configuration",
                    color = HotBellOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "Required for Photo Match Challenge. Your key is stored locally.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = geminiApiKey,
                    onValueChange = { viewModel.setGeminiApiKey(it) },
                    label = { Text("Gemini API Key", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HotBellOrange,
                        unfocusedBorderColor = DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = HotBellOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))

                // Start Volume
                ConfigSlider(
                    label = "Start Volume",
                    value = startVolume,
                    valueRange = 5f..50f,
                    suffix = "%",
                    onValueChange = { viewModel.setStartVolume(it) }
                )

                // Max Volume Boost
                ConfigSlider(
                    label = "Max Volume Boost",
                    value = maxBoost,
                    valueRange = 100f..200f,
                    suffix = "%",
                    steps = 9,
                    onValueChange = { viewModel.setMaxBoost(it) }
                )

                // Crescendo Duration
                ConfigSlider(
                    label = "Crescendo Duration",
                    value = crescendoSec,
                    valueRange = 15f..120f,
                    suffix = "s",
                    steps = 6,
                    onValueChange = { viewModel.setCrescendoSec(it) }
                )

                // Dismiss Hold Time
                ConfigSlider(
                    label = "Dismiss Hold Time",
                    value = dismissHoldSec,
                    valueRange = 1f..5f,
                    suffix = "s",
                    steps = 3,
                    onValueChange = { viewModel.setDismissHoldSec(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Permissions Section
        Text(
            text = "Permissions",
            color = HotBellOrange,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val context = LocalContext.current
        val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
        val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as? AlarmManager
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? NotificationManager

        // Re-evaluate permissions every time screen resumes (user returns from Settings)
        val hasNotificationPermission = remember(lifecycleState) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        }

        val hasExactAlarmPermission = remember(lifecycleState) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager?.canScheduleExactAlarms() == true
            } else true
        }

        val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
        val isIgnoringBatteryOptimizations = remember(lifecycleState) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        }

        val canUseFullScreenIntent = remember(lifecycleState) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                notificationManager?.canUseFullScreenIntent() == true
            } else true
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PermissionRow(
                    title = "Notifications",
                    isGranted = hasNotificationPermission,
                    onClick = {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to app details
                            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(fallbackIntent)
                        }
                    }
                )
                
                androidx.compose.material3.HorizontalDivider(
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                PermissionRow(
                    title = "Battery Optimization",
                    isGranted = isIgnoringBatteryOptimizations,
                    onClick = {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    androidx.compose.material3.HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    PermissionRow(
                        title = "Exact Alarms",
                        isGranted = hasExactAlarmPermission,
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    androidx.compose.material3.HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    PermissionRow(
                        title = "Full Screen Intent",
                        isGranted = canUseFullScreenIntent,
                        onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // About Section
        Text(
            text = "About",
            color = HotBellOrange,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // H. Logo Implementation
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "H",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(bottom = 8.dp)
                            .background(HotBellOrange, CircleShape)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "HotBell Radio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                Text(
                    text = "Version $versionName",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/arinadi/HotBell-Radio"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("GitHub Repository", color = ElectricBlue)
                }

                Button(
                    onClick = { viewModel.checkForUpdates(manual = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = HotBellOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Check for Updates", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionRow(title: String, isGranted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isGranted) onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, color = Color.White, fontSize = 16.sp)
            Text(
                text = if (isGranted) "Granted" else "Tap to grant",
                color = if (isGranted) Color.Green.copy(alpha = 0.7f) else NeonRed,
                fontSize = 12.sp
            )
        }
        if (!isGranted) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun ConfigSlider(
    label: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    suffix: String,
    steps: Int = 0,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(
                text = "$value$suffix",
                color = HotBellOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = HotBellOrange,
                activeTrackColor = HotBellOrange,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

