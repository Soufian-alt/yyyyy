package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ControlCenterState
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidAmber
import com.example.ui.theme.LiquidBlue
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMint
import com.example.ui.theme.LiquidViolet
import com.example.ui.theme.StatusConnected

@Composable
fun ControlCenterSheet(
    state: ControlCenterState,
    onRefresh: () -> Unit,
    onWifiClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onAirplaneClick: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleDnd: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onRequestWriteSettings: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onOpenVolumePanel: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onOpenLockPreview: () -> Unit,
    onRequestDefaultHome: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onRefresh()
            if (granted) onBluetoothClick()
        }
    )

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.54f))
            .clickable(onClick = onClose)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("control_center_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .clickable(enabled = false, onClick = {})
        ) {
            // Header Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Tune, null, tint = LiquidCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Control Center",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .defaultMinSize(48.dp, 48.dp)
                        .testTag("close_control_center_btn")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_close), tint = Color.White)
                }
            }

            if (!state.isDefaultHomeLauncher) {
                LiquidGlassClickable(
                    onClick = onRequestDefaultHome,
                    accentTint = LiquidCyan.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Rounded.Home, null, tint = LiquidCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Set My Liquid OS as Default Home", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Tap to assign Home Launcher role", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.78f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Connectivity Grid + Quick Toggles
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Connectivity Card (Wi-Fi, Bluetooth, Airplane Mode)
                LiquidGlassSurface(
                    elevated = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Connectivity",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        ConnectivityTile(
                            icon = Icons.Rounded.Wifi,
                            title = "Wi-Fi",
                            subtitle = state.wifiLabel,
                            active = state.isWifiConnected,
                            activeColor = LiquidBlue,
                            onClick = onWifiClick,
                            testTag = "cc_wifi_tile"
                        )
                        ConnectivityTile(
                            icon = Icons.Rounded.Bluetooth,
                            title = "Bluetooth",
                            subtitle = when {
                                state.bluetoothPermissionMissing -> "Allow permission"
                                state.isBluetoothEnabled -> "On"
                                else -> "Off"
                            },
                            active = state.isBluetoothEnabled,
                            activeColor = LiquidCyan,
                            onClick = {
                                if (state.bluetoothPermissionMissing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    btPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                } else {
                                    onBluetoothClick()
                                }
                            },
                            testTag = "cc_bluetooth_tile"
                        )
                        ConnectivityTile(
                            icon = Icons.Rounded.AirplanemodeActive,
                            title = "Airplane",
                            subtitle = if (state.isAirplaneModeOn) "On" else "Off",
                            active = state.isAirplaneModeOn,
                            activeColor = LiquidAmber,
                            onClick = onAirplaneClick,
                            testTag = "cc_airplane_tile"
                        )
                    }
                }

                // Right Column: Flashlight, DND, Lock Preview, Launcher Settings
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickSquareTile(
                            icon = if (state.isTorchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                            label = if (state.isTorchOn) "Torch On" else "Torch",
                            active = state.isTorchOn,
                            activeColor = LiquidAmber,
                            enabled = state.isTorchAvailable,
                            onClick = onToggleTorch,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_torch_tile")
                        )
                        QuickSquareTile(
                            icon = Icons.Rounded.DoNotDisturbOn,
                            label = if (state.isDndOn) "DND On" else "DND",
                            active = state.isDndOn,
                            activeColor = LiquidViolet,
                            onClick = onToggleDnd,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_dnd_tile")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickSquareTile(
                            icon = Icons.Rounded.Lock,
                            label = "Lock UI",
                            active = false,
                            activeColor = LiquidCyan,
                            onClick = onOpenLockPreview,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_lock_tile")
                        )
                        QuickSquareTile(
                            icon = Icons.Rounded.Settings,
                            label = "Liquid OS",
                            active = false,
                            activeColor = LiquidCyan,
                            onClick = onOpenLauncherSettings,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_launcher_settings_tile")
                        )
                    }

                    // Battery & RAM Summary Tile
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (state.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                                    contentDescription = "Battery",
                                    tint = if (state.isCharging) StatusConnected else LiquidMint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${state.batteryPercent}%", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            }
                            Text(
                                text = "${state.availableRamMb} MB free",
                                style = MaterialTheme.typography.labelMedium,
                                color = LiquidCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brightness & Volume Sliders
            LiquidGlassSurface(
                elevated = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Brightness6, null, tint = LiquidAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Brightness", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        if (!state.canWriteSystemSettings) {
                            Text(
                                text = "Grant System Write",
                                style = MaterialTheme.typography.labelMedium,
                                color = LiquidCyan,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onRequestWriteSettings)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        } else {
                            Text(
                                text = "${(state.brightnessFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Slider(
                        value = state.brightnessFraction,
                        onValueChange = onBrightnessChange,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = LiquidAmber,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("cc_brightness_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = LiquidCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Media Volume", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        Text(
                            text = "Panel",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidCyan,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onOpenVolumePanel)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Slider(
                        value = state.volumeFraction,
                        onValueChange = onVolumeChange,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = LiquidCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("cc_volume_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // System Settings Shortcuts
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    ShortcutChip(Icons.Rounded.Settings, "Android", onOpenSystemSettings)
                    ShortcutChip(Icons.Rounded.DisplaySettings, "Display", onOpenDisplaySettings)
                    ShortcutChip(Icons.Rounded.BatteryStd, "Battery", onOpenBatterySettings)
                }
            }
        }
    }
}

@Composable
private fun ConnectivityTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    LiquidGlassClickable(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        accentTint = if (active) activeColor.copy(alpha = 0.28f) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (active) activeColor else Color.White.copy(alpha = 0.14f))
            ) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.78f))
            }
        }
    }
}

@Composable
private fun QuickSquareTile(
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    LiquidGlassClickable(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(22.dp),
        accentTint = if (active) activeColor.copy(alpha = 0.30f) else null,
        modifier = modifier.height(82.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    !enabled -> Color.White.copy(alpha = 0.35f)
                    active -> activeColor
                    else -> Color.White
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun ShortcutChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    LiquidGlassClickable(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.defaultMinSize(minWidth = 88.dp, minHeight = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(icon, contentDescription = label, tint = LiquidCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
    }
}
