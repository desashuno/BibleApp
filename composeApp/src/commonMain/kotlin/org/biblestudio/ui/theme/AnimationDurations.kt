package org.biblestudio.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/**
 * Animation duration constants (milliseconds) and standard easing curves
 * aligned with Material 3 motion guidance.
 */
object AnimationDurations {
    const val FAST = 150
    const val MEDIUM = 250
    const val SLOW = 400

    /** Standard easing — enter + exit, most transitions. */
    val EaseStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Decelerate — elements entering the screen. */
    val EaseDecelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    /** Accelerate — elements leaving the screen. */
    val EaseAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)
}
