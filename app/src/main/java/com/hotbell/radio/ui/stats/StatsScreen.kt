package com.hotbell.radio.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotbell.radio.ui.theme.*

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Alarm Statistics",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Track your wake-up habits",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dismiss Rate Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HotBellOrange.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${stats.dismissRate}%",
                    color = HotBellOrange,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Dismiss Rate",
                    color = Color.White,
                    fontSize = 16.sp
                )
                if (stats.avgDismissTimeSec > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Avg. ${stats.avgDismissTimeSec}s to dismiss",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Last 7 Days
        Text(
            "LAST 7 DAYS",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Fired", stats.last7DaysFired, ElectricBlue, Modifier.weight(1f))
            StatCard("Dismissed", stats.last7DaysDismissed, HotBellOrange, Modifier.weight(1f))
            StatCard("Snoozed", stats.last7DaysSnoozed, Color.Yellow, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // All Time
        Text(
            "ALL TIME",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Fired", stats.totalFired, ElectricBlue, Modifier.weight(1f))
            StatCard("Dismissed", stats.totalDismissed, HotBellOrange, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Snoozed", stats.totalSnoozed, Color.Yellow, Modifier.weight(1f))
            StatCard("Auto-Off", stats.totalAutoDismissed, NeonRed, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(label: String, value: Int, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$value",
                color = accent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
