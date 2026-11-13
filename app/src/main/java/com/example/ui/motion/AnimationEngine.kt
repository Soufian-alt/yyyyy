package com.example.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntOffset
import com.example.data.model.MotionLevel

@Immutable
class MotionConfig(val level: MotionLevel) {

    val isEnabled: Boolean get() = level != MotionLevel.OFF

    fun <T> standardSpring(): FiniteAnimationSpec<T> = when (level) {
        MotionLevel.FULL -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        MotionLevel.REDUCED -> tween(durationMillis = 150, easing = FastOutSlowInEasing)
        MotionLevel.OFF -> tween(durationMillis = 0)
    }

    fun <T> bouncySpring(): FiniteAnimationSpec<T> = when (level) {
        MotionLevel.FULL -> spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMedium
        )
        MotionLevel.REDUCED -> tween(durationMillis = 120, easing = FastOutSlowInEasing)
        MotionLevel.OFF -> tween(durationMillis = 0)
    }

    fun drawerEnter(): EnterTransition = when (level) {
        MotionLevel.FULL -> slideInVertically(
            animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
            initialOffsetY = { it / 3 }
        ) + fadeIn(tween(200))
        MotionLevel.REDUCED -> fadeIn(tween(140))
        MotionLevel.OFF -> EnterTransition.None
    }

    fun drawerExit(): ExitTransition = when (level) {
        MotionLevel.FULL -> slideOutVertically(
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            targetOffsetY = { it / 3 }
        ) + fadeOut(tween(170))
        MotionLevel.REDUCED -> fadeOut(tween(120))
        MotionLevel.OFF -> ExitTransition.None
    }

    fun controlCenterEnter(): EnterTransition = when (level) {
        MotionLevel.FULL -> slideInVertically(
            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
            initialOffsetY = { -it / 3 }
        ) + fadeIn(tween(200))
        MotionLevel.REDUCED -> fadeIn(tween(140))
        MotionLevel.OFF -> EnterTransition.None
    }

    fun controlCenterExit(): ExitTransition = when (level) {
        MotionLevel.FULL -> slideOutVertically(
            animationSpec = tween(190, easing = FastOutSlowInEasing),
            targetOffsetY = { -it / 3 }
        ) + fadeOut(tween(160))
        MotionLevel.REDUCED -> fadeOut(tween(120))
        MotionLevel.OFF -> ExitTransition.None
    }

    fun folderEnter(): EnterTransition = when (level) {
        MotionLevel.FULL -> scaleIn(
            initialScale = 0.86f,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(tween(180))
        MotionLevel.REDUCED -> fadeIn(tween(130))
        MotionLevel.OFF -> EnterTransition.None
    }

    fun folderExit(): ExitTransition = when (level) {
        MotionLevel.FULL -> scaleOut(
            targetScale = 0.90f,
            animationSpec = tween(150)
        ) + fadeOut(tween(140))
        MotionLevel.REDUCED -> fadeOut(tween(110))
        MotionLevel.OFF -> ExitTransition.None
    }

    fun offsetSpring(): FiniteAnimationSpec<IntOffset> = when (level) {
        MotionLevel.FULL -> spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow)
        MotionLevel.REDUCED -> tween(140)
        MotionLevel.OFF -> tween(0)
    }
}
