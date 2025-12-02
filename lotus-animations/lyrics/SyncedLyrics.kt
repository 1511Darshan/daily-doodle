package com.yourapp.animations.lyrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlin.math.PI
import kotlin.math.sin

/**
 * A plain lyrics line with no animation
 */
@Composable
fun PlainLyricsLine(
    line: String,
    style: TextStyle,
    color: Color = LocalContentColor.current,
    modifier: Modifier = Modifier
) {
    Text(
        text = line,
        style = style,
        color = color,
        modifier = modifier
    )
}

/**
 * A synced lyrics line that animates based on playback position.
 * Features:
 * - Color animation when line becomes active
 * - Scale animation for emphasis
 * - Gradient progress indicator
 * - Glow effect during playback
 *
 * @param positionFlow Flow of current playback position in milliseconds
 * @param time Start time of this lyrics line in milliseconds
 * @param nextTime Start time of the next line (or end time)
 * @param line The lyrics text
 * @param style Text style for the lyrics
 * @param onClick Callback when user taps to seek
 * @param onBecomeCurrent Callback when this line becomes active (for auto-scroll)
 */
@Composable
fun SyncedLyricsLine(
    positionFlow: Flow<Long>,
    time: Int,
    nextTime: Int,
    line: String,
    style: TextStyle,
    onClick: () -> Unit,
    onBecomeCurrent: (textHeight: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val position by positionFlow.collectAsState(0)
    var textHeight by remember {
        mutableFloatStateOf(0f)
    }
    val isCurrentLine by remember {
        derivedStateOf {
            position in time..nextTime
        }
    }

    LaunchedEffect(isCurrentLine) {
        if (isCurrentLine) {
            onBecomeCurrent(textHeight)
        }
    }

    val progressFraction by remember {
        derivedStateOf {
            ((position.toFloat() - time) / (nextTime - time))
                .coerceIn(0f, 1f)
        }
    }

    val gradientOrigin by remember {
        derivedStateOf {
            progressFraction * textHeight
        }
    }

    val localContentColor = LocalContentColor.current

    val color by animateColorAsState(
        targetValue = if (isCurrentLine) {
            localContentColor
        } else localContentColor.copy(alpha = .5f),
        label = "lyrics-color-animation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isCurrentLine) 1.05f else 1f,
        label = "current-line-scale-animation"
    )

    Text(
        text = line,
        style = style
            .copy(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = color.alpha / 2)
                    ),
                    startY = gradientOrigin - 10f,
                    endY = gradientOrigin + 10f
                ),
                shadow = Shadow(
                    color = localContentColor.copy(alpha = .5f),
                    blurRadius = if (isCurrentLine) progressFraction * 20f else 0f
                )
            ),
        color = color,
        modifier = modifier
            .onGloballyPositioned {
                textHeight = it.size.height.toFloat()
            }
            .graphicsLayer {
                transformOrigin = when (style.textAlign) {
                    TextAlign.Center -> TransformOrigin(.5f, .5f)
                    TextAlign.End -> TransformOrigin(1f, .5f)
                    else -> TransformOrigin(0f, .5f)
                }
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}

/**
 * Animated bubbles that appear between synced lyrics lines.
 * Shows waiting progress with bouncing, floating bubbles.
 */
@Composable
fun BubblesLine(
    positionFlow: Flow<Long>,
    time: Int,
    nextTime: Int,
    modifier: Modifier = Modifier
) {
    val position by positionFlow.collectAsState(-1)
    var bubblesContainerHeight by remember {
        mutableFloatStateOf(0f)
    }
    val isCurrentLine by remember {
        derivedStateOf {
            position >= 0L && position in time..nextTime
        }
    }

    val progressFraction by remember {
        derivedStateOf {
            ((position.toFloat() - time) / (nextTime - time))
                .coerceIn(0f, 1f)
        }
    }

    val density = LocalDensity.current
    val height = with(density) {
        MaterialTheme.typography.headlineMedium.fontSize.toDp()
    }

    val infiniteTransition = rememberInfiniteTransition(
        label = "bubbles-transition"
    )

    val firstBubbleProgress by remember {
        derivedStateOf {
            (progressFraction / .33f).coerceIn(0f, 1f)
        }
    }

    val firstBubbleTranslationX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "first-bubble-translation-x"
    )

    val secondBubbleProgress by remember {
        derivedStateOf {
            ((progressFraction - .33f) / .33f).coerceIn(0f, 1f)
        }
    }

    val secondBubbleTranslationX by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(
                offsetMillis = 500,
                offsetType = StartOffsetType.FastForward
            )
        ),
        label = "second-bubble-translation-x"
    )

    val thirdBubbleProgress by remember {
        derivedStateOf {
            ((progressFraction - .33f * 2) / .33f).coerceIn(0f, 1f)
        }
    }

    val thirdBubbleTranslationX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(
                offsetMillis = 1000,
                offsetType = StartOffsetType.FastForward
            )
        ),
        label = "third-bubble-translation-x"
    )

    val scale by animateFloatAsState(
        targetValue = if (progressFraction < .97f) 1f else 1.2f,
        label = "bubbles-scale-before-next-line"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(height)
            .onGloballyPositioned {
                bubblesContainerHeight = it.size.height.toFloat()
            },
    ) {
        AnimatedVisibility(
            visible = isCurrentLine,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .fillMaxHeight()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Bubble(
                    bubbleHeight = height,
                    containerHeight = bubblesContainerHeight,
                    animationProgress = firstBubbleProgress,
                    translationX = firstBubbleTranslationX,
                    translationOffset = secondBubbleTranslationX
                )

                Bubble(
                    bubbleHeight = height,
                    containerHeight = bubblesContainerHeight,
                    animationProgress = secondBubbleProgress,
                    translationX = secondBubbleTranslationX,
                    translationOffset = thirdBubbleTranslationX
                )

                Bubble(
                    bubbleHeight = height,
                    containerHeight = bubblesContainerHeight,
                    animationProgress = thirdBubbleProgress,
                    translationX = thirdBubbleTranslationX,
                    translationOffset = firstBubbleTranslationX
                )
            }
        }
    }
}

/**
 * A single animated bubble with glow effect
 */
@Composable
fun Bubble(
    bubbleHeight: Dp,
    containerHeight: Float,
    animationProgress: Float,
    translationX: Float,
    translationOffset: Float,
    color: Color = LocalContentColor.current,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(bubbleHeight * .7f)
            .graphicsLayer {
                this.translationY =
                    -containerHeight / 6 *
                            (sin(20 * (animationProgress - .25f) / PI.toFloat()) / 2 + .5f) +
                            translationX * translationOffset / 2

                this.translationX = translationX
                val scale = .5f + animationProgress / 2
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                drawCircle(
                    radius = size.width,
                    brush = Brush.radialGradient(
                        0f to Color.Transparent,
                        .5f to Color.Transparent,
                        .5f to color.copy(alpha = animationProgress / 2 - .25f),
                        .6f to color.copy(alpha = animationProgress / 3 - .25f),
                        .8f to Color.Transparent,
                        radius = size.width
                    )
                )

                drawCircle(
                    color = color.copy(
                        alpha = .25f + animationProgress
                    )
                )
            }
    )
}

/**
 * A toggle switch for synced/plain lyrics with animated capsule
 */
@Composable
fun LyricsTypeSwitch(
    isSynced: Boolean,
    onIsSyncedSwitch: (Boolean) -> Unit,
    syncedText: String = "Synced",
    plainText: String = "Plain",
    contentColor: Color = LocalContentColor.current,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
            .clip(ShapeDefaults.ExtraLarge)
            .background(color = contentColor.copy(alpha = .1f))
    ) {
        var midPoint by remember {
            mutableStateOf(0.dp)
        }
        val density = LocalDensity.current
        val capsuleOffset by animateDpAsState(
            targetValue = if (isSynced) 0.dp else midPoint,
            label = "capsule-offset-animation"
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(.5f)
                .offset(x = capsuleOffset)
                .clip(ShapeDefaults.ExtraLarge)
                .background(color = contentColor.copy(alpha = .1f))
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .onGloballyPositioned {
                    midPoint = with(density) { (it.size.width / 2).toDp() }
                }
        ) {
            Text(
                text = syncedText,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(ShapeDefaults.ExtraLarge)
                    .clickable(
                        enabled = !isSynced && enabled,
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        }
                    ) {
                        onIsSyncedSwitch(true)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = plainText,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(ShapeDefaults.ExtraLarge)
                    .clickable(
                        enabled = isSynced && enabled,
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        }
                    ) {
                        onIsSyncedSwitch(false)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
