package com.hotbell.radio.ui.alarm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.ElectricBlue
import com.hotbell.radio.ui.theme.NeonRed
import com.hotbell.radio.ui.theme.PitchBlack

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

    LaunchedEffect(alarmId) {
        alarmId?.let { viewModel.loadAlarm(it) }
    }

    Scaffold(
        containerColor = PitchBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (alarmId != null) "Edit Alarm" else "Add Alarm",
                        color = ElectricBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PitchBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Time display (tappable to change)
            TimeSelector(hour = hour, minute = minute, onTimeChange = viewModel::setTime)

            Spacer(modifier = Modifier.height(32.dp))

            // Day selector
            Text("Repeat", color = DarkGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = daysOfWeek and (1 shl index) != 0
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleDay(index) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.3f),
                            selectedLabelColor = ElectricBlue,
                            containerColor = DarkGray.copy(alpha = 0.2f),
                            labelColor = DarkGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Station selector
            Text("Wake Up Station", color = DarkGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                onClick = onSelectStation
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stationName ?: "Tap to select a station",
                        color = if (stationName != null) NeonRed else DarkGray,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text("🎵", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = { viewModel.saveAlarm(onDone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Text("  Save Alarm", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun TimeSelector(hour: Int, minute: Int, onTimeChange: (Int, Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Hour
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                onClick = { onTimeChange((hour + 1) % 24, minute) },
                color = Color.Transparent
            ) { Text("▲", color = ElectricBlue, fontSize = 20.sp) }

            Text(
                text = String.format("%02d", hour),
                color = ElectricBlue,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                onClick = { onTimeChange((hour - 1 + 24) % 24, minute) },
                color = Color.Transparent
            ) { Text("▼", color = ElectricBlue, fontSize = 20.sp) }
        }

        Text(":", color = ElectricBlue, fontSize = 64.sp, fontWeight = FontWeight.Bold)

        // Minute
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                onClick = { onTimeChange(hour, (minute + 1) % 60) },
                color = Color.Transparent
            ) { Text("▲", color = ElectricBlue, fontSize = 20.sp) }

            Text(
                text = String.format("%02d", minute),
                color = ElectricBlue,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                onClick = { onTimeChange(hour, (minute - 1 + 60) % 60) },
                color = Color.Transparent
            ) { Text("▼", color = ElectricBlue, fontSize = 20.sp) }
        }
    }
}
