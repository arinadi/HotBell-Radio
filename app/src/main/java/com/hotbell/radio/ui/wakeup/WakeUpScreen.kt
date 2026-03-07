package com.hotbell.radio.ui.wakeup

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.ElectricBlue
import com.hotbell.radio.ui.theme.HotBellOrange
import com.hotbell.radio.ui.theme.NeonRed
import com.hotbell.radio.ui.theme.PitchBlack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WakeUpScreen(
    viewModel: WakeUpViewModel,
    stationName: String?,
    onDismissed: () -> Unit
) {
    val challenge by viewModel.challenge.collectAsState()
    val isFallbackActive by viewModel.isFallbackActive.collectAsState()
    val context = LocalContext.current
    var currentTime by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    var flashColor by remember { mutableStateOf(Color.Transparent) }
    val coroutineScope = rememberCoroutineScope()

    val colors = listOf(
        Color.White,
        Color.White,
        Color.White,
        Color.White
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HotBellOrange,
                        Color(0xFFE65100) // Deep Orange
                    )
                )
            )
    ) {
        // Flash overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(flashColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Time & Status)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = "WAKE UP!",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Text(
                    text = timeFormat.format(Date(currentTime)),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = dateFormat.format(Date(currentTime)),
                    color = Color.White.copy(alpha = 0.8f),
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
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp
                    )
                }
            }

            // Live Radio Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    VisualEqualizer()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "LIVE RADIO",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
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
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
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
                        baseColor = colors[0],
                        modifier = Modifier.weight(1f),
                        onSuccess = { 
                            handleSuccess(coroutineScope) { flashColor = it }
                            viewModel.dismissAlarm(onDismissed) 
                        },
                        onFail = { 
                            handleFail(coroutineScope, context) { flashColor = it }
                            viewModel.generateNewChallenge() 
                        }
                    )
                    ChallengeButton(
                        text = challenge.options[1].toString(),
                        isCorrect = challenge.correctIndex == 1,
                        baseColor = colors[1],
                        modifier = Modifier.weight(1f),
                        onSuccess = { 
                            handleSuccess(coroutineScope) { flashColor = it }
                            viewModel.dismissAlarm(onDismissed) 
                        },
                        onFail = { 
                            handleFail(coroutineScope, context) { flashColor = it }
                            viewModel.generateNewChallenge() 
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ChallengeButton(
                        text = challenge.options[2].toString(),
                        isCorrect = challenge.correctIndex == 2,
                        baseColor = colors[2],
                        modifier = Modifier.weight(1f),
                        onSuccess = { 
                            handleSuccess(coroutineScope) { flashColor = it }
                            viewModel.dismissAlarm(onDismissed) 
                        },
                        onFail = { 
                            handleFail(coroutineScope, context) { flashColor = it }
                            viewModel.generateNewChallenge() 
                        }
                    )
                    ChallengeButton(
                        text = challenge.options[3].toString(),
                        isCorrect = challenge.correctIndex == 3,
                        baseColor = colors[3],
                        modifier = Modifier.weight(1f),
                        onSuccess = { 
                            handleSuccess(coroutineScope) { flashColor = it }
                            viewModel.dismissAlarm(onDismissed) 
                        },
                        onFail = { 
                            handleFail(coroutineScope, context) { flashColor = it }
                            viewModel.generateNewChallenge() 
                        }
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
    baseColor: Color,
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val progressAnim = remember { Animatable(0f) }
    val shakeOffset = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(baseColor.copy(alpha = 0.3f))
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
                            coroutineScope.launch {
                                shakeOffset.animateTo(
                                    targetValue = 50f,
                                    animationSpec = repeatable(iterations = 10, animation = tween(durationMillis = 30), repeatMode = RepeatMode.Reverse)
                                )
                                shakeOffset.snapTo(0f)
                            }
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
                .background(baseColor)
        )
        // Button Text
        Text(
            text = text,
            color = PitchBlack,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun handleSuccess(scope: CoroutineScope, setFlashColor: (Color) -> Unit) {
    scope.launch {
        setFlashColor(Color.Green.copy(alpha = 0.5f))
        delay(200)
        setFlashColor(Color.Transparent)
    }
}

private fun handleFail(scope: CoroutineScope, context: Context, setFlashColor: (Color) -> Unit) {
    vibrateDevice(context, isError = true)
    scope.launch {
        setFlashColor(Color.Red.copy(alpha = 0.5f))
        delay(100)
        setFlashColor(Color.Transparent)
        delay(100)
        setFlashColor(Color.Red.copy(alpha = 0.5f))
        delay(100)
        setFlashColor(Color.Transparent)
    }
}

private fun vibrateDevice(context: Context, isError: Boolean = false) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    if (isError) {
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)
        val timings = longArrayOf(0, 400, 100, 400, 100, 400, 100, 1500)
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    } else {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

@Composable
fun VisualEqualizer() {
    val infiniteTransition = rememberInfiniteTransition()
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(24.dp)
    ) {
        for (i in 0 until 4) {
            val h = infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 300 + (100 * i), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "eq_$i"
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(h.value)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
        }
    }
}
