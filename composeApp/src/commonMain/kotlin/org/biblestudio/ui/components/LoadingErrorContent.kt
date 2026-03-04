package org.biblestudio.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.biblestudio.ui.theme.AnimationDurations

/**
 * Generic tri-state container: loading → error → empty → content.
 *
 * Uses [AnimatedContent] with [AnimationDurations.MEDIUM] to smoothly
 * transition between states, activating the dormant animation tokens.
 *
 * @param isLoading Whether to show the loading indicator.
 * @param error Error message to display, or null.
 * @param data The loaded data value (may be null before first load).
 * @param emptyMessage Message to show when [isEmpty] returns true.
 * @param isEmpty Predicate to check if [data] represents an empty state.
 * @param content Composable rendered with the non-null, non-empty [data].
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun <T> LoadingErrorContent(
    isLoading: Boolean,
    error: String?,
    data: T?,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    isEmpty: (T) -> Boolean = { false },
    content: @Composable (T) -> Unit,
) {
    // Discriminant key for AnimatedContent
    val stateKey = when {
        isLoading -> "loading"
        error != null -> "error"
        data == null || isEmpty(data) -> "empty"
        else -> "content"
    }

    AnimatedContent(
        targetState = stateKey,
        modifier = modifier,
        transitionSpec = {
            fadeIn(tween(AnimationDurations.MEDIUM, easing = AnimationDurations.EaseStandard)) togetherWith
                fadeOut(tween(AnimationDurations.MEDIUM, easing = AnimationDurations.EaseStandard))
        },
        label = "LoadingErrorContent",
    ) { state ->
        when (state) {
            "loading" -> LoadingIndicator(fullScreen = true)
            "error" -> ErrorMessage(message = error ?: "Unknown error")
            "empty" -> EmptyStateMessage(message = emptyMessage, centered = true)
            else -> data?.let { content(it) }
        }
    }
}
