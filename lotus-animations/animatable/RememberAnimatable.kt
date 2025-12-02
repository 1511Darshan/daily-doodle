package com.yourapp.animations.animatable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * A saveable version of Animatable that preserves animation state across configuration changes.
 * Use this when you need an Animatable that survives screen rotation or process death.
 */
@Composable
fun rememberAnimatable(
    initialValue: Float,
    visibilityThreshold: Float = Spring.DefaultDisplacementThreshold
): Animatable<Float, AnimationVector1D> {
    return rememberSaveable(
        saver = AnimatableSaver
    ) {
        Animatable(initialValue, visibilityThreshold)
    }
}

object AnimatableSaver : Saver<Animatable<Float, AnimationVector1D>, Float> {
    override fun restore(value: Float): Animatable<Float, AnimationVector1D>? {
        return Animatable(value)
    }

    override fun SaverScope.save(value: Animatable<Float, AnimationVector1D>): Float? {
        return value.value
    }
}
