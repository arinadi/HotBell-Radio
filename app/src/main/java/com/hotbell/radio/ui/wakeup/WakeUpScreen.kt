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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
    BackHandler { }

    val challenge by viewModel.challenge.collectAsState()
    val isFallbackActive by viewModel.isFallbackActive.collectAsState()
    val canSnooze by viewModel.canSnooze.collectAsState()
    val snoozeRemaining by viewModel.snoozeCountRemaining.collectAsState()
    val dismissType by viewModel.dismissType.collectAsState()
    val isVerifying by viewModel.isVerifying.collectAsState()
    val verificationResult by viewModel.verificationResult.collectAsState()

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("hotbell_prefs", android.content.Context.MODE_PRIVATE) }
    val holdDurationMs = remember { prefs.getInt("alarm_dismiss_hold_sec", 3) * 1000 }
    var currentTime by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }

    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && capturedPhotoPath != null) {
                viewModel.verifyPhoto(capturedPhotoPath!!)
            }
        }
    )

    fun launchCamera() {
        val photoFile = File(context.cacheDir, "verify_${System.currentTimeMillis()}.jpg")
        capturedPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
        tempPhotoUri = photoUri
        cameraLauncher.launch(photoUri)
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HotBellOrange,
                        Color(0xFFE65100)
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
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "WAKE UP!",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Text(
                    text = timeFormat.format(Date(currentTime)),
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = dateFormat.format(Date(currentTime)),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isFallbackActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(NeonRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Fallback",
                            tint = NeonRed,
                            modifier = Modifier.size(20.dp)
                        )
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
                        fontSize = 16.sp
                    )
                }
            }

            // Live Radio Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 12.dp)
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

            // Middle Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dismissType == "photo") {
                        // PHOTO MATCH UI
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Photo Match to Dismiss",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (isVerifying) {
                                CircularProgressIndicator(color = HotBellOrange)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Verifying with Gemini...", color = Color.White.copy(alpha = 0.7f))
                            } else {
                                verificationResult?.let { result ->
                                    if (!result.match) {
                                        Text(
                                            text = "Match Failed: ${result.reason}",
                                            color = NeonRed,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(HotBellOrange)
                                        .clickable { launchCamera() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Take Photo",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Take a picture matching your reference photo.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    } else {
                        // MATH CHALLENGE UI
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Brain Check to Dismiss",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = challenge.question,
                                            color = Color.White,
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Hold the correct answer for 3 seconds",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // Answer Grid
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ChallengeButton(
                                        text = challenge.options[0].toString(),
                                        isCorrect = challenge.correctIndex == 0,
                                        holdDurationMs = holdDurationMs,
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
                                        holdDurationMs = holdDurationMs,
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ChallengeButton(
                                        text = challenge.options[2].toString(),
                                        isCorrect = challenge.correctIndex == 2,
                                        holdDurationMs = holdDurationMs,
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
                                        holdDurationMs = holdDurationMs,
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
            }

            // Snooze Button
            if (canSnooze) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { viewModel.snoozeAlarm { onDismissed() } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83D\uDCA4 Snooze ($snoozeRemaining left)",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
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
    modifier: Modifier = Modifier,
    holdDurationMs: Int = 3000,
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
            .background(Color.White.copy(alpha = 0.15f))
            .pointerInput(isCorrect, text) {
                detectTapGestures(
                    onPress = {
                        if (isCorrect) {
                            try {
                                val job = coroutineScope.launch {
                                    progressAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = holdDurationMs,
                                            easing = LinearEasing
                                        )
                                    )
                                    if (progressAnim.value >= 1f) {
                                        onSuccess()
                                    }
                                }
                                tryAwaitRelease()
                                job.cancel()
                                if (progressAnim.value < 1f) {
                                    coroutineScope.launch {
                                        progressAnim.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 300)
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                coroutineScope.launch { progressAnim.snapTo(0f) }
                            }
                        } else {
                            tryAwaitRelease()
                            coroutineScope.launch {
                                shakeOffset.animateTo(
                                    targetValue = 50f,
                                    animationSpec = repeatable(
                                        iterations = 10,
                                        animation = tween(durationMillis = 30),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                shakeOffset.snapTo(0f)
                            }
                            onFail()
                        }
                    }
                )
            }
    ) {
        // Progress fill — Box terpisah agar fillMaxWidth(fraction) bekerja
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progressAnim.value)
                .background(Color(0xFF4CAF50).copy(alpha = 0.85f))
        )

        // Teks jawaban
        Text(
            text = text,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        // Circular progress indicator saat ditahan
        if (progressAnim.value > 0f) {
            CircularProgressIndicator(
                progress = { progressAnim.value },
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 6.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
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
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(24.dp)
    ) {
        for (i in 0 until 4) {
            val h by infiniteTransition.animateFloat(
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
                    .fillMaxHeight(h)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
        }
    }
}