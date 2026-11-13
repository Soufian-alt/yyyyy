package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.MotionLevel
import com.example.data.model.ThemeMode
import com.example.ui.motion.MotionConfig

private val LiquidDarkColorScheme = darkColorScheme(
    primary = LiquidCyan,
    onPrimary = ObsidianDeep,
    primaryContainer = LiquidBlue.copy(alpha = 0.24f),
    onPrimaryContainer = Color.White,
    secondary = LiquidViolet,
    onSecondary = Color.White,
    secondaryContainer = LiquidViolet.copy(alpha = 0.22f),
    onSecondaryContainer = Color.White,
    tertiary = LiquidMint,
    background = ObsidianDeep,
    onBackground = Color.White,
    surface = MidnightNavy,
    onSurface = Color.White,
    surfaceVariant = GlassDarkTint,
    onSurfaceVariant = Color.White.copy(alpha = 0.78f),
    outline = Color.White.copy(alpha = 0.25f)
)

private val LiquidLightColorScheme = lightColorScheme(
    primary = LiquidBlue,
    onPrimary = Color.White,
    primaryContainer = LiquidBlue.copy(alpha = 0.16f),
    onPrimaryContainer = MidnightNavy,
    secondary = LiquidViolet,
    onSecondary = Color.White,
    secondaryContainer = LiquidViolet.copy(alpha = 0.14f),
    onSecondaryContainer = MidnightNavy,
    tertiary = LiquidMint,
    background = FrostLightBase,
    onBackground = ObsidianDeep,
    surface = FrostCardLight,
    onSurface = ObsidianDeep,
    surfaceVariant = GlassLightTint,
    onSurfaceVariant = ObsidianDeep.copy(alpha = 0.76f),
    outline = ObsidianDeep.copy(alpha = 0.18f)
)

@Immutable
data class LiquidGlassThemeConfig(
    val isDarkGlass: Boolean = true,
    val glassOpacity: Float = 0.24f,
    val blurRadiusDp: Float = 16f,
    val performanceMode: Boolean = false,
    val motionConfig: MotionConfig = MotionConfig(MotionLevel.FULL)
)

val LocalLiquidGlassConfig = staticCompositionLocalOf { LiquidGlassThemeConfig() }

@Composable
fun MyLiquidOSTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    glassOpacity: Float = 0.24f,
    blurRadiusDp: Float = 16f,
    motionLevel: MotionLevel = MotionLevel.FULL,
    performanceMode: Boolean = false,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LiquidDarkColorScheme
        else -> LiquidLightColorScheme
    }

    val effectiveMotion = if (performanceMode && motionLevel == MotionLevel.FULL) {
        MotionLevel.REDUCED
    } else {
        motionLevel
    }

    val glassConfig = LiquidGlassThemeConfig(
        isDarkGlass = darkTheme,
        glassOpacity = glassOpacity.coerceIn(0.08f, 0.75f),
        blurRadiusDp = if (performanceMode) 0f else blurRadiusDp.coerceIn(0f, 32f),
        performanceMode = performanceMode,
        motionConfig = MotionConfig(effectiveMotion)
    )

    CompositionLocalProvider(LocalLiquidGlassConfig provides glassConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LiquidTypography,
            content = content
        )
    }
}
