package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.example.R

@Immutable
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isSystemApp: Boolean,
    val firstInstallTime: Long,
    val launchCount: Int = 0,
    val lastLaunchedAt: Long = 0L
) {
    val componentKey: String get() = "$packageName/$activityName"
}

enum class WidgetType(val title: String, val subtitle: String) {
    CLOCK_DATE("Liquid Clock", "Live time, date & day summary"),
    DEVICE_STATS("Device Pulse", "Real battery, charging & RAM monitor"),
    QUICK_SEARCH("Glass Search", "Instant app search & quick launch")
}

sealed interface HomeItem {
    val id: Long
    val page: Int
    val position: Int

    @Immutable
    data class AppTile(
        override val id: Long,
        override val page: Int,
        override val position: Int,
        val app: AppInfo
    ) : HomeItem

    @Immutable
    data class FolderTile(
        override val id: Long,
        override val page: Int,
        override val position: Int,
        val folderId: String,
        val name: String,
        val apps: List<AppInfo>
    ) : HomeItem

    @Immutable
    data class WidgetTile(
        override val id: Long,
        override val page: Int,
        override val position: Int,
        val widgetType: WidgetType
    ) : HomeItem
}

@Immutable
data class DockItem(
    val id: Long,
    val dockOrder: Int,
    val app: AppInfo
)

enum class WallpaperPreset(
    val displayName: String,
    @get:DrawableRes val drawableRes: Int?
) {
    AURORA("Aurora Prism", R.drawable.img_wallpaper_aurora),
    OCEAN("Arctic Frost", R.drawable.img_wallpaper_ocean),
    SUNSET("Amber Refraction", R.drawable.img_wallpaper_sunset),
    OBSIDIAN("Obsidian Glass", R.drawable.img_wallpaper_obsidian),
    CUSTOM_URI("Custom Photo", null)
}

enum class ThemeMode(val label: String) {
    DARK("Dark Glass"),
    LIGHT("Light Frost"),
    SYSTEM("System")
}

enum class MotionLevel(val label: String, val description: String) {
    FULL("Full Liquid", "Spring physics & soft bounce"),
    REDUCED("Reduced", "Fast minimal transitions"),
    OFF("Instant", "Zero motion for max speed")
}

enum class IconSizeOption(val label: String, val iconDp: Float) {
    COMPACT("Compact", 48f),
    BALANCED("Balanced", 56f),
    LARGE("Large", 64f)
}

enum class AppSortOrder(val label: String) {
    NAME_ASC("A–Z"),
    NAME_DESC("Z–A"),
    MOST_USED("Most Used"),
    NEWEST("Newest")
}

enum class LockClockStyle(val label: String) {
    MINIMAL_GLASS("Minimal Glass"),
    BOLD_EDITORIAL("Bold Display"),
    COMPACT_PILL("Compact Pill")
}

@Immutable
data class LauncherSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.AURORA,
    val customWallpaperUri: String? = null,
    val glassOpacity: Float = 0.24f,
    val blurRadiusDp: Float = 16f,
    val motionLevel: MotionLevel = MotionLevel.FULL,
    val iconSize: IconSizeOption = IconSizeOption.BALANCED,
    val showIconLabels: Boolean = true,
    val showDock: Boolean = true,
    val dockMaxSlots: Int = 4,
    val performanceMode: Boolean = false,
    val lockClockStyle: LockClockStyle = LockClockStyle.MINIMAL_GLASS,
    val hasInitializedHome: Boolean = false
)

@Immutable
data class ControlCenterState(
    val isWifiConnected: Boolean = false,
    val wifiLabel: String = "Wi-Fi",
    val isBluetoothEnabled: Boolean = false,
    val bluetoothPermissionMissing: Boolean = false,
    val isAirplaneModeOn: Boolean = false,
    val isTorchOn: Boolean = false,
    val isTorchAvailable: Boolean = false,
    val isDndOn: Boolean = false,
    val hasDndPolicyAccess: Boolean = false,
    val brightnessFraction: Float = 0.6f,
    val canWriteSystemSettings: Boolean = false,
    val volumeFraction: Float = 0.5f,
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val availableRamMb: Long = 0L,
    val totalRamMb: Long = 0L,
    val isDefaultHomeLauncher: Boolean = false
)

sealed interface ActiveOverlay {
    data object None : ActiveOverlay
    data class AppDrawer(val focusSearch: Boolean = false) : ActiveOverlay
    data object ControlCenter : ActiveOverlay
    data class Folder(val folderId: String) : ActiveOverlay
    data object Settings : ActiveOverlay
    data object LockPreview : ActiveOverlay
    data class WidgetPicker(val targetPage: Int) : ActiveOverlay
}
