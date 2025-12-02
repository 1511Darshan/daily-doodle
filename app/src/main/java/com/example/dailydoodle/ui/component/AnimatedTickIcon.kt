package com.example.dailydoodle.ui.component

import android.graphics.drawable.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.dailydoodle.R
import com.google.accompanist.drawablepainter.rememberDrawablePainter

/**
 * Displays an animated tick/checkmark icon that plays automatically.
 * Uses the ic_animated_tick.xml animated vector drawable.
 */
@Composable
fun AnimatedTickIcon(
    modifier: Modifier = Modifier,
    size: Dp = 148.dp
) {
    val context = LocalContext.current
    val drawable = remember {
        ContextCompat.getDrawable(context, R.drawable.ic_animated_tick)
    }
    
    LaunchedEffect(drawable) {
        (drawable as? Animatable)?.start()
    }
    
    Image(
        painter = rememberDrawablePainter(drawable = drawable),
        contentDescription = "Success",
        modifier = modifier.size(size)
    )
}
