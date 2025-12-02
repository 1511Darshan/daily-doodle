package com.example.app.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * A scrolling marquee text component that repeats the text with bullet separators.
 * Used for the "Select A Plan • Select A Plan • ..." effect.
 */
@Composable
fun TickerText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = LocalContentColor.current,
) {
    val tickerMarquee = remember(text) {
        List(10) { text }
            .joinToString(
                separator = " ${Typography.bullet} ",
                prefix = " ${Typography.bullet} ",
            )
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .basicMarquee(
                    initialDelayMillis = 0,
                    iterations = Int.MAX_VALUE,
                ),
            text = tickerMarquee,
            style = style,
            color = color,
        )
    }
}

/**
 * Overload that accepts an array of texts to join together.
 */
@Composable
fun TickerText(
    texts: Array<String>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = LocalContentColor.current,
) {
    val tickerMarquee = remember(texts) {
        texts.joinToString(
            separator = " ${Typography.bullet} ",
            prefix = " ${Typography.bullet} ",
        )
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .basicMarquee(
                    initialDelayMillis = 0,
                    iterations = Int.MAX_VALUE,
                ),
            text = tickerMarquee,
            style = style,
            color = color,
        )
    }
}
