package com.example.ui.glass

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassDarkTint
import com.example.ui.theme.GlassLightTint
import com.example.ui.theme.GlassSpecularWhite
import com.example.ui.theme.LocalLiquidGlassConfig

/**
 * Lightweight, 60Hz-safe Liquid Glass container inspired by frosted refractive glass.
 * On Android 11 (API 30, e.g. Galaxy A03s) and when Performance Mode is active, it avoids
 * offscreen RenderEffect passes and renders layered translucent frost gradients + specular rims
 * directly in a single GPU pass for smooth 60fps.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    elevated: Boolean = false,
    accentTint: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val config = LocalLiquidGlassConfig.current
    val baseAlpha = if (elevated) {
        (config.glassOpacity + 0.14f).coerceAtMost(0.84f)
    } else {
        config.glassOpacity
    }

    val primaryColor = accentTint ?: if (config.isDarkGlass) GlassDarkTint else GlassLightTint
    val highlightTopAlpha = if (config.isDarkGlass) 0.28f else 0.72f
    val highlightBottomAlpha = if (config.isDarkGlass) 0.08f else 0.30f

    val fillBrush = remember(primaryColor, baseAlpha, config.isDarkGlass) {
        if (config.isDarkGlass) {
            Brush.linearGradient(
                colors = listOf(
                    primaryColor.copy(alpha = (baseAlpha + 0.14f).coerceAtMost(0.88f)),
                    primaryColor.copy(alpha = baseAlpha),
                    Color(0xFF0A1222).copy(alpha = (baseAlpha + 0.08f).coerceAtMost(0.85f))
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    primaryColor.copy(alpha = (baseAlpha + 0.42f).coerceAtMost(0.92f)),
                    primaryColor.copy(alpha = (baseAlpha + 0.28f).coerceAtMost(0.82f)),
                    Color(0xFFDCE7F7).copy(alpha = (baseAlpha + 0.22f).coerceAtMost(0.78f))
                )
            )
        }
    }

    val rimBrush = remember(config.isDarkGlass, highlightTopAlpha, highlightBottomAlpha) {
        Brush.linearGradient(
            colors = listOf(
                GlassSpecularWhite.copy(alpha = highlightTopAlpha),
                GlassSpecularWhite.copy(alpha = highlightBottomAlpha * 0.6f),
                GlassSpecularWhite.copy(alpha = highlightBottomAlpha)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush = fillBrush, shape = shape)
            .drawBehind {
                if (!config.performanceMode) {
                    // Subtle internal specular sheen along the top curve
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GlassSpecularWhite.copy(alpha = if (config.isDarkGlass) 0.10f else 0.32f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.48f
                        )
                    )
                }
            }
            .border(
                border = BorderStroke(width = 1.dp, brush = rimBrush),
                shape = shape
            ),
        content = content
    )
}

/**
 * Optional backdrop blur modifier applied only on Android 12+ (API 31+) when blur > 0 and
 * Performance Mode is off, protecting Android 11 (API 30) and 3GB RAM devices from lag.
 */
@Composable
fun Modifier.optionalBackdropBlur(radiusDp: Dp): Modifier {
    val config = LocalLiquidGlassConfig.current
    return if (!config.performanceMode &&
        config.blurRadiusDp > 0f &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        val clamped = radiusDp.value.coerceAtMost(config.blurRadiusDp).dp
        this.blur(clamped)
    } else {
        this
    }
}

/**
 * Interactive bounce + ripple wrapper adhering to 48dp minimum touch targets and MotionConfig.
 */
@Composable
fun LiquidGlassClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    minTouchSize: Dp = 48.dp,
    accentTint: Color? = null,
    elevated: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    val config = LocalLiquidGlassConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && config.motionConfig.isEnabled) 0.93f else 1f,
        animationSpec = config.motionConfig.bouncySpring(),
        label = "glassPressScale"
    )

    LiquidGlassSurface(
        modifier = modifier
            .defaultMinSize(minWidth = minTouchSize, minHeight = minTouchSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick
            ),
        shape = shape,
        elevated = elevated,
        accentTint = accentTint
    ) {
        Box(contentAlignment = contentAlignment, content = content)
    }
}

/**
 * Subtle translucent pill used for quick status badges and section headers.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    accentTint: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = CircleShape,
        accentTint = accentTint,
        content = content
    )
}
