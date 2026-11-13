package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ControlCenterState
import com.example.data.model.LauncherSettings
import com.example.data.model.LockClockStyle
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassPill
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidAmber
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.StatusConnected
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun LockPreviewScreen(
    settings: LauncherSettings,
    controlState: ControlCenterState,
    onToggleTorch: () -> Unit,
    onOpenCamera: () -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onUnlock)

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val dateObj = remember(nowMillis / 15_000L) { Date(nowMillis) }

    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f))
            .offset { IntOffset(0, dragOffsetY.roundToInt().coerceAtMost(0)) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        dragOffsetY = (dragOffsetY + dragAmount).coerceAtMost(0f)
                    },
                    onDragEnd = {
                        if (dragOffsetY < -140f) {
                            onUnlock()
                        } else {
                            dragOffsetY = 0f
                        }
                    },
                    onDragCancel = { dragOffsetY = 0f }
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("lock_preview_screen")
    ) {
        // Honest Preview Disclaimer Badge
        LiquidGlassPill(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
        ) {
            Text(
                text = "Lock Screen Preview • In-App Mode",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }

        // Clock & Status Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            Text(
                text = dateFormat.format(dateObj),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(6.dp))

            when (settings.lockClockStyle) {
                LockClockStyle.MINIMAL_GLASS -> {
                    LiquidGlassSurface(shape = RoundedCornerShape(32.dp)) {
                        Text(
                            text = timeFormat.format(dateObj),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 68.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                        )
                    }
                }
                LockClockStyle.BOLD_EDITORIAL -> {
                    Text(
                        text = timeFormat.format(dateObj),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 78.sp),
                        color = Color.White
                    )
                }
                LockClockStyle.COMPACT_PILL -> {
                    LiquidGlassPill {
                        Text(
                            text = timeFormat.format(dateObj),
                            style = MaterialTheme.typography.displayMedium,
                            color = LiquidCyan,
                            modifier = Modifier.padding(horizontal = 26.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LiquidGlassPill {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (controlState.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                        contentDescription = "Battery",
                        tint = if (controlState.isCharging) StatusConnected else LiquidCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${controlState.batteryPercent}% • ${if (controlState.isWifiConnected) "Wi-Fi Connected" else "Standby"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Bottom Quick Actions + Swipe-to-Unlock Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            LiquidGlassClickable(
                onClick = onToggleTorch,
                shape = CircleShape,
                accentTint = if (controlState.isTorchOn) LiquidAmber.copy(alpha = 0.35f) else null,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (controlState.isTorchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                    contentDescription = "Toggle Flashlight",
                    tint = if (controlState.isTorchOn) LiquidAmber else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            LiquidGlassClickable(
                onClick = onUnlock,
                shape = CircleShape,
                modifier = Modifier.testTag("lock_preview_unlock_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Icon(Icons.Rounded.KeyboardArrowUp, null, tint = LiquidCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Rounded.LockOpen, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Swipe or Tap to Unlock", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }

            LiquidGlassClickable(
                onClick = onOpenCamera,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Open Camera",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
