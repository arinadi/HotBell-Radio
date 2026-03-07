package com.hotbell.radio.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditScreen(
    viewModel: AlarmEditViewModel,
    alarmId: String?,
    onSelectStation: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val hour by viewModel.hour.collectAsState()
    val minute by viewModel.minute.collectAsState()
    val daysOfWeek by viewModel.daysOfWeek.collectAsState()
    val stationName by viewModel.stationName.collectAsState()
    val label by viewModel.label.collectAsState()
    val isVibrateEnabled by viewModel.isVibrateEnabled.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    Scaffold(
        containerColor = PitchBlack,
        topBar = {
            // Custom Header from Mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancel",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = if (alarmId != null) "Edit Alarm" else "Add Alarm",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Save",
                    color = HotBellOrange,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.saveAlarm(onDone) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoaded) {
                var isEditingTime by rememberSaveable { mutableStateOf(alarmId == null) }
                val timePickerState = rememberTimePickerState(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = true
                )

                LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    viewModel.setTime(timePickerState.hour, timePickerState.minute)
                }

                if (isEditingTime) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = DarkGray.copy(alpha = 0.5f),
                            clockDialSelectedContentColor = PitchBlack,
                            clockDialUnselectedContentColor = Color.White,
                            selectorColor = HotBellOrange,
                            timeSelectorSelectedContainerColor = HotBellOrange.copy(alpha = 0.3f),
                            timeSelectorUnselectedContainerColor = DarkGray.copy(alpha = 0.5f),
                            timeSelectorSelectedContentColor = HotBellOrange,
                            timeSelectorUnselectedContentColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { isEditingTime = false },
                        colors = ButtonDefaults.buttonColors(containerColor = HotBellOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Set Time", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Stylized Time Display from Mockup
                    Row(
                        modifier = Modifier
                            .clickable { isEditingTime = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d", hour),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Normal,
                            color = HotBellOrange,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp)
                        )
                        Text(
                            text = " : ",
                            fontSize = 60.sp,
                            color = DarkGray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = String.format("%02d", minute),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(200.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Repeat Days Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "REPEAT DAYS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                    dayLabels.forEachIndexed { index, label ->
                        val isSelected = daysOfWeek and (1 shl index) != 0
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) HotBellOrange else DarkGray.copy(alpha = 0.2f))
                                .clickable { viewModel.toggleDay(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Configuration Menu Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Label Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = label,
                                onValueChange = { viewModel.setLabel(it) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                decorationBox = { innerTextField ->
                                    if (label.isEmpty()) {
                                        Text("Alarm Label", color = DarkGray, fontSize = 16.sp)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    androidx.compose.material3.HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Station Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStation() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = HotBellOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stationName ?: "Select Stream",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "WAKE UP STREAM",
                            color = DarkGray,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = DarkGray
                    )
                }

                    androidx.compose.material3.HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Vibrate Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vibrate",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isVibrateEnabled,
                            onCheckedChange = { viewModel.setVibrate(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HotBellOrange,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = DarkGray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Delete Button
            if (alarmId != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonRed.copy(alpha = 0.1f))
                        .clickable { viewModel.deleteAlarm(onDone) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Alarm",
                            color = NeonRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
