package com.yourapp.animations.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Shared element transition utilities for tab switching and other
 * scenarios where elements animate between states.
 */

/**
 * Animation spec for shared bounds transformation
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val DefaultBoundTransformSpec: FiniteAnimationSpec<Rect> = spring()

/**
 * Animation spec for content fade
 */
val DefaultContentAnimationSpec: FiniteAnimationSpec<Float> = spring()

/**
 * A tab title with shared element animation support.
 * Use this in combination with SharedTransitionLayout for smooth tab switching.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AnimatedTabTitle(
    title: String,
    tabKey: Any,
    style: TextStyle,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundTransformAnimationSpec: FiniteAnimationSpec<Rect> = DefaultBoundTransformSpec,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
    ) {
        Text(
            text = title,
            style = style,
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(tabKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        boundTransformAnimationSpec
                    }
                )
        )

        Box(
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("indicator"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    boundsTransform = { _, _ ->
                        boundTransformAnimationSpec
                    }
                )
                .fillMaxWidth()
                .height(2.dp)
                .clip(ShapeDefaults.ExtraLarge)
                .background(color = indicatorColor)
        )
    }
}

/**
 * A tab row title item with shared element animation support.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AnimatedTabRowTitle(
    title: String,
    tabKey: Any,
    isSelected: Boolean,
    style: TextStyle,
    onClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundTransformAnimationSpec: FiniteAnimationSpec<Rect> = DefaultBoundTransformSpec,
    selectedIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .heightIn(min = 60.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        key("$isSelected-$tabKey") {
            Text(
                text = title,
                style = style,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(tabKey),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            boundTransformAnimationSpec
                        }
                    )
            )

            val color = remember(isSelected) {
                mutableStateOf(
                    if (isSelected) selectedIndicatorColor else Color.Transparent
                )
            }
            
            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            if (color.value != Color.Transparent) {
                                "indicator"
                            } else {
                                "invisible-indicator"
                            }
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        boundsTransform = { _, _ ->
                            boundTransformAnimationSpec
                        }
                    )
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(ShapeDefaults.ExtraLarge)
                    .background(color = color.value)
            )
        }
    }
}

/**
 * Scale + fade transition spec for AnimatedContent
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun scaleFadeTransition(
    contentAnimationSpec: FiniteAnimationSpec<Float> = DefaultContentAnimationSpec
) = scaleIn(contentAnimationSpec, initialScale = 1.5f) +
        fadeIn(contentAnimationSpec) togetherWith
        scaleOut(contentAnimationSpec, targetScale = 1.5f) +
        fadeOut(contentAnimationSpec)
