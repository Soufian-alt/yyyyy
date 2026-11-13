package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.data.model.IconSizeOption
import com.example.data.model.LauncherSettings
import com.example.data.model.LockClockStyle
import com.example.data.model.MotionLevel
import com.example.data.model.ThemeMode
import com.example.data.model.WallpaperPreset
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMint
import com.example.ui.theme.StatusConnected

@Composable
fun SettingsScreen(
    settings: LauncherSettings,
    isDefaultHome: Boolean,
    onRequestDefaultHome: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onWallpaperChange: (WallpaperPreset, String?) -> Unit,
    onGlassOpacityChange: (Float) -> Unit,
    onBlurRadiusChange: (Float) -> Unit,
    onMotionLevelChange: (MotionLevel) -> Unit,
    onIconSizeChange: (IconSizeOption) -> Unit,
    onShowLabelsChange: (Boolean) -> Unit,
    onShowDockChange: (Boolean) -> Unit,
    onDockSlotsChange: (Int) -> Unit,
    onPerformanceModeChange: (Boolean) -> Unit,
    onLockStyleChange: (LockClockStyle) -> Unit,
    onOpenLockPreview: () -> Unit,
    onResetLayout: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onWallpaperChange(WallpaperPreset.CUSTOM_URI, uri.toString())
            }
        }
    )

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .defaultMinSize(48.dp, 48.dp)
                        .testTag("settings_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Home", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Liquid OS Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Default Launcher Card
                SettingsSectionCard(icon = Icons.Rounded.Home, title = "Home Launcher Role") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDefaultHome) "Active Default Launcher" else "Not Default Launcher",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDefaultHome) StatusConnected else Color.White
                            )
                            Text(
                                text = "Assign My Liquid OS as your Home screen app.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LiquidGlassClickable(onClick = onRequestDefaultHome) {
                            Text(
                                text = if (isDefaultHome) "Change" else "Set Default",
                                color = LiquidCyan,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                // Performance Mode Card (Tuned for Galaxy A03s / 3GB RAM / 60Hz)
                SettingsSectionCard(icon = Icons.Rounded.Speed, title = "Performance Mode (3GB RAM / 60Hz)") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Performance Mode", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                "Disables backdrop blur, reduces glass sheen, trims icon cache, and simplifies motion for locked 60fps on Android 11.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.76f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = settings.performanceMode,
                            onCheckedChange = onPerformanceModeChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LiquidMint),
                            modifier = Modifier.testTag("settings_performance_switch")
                        )
                    }
                }

                // Theme & Glass Appearance
                SettingsSectionCard(icon = Icons.Rounded.Palette, title = "Theme & Liquid Glass") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEach { mode ->
                            val selected = mode == settings.themeMode
                            LiquidGlassClickable(
                                onClick = { onThemeChange(mode) },
                                accentTint = if (selected) LiquidCyan.copy(alpha = 0.30f) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Frost Glass Opacity (${(settings.glassOpacity * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Slider(
                        value = settings.glassOpacity,
                        onValueChange = onGlassOpacityChange,
                        valueRange = 0.10f..0.65f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = LiquidCyan)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.BlurOn, null, tint = LiquidCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                "Blur Radius (${settings.blurRadiusDp.toInt()} dp)"
                            } else {
                                "Frosted Shader Mode (Android 11 uses fast gradient frost)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Slider(
                        value = settings.blurRadiusDp,
                        onValueChange = onBlurRadiusChange,
                        enabled = !settings.performanceMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                        valueRange = 0f..32f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = LiquidCyan)
                    )
                }

                // Wallpaper Studio
                SettingsSectionCard(icon = Icons.Rounded.Wallpaper, title = "Wallpapers") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        val presets = WallpaperPreset.entries.filter { it.drawableRes != null }
                        items(presets) { preset ->
                            val selected = settings.wallpaperPreset == preset
                            Box(
                                modifier = Modifier
                                    .width(86.dp)
                                    .height(136.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) LiquidCyan else Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { onWallpaperChange(preset, null) }
                            ) {
                                Image(
                                    painter = painterResource(preset.drawableRes!!),
                                    contentDescription = preset.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = LiquidCyan,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(20.dp)
                                    )
                                }
                                Text(
                                    text = preset.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.45f))
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LiquidGlassClickable(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, null, tint = LiquidCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Photo from Gallery", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                // Animation Engine
                SettingsSectionCard(icon = Icons.Rounded.Animation, title = "Animation Engine") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MotionLevel.entries.forEach { level ->
                            val selected = level == settings.motionLevel
                            LiquidGlassClickable(
                                onClick = { onMotionLevelChange(level) },
                                accentTint = if (selected) LiquidCyan.copy(alpha = 0.30f) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)
                                ) {
                                    Text(level.label, style = MaterialTheme.typography.labelMedium, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Icons & Dock Customization
                SettingsSectionCard(icon = Icons.Rounded.Dock, title = "Icons & Glass Dock") {
                    Text("Icon Size", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        IconSizeOption.entries.forEach { option ->
                            val selected = option == settings.iconSize
                            LiquidGlassClickable(
                                onClick = { onIconSizeChange(option) },
                                accentTint = if (selected) LiquidCyan.copy(alpha = 0.30f) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow("Show App Labels", settings.showIconLabels, onShowLabelsChange)
                    SettingToggleRow("Show Bottom Glass Dock", settings.showDock, onShowDockChange)

                    if (settings.showDock) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dock Pinned Slots", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(4, 5).forEach { count ->
                                    val selected = settings.dockMaxSlots == count
                                    LiquidGlassClickable(
                                        onClick = { onDockSlotsChange(count) },
                                        accentTint = if (selected) LiquidCyan.copy(alpha = 0.30f) else null,
                                        modifier = Modifier.defaultMinSize(48.dp, 40.dp)
                                    ) {
                                        Text("$count", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Lock Screen Preview Customization
                SettingsSectionCard(icon = Icons.Rounded.Lock, title = "Lock Screen Preview") {
                    Text(
                        text = "Customize the optional in-app Lock Screen preview. Android security reserves system lockscreen replacement for OS/OEM components.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        LockClockStyle.entries.forEach { style ->
                            val selected = style == settings.lockClockStyle
                            LiquidGlassClickable(
                                onClick = { onLockStyleChange(style) },
                                accentTint = if (selected) LiquidCyan.copy(alpha = 0.30f) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = style.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LiquidGlassClickable(onClick = onOpenLockPreview, modifier = Modifier.fillMaxWidth()) {
                        Text("Open Lock Screen Preview", color = LiquidCyan, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }

                // Reset & Android 11 Notes
                SettingsSectionCard(icon = Icons.Rounded.Info, title = "Android 11 & Storage") {
                    Text(
                        text = "• Package Visibility: Uses Android 11+ <queries> for MAIN/LAUNCHER apps without broad QUERY_ALL_PACKAGES.\n" +
                            "• System Toggles: Wi-Fi, Bluetooth & Airplane Mode open official system panels/settings as required by Android 11.\n" +
                            "• Flashlight & Volume work directly; System Brightness works after granting Modify System Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LiquidGlassClickable(onClick = onResetLayout, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                            Icon(Icons.Rounded.Refresh, null, tint = LiquidCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Home Layout to Installed Apps", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    LiquidGlassSurface(
        elevated = true,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = LiquidCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LiquidCyan)
        )
    }
}
