package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ControlCenterState
import com.example.data.model.WidgetType
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMint
import com.example.ui.theme.LiquidViolet
import com.example.ui.theme.StatusConnected
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeWidgetCard(
    widgetType: WidgetType,
    controlState: ControlCenterState,
    isEditMode: Boolean,
    onRemove: () -> Unit,
    onOpenClock: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenControlCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when (widgetType) {
            WidgetType.CLOCK_DATE -> ClockDateWidget(
                batteryPercent = controlState.batteryPercent,
                isCharging = controlState.isCharging,
                onOpenClock = onOpenClock,
                onOpenCalendar = onOpenCalendar
            )
            WidgetType.DEVICE_STATS -> DevicePulseWidget(
                controlState = controlState,
                onOpenControlCenter = onOpenControlCenter
            )
            WidgetType.QUICK_SEARCH -> QuickSearchWidget(
                onOpenSearch = onOpenSearch,
                onOpenControlCenter = onOpenControlCenter
            )
        }

        if (isEditMode) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Remove ${widgetType.title}",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ClockDateWidget(
    batteryPercent: Int,
    isCharging: Boolean,
    onOpenClock: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            val delayToNextMinute = 60_000L - (nowMillis % 60_000L)
            delay(delayToNextMinute.coerceIn(1_000L, 60_000L))
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMM", Locale.getDefault()) }
    val dateObj = remember(nowMillis / 60_000L) { Date(nowMillis) }

    LiquidGlassSurface(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("widget_clock_date")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenClock)
            ) {
                Text(
                    text = timeFormat.format(dateObj),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(dateObj),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.84f),
                    modifier = Modifier.clickable(onClick = onOpenCalendar)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                        contentDescription = "Battery $batteryPercent%",
                        tint = if (isCharging) StatusConnected else LiquidCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$batteryPercent%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "My Liquid OS",
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidCyan.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun DevicePulseWidget(
    controlState: ControlCenterState,
    onOpenControlCenter: () -> Unit
) {
    val usedRam = (controlState.totalRamMb - controlState.availableRamMb).coerceAtLeast(0L)
    val ramProgress = if (controlState.totalRamMb > 0L) {
        (usedRam.toFloat() / controlState.totalRamMb.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LiquidGlassClickable(
        onClick = onOpenControlCenter,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("widget_device_stats")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = "Memory monitor",
                        tint = LiquidCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Device Pulse",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                Text(
                    text = "${controlState.availableRamMb} MB free",
                    style = MaterialTheme.typography.labelMedium,
                    color = LiquidMint
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { ramProgress },
                color = LiquidCyan,
                trackColor = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "RAM: $usedRam / ${controlState.totalRamMb} MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.78f)
                )
                Text(
                    text = if (controlState.isWifiConnected) "Wi-Fi Active" else "Wi-Fi ${controlState.wifiLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidViolet.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
fun QuickSearchWidget(
    onOpenSearch: () -> Unit,
    onOpenControlCenter: () -> Unit
) {
    LiquidGlassSurface(
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("widget_quick_search")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search installed apps",
                    tint = LiquidCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Search apps & quick launch...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.80f)
                )
            }

            IconButton(
                onClick = onOpenControlCenter,
                modifier = Modifier
                    .defaultMinSize(48.dp, 48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Open Control Center",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
