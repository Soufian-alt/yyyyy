package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.IconSizeOption
import com.example.data.model.LauncherSettings
import com.example.data.model.LockClockStyle
import com.example.data.model.MotionLevel
import com.example.data.model.ThemeMode
import com.example.data.model.WallpaperPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.liquidSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "liquid_os_settings"
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val GLASS_OPACITY = floatPreferencesKey("glass_opacity")
        val BLUR_RADIUS_DP = floatPreferencesKey("blur_radius_dp")
        val MOTION_LEVEL = stringPreferencesKey("motion_level")
        val ICON_SIZE = stringPreferencesKey("icon_size")
        val SHOW_ICON_LABELS = booleanPreferencesKey("show_icon_labels")
        val SHOW_DOCK = booleanPreferencesKey("show_dock")
        val DOCK_MAX_SLOTS = intPreferencesKey("dock_max_slots")
        val PERFORMANCE_MODE = booleanPreferencesKey("performance_mode")
        val LOCK_CLOCK_STYLE = stringPreferencesKey("lock_clock_style")
        val HAS_INITIALIZED_HOME = booleanPreferencesKey("has_initialized_home")
    }

    val settingsFlow: Flow<LauncherSettings> = context.liquidSettingsDataStore.data.map { prefs ->
        LauncherSettings(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.DARK,
            wallpaperPreset = prefs[Keys.WALLPAPER_PRESET]?.let {
                runCatching { WallpaperPreset.valueOf(it) }.getOrNull()
            } ?: WallpaperPreset.AURORA,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI],
            glassOpacity = prefs[Keys.GLASS_OPACITY] ?: 0.24f,
            blurRadiusDp = prefs[Keys.BLUR_RADIUS_DP] ?: 16f,
            motionLevel = prefs[Keys.MOTION_LEVEL]?.let { runCatching { MotionLevel.valueOf(it) }.getOrNull() }
                ?: MotionLevel.FULL,
            iconSize = prefs[Keys.ICON_SIZE]?.let { runCatching { IconSizeOption.valueOf(it) }.getOrNull() }
                ?: IconSizeOption.BALANCED,
            showIconLabels = prefs[Keys.SHOW_ICON_LABELS] ?: true,
            showDock = prefs[Keys.SHOW_DOCK] ?: true,
            dockMaxSlots = (prefs[Keys.DOCK_MAX_SLOTS] ?: 4).coerceIn(4, 5),
            performanceMode = prefs[Keys.PERFORMANCE_MODE] ?: false,
            lockClockStyle = prefs[Keys.LOCK_CLOCK_STYLE]?.let {
                runCatching { LockClockStyle.valueOf(it) }.getOrNull()
            } ?: LockClockStyle.MINIMAL_GLASS,
            hasInitializedHome = prefs[Keys.HAS_INITIALIZED_HOME] ?: false
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.liquidSettingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateWallpaperPreset(preset: WallpaperPreset, customUri: String? = null) {
        context.liquidSettingsDataStore.edit { prefs ->
            prefs[Keys.WALLPAPER_PRESET] = preset.name
            if (customUri != null) {
                prefs[Keys.CUSTOM_WALLPAPER_URI] = customUri
            }
        }
    }

    suspend fun updateGlassOpacity(opacity: Float) {
        context.liquidSettingsDataStore.edit { it[Keys.GLASS_OPACITY] = opacity.coerceIn(0.08f, 0.70f) }
    }

    suspend fun updateBlurRadius(radiusDp: Float) {
        context.liquidSettingsDataStore.edit { it[Keys.BLUR_RADIUS_DP] = radiusDp.coerceIn(0f, 32f) }
    }

    suspend fun updateMotionLevel(level: MotionLevel) {
        context.liquidSettingsDataStore.edit { it[Keys.MOTION_LEVEL] = level.name }
    }

    suspend fun updateIconSize(size: IconSizeOption) {
        context.liquidSettingsDataStore.edit { it[Keys.ICON_SIZE] = size.name }
    }

    suspend fun updateShowIconLabels(show: Boolean) {
        context.liquidSettingsDataStore.edit { it[Keys.SHOW_ICON_LABELS] = show }
    }

    suspend fun updateShowDock(show: Boolean) {
        context.liquidSettingsDataStore.edit { it[Keys.SHOW_DOCK] = show }
    }

    suspend fun updateDockMaxSlots(slots: Int) {
        context.liquidSettingsDataStore.edit { it[Keys.DOCK_MAX_SLOTS] = slots.coerceIn(4, 5) }
    }

    suspend fun updatePerformanceMode(enabled: Boolean) {
        context.liquidSettingsDataStore.edit { prefs ->
            prefs[Keys.PERFORMANCE_MODE] = enabled
        }
    }

    suspend fun updateLockClockStyle(style: LockClockStyle) {
        context.liquidSettingsDataStore.edit { it[Keys.LOCK_CLOCK_STYLE] = style.name }
    }

    suspend fun markHomeInitialized(initialized: Boolean = true) {
        context.liquidSettingsDataStore.edit { it[Keys.HAS_INITIALIZED_HOME] = initialized }
    }
}
