package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.LauncherSettings
import com.example.data.model.WallpaperPreset
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidViolet
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.MidnightNavy

@Composable
fun WallpaperBackground(
    settings: LauncherSettings,
    modifier: Modifier = Modifier
) {
    val glassConfig = LocalLiquidGlassConfig.current
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize().background(MidnightNavy)) {
        if (settings.wallpaperPreset == WallpaperPreset.CUSTOM_URI && !settings.customWallpaperUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(settings.customWallpaperUri)
                    .size(1080, 1920)
                    .crossfade(!glassConfig.performanceMode)
                    .build(),
                contentDescription = "Custom launcher wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val resId = settings.wallpaperPreset.drawableRes ?: R.drawable.img_wallpaper_aurora
            Image(
                painter = painterResource(id = resId),
                contentDescription = "${settings.wallpaperPreset.displayName} wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Subtle refractive tint overlay for readable icons and glass depth
        if (!glassConfig.performanceMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                LiquidCyan.copy(alpha = 0.08f),
                                LiquidViolet.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        val topScrim = if (glassConfig.isDarkGlass) 0.28f else 0.12f
        val bottomScrim = if (glassConfig.isDarkGlass) 0.42f else 0.18f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = topScrim),
                            Color.Transparent,
                            Color.Black.copy(alpha = bottomScrim)
                        )
                    )
                )
        )
    }
}
