package com.hotbell.radio.ui.wakeup

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.ElectricBlue
import com.hotbell.radio.ui.theme.NeonRed
import com.hotbell.radio.ui.theme.PitchBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WakeUpScreen(
    viewModel: WakeUpViewModel,
    stationName: String?,
    onDismissed: () -> Unit
) {
    val challenge by viewModel.challenge.collectAsState()
    val isFallbackActive by viewModel.isFallbackActive.collectAsState()
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Time & Status)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = timeFormat.format(Date(currentTime)),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = dateFormat.format(Date(currentTime)),
                    color = ElectricBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                if (isFallbackActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(NeonRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Fallback", tint = NeonRed, modifier = Modifier.size(20.dp))
                        Text(
                            text = " Playing Fallback Alarm",
                            color = NeonRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Radio: ${stationName ?: "Unknown Station"}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp
                    )
                }
            }

            // Middle Section (Math Challenge)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Brain Check to Dismiss",
                    color = ElectricBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = challenge.question,
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hold the correct answer for 3 seconds",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Bottom Section (Answer Grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ChallengeButton(
                        text = challenge.options[0].toString(),
                        isCorrect = challenge.correctIndex == 0,
                        context = context,
                        modifier = Modifier.weight(1f),
                        onSuccess = { viewModel.dismissAlarm(onDismissed) },
                        onFail = { viewModel.generateNewChallenge() }
                    )
                    ChallengeButton(
                        text = challenge.options[1].toString(),
                        isCorrect = challenge.correctIndex == 1,
                        context = context,
                        modifier = Modifier.weight(1f),
                        onSuccess = { viewModel.dismissAlarm(onDismissed) },
                        onFail = { viewModel.generateNewChallenge() }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ChallengeButton(
                        text = challenge.options[2].toString(),
                        isCorrect = challenge.correctIndex == 2,
                        context = context,
                        modifier = Modifier.weight(1f),
                        onSuccess = { viewModel.dismissAlarm(onDismissed) },
                        onFail = { viewModel.generateNewChallenge() }
                    )
                    ChallengeButton(
                        text = challenge.options[3].toString(),
                        isCorrect = challenge.correctIndex == 3,
                        context = context,
                        modifier = Modifier.weight(1f),
                        onSuccess = { viewModel.dismissAlarm(onDismissed) },
                        onFail = { viewModel.generateNewChallenge() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeButton(
    text: String,
    isCorrect: Boolean,
    context: Context,
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val progressAnim = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkGray)
            .pointerInput(isCorrect, text) {
                detectTapGestures(
                    onPress = {
                        if (isCorrect) {
                            try {
                                // Start filling animation
                                val job = coroutineScope.launch {
                                    progressAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                                    )
                                    if (progressAnim.value == 1f) {
                                        onSuccess()
                                    }
                                }
                                tryAwaitRelease() // Wait for release
                                job.cancel()
                                // If released before 100%, animate back to 0
                                coroutineScope.launch {
                                    progressAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 300)
                                    )
                                }
                            } finally {
                                // Fallback just in case
                                coroutineScope.launch { progressAnim.snapTo(0f) }
                            }
                        } else {
                            tryAwaitRelease()
                            vibrateDevice(context)
                            onFail()
                        }
                    }
                )
            }
    ) {
        // Progress Fill Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(progressAnim.value)
                .background(ElectricBlue)
        )
        // Button Text
        Text(
            text = text,
            color = if (progressAnim.value > 0.5f) PitchBlack else Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun vibrateDevice(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}
