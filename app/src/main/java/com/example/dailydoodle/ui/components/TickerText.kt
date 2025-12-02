package com.example.dailydoodle.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
 * Creates a smooth horizontal scrolling effect like "Create • Draw • Share • Create • Draw • Share"
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
        modifier = modifier.fillMaxWidth(),
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
 * Example: TickerText(texts = arrayOf("Create", "Draw", "Share", "Inspire"))
 */
@Composable
fun TickerText(
    texts: Array<String>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = LocalContentColor.current,
) {
    val tickerMarquee = remember(texts) {
        // Repeat the array multiple times for continuous scrolling
        val repeated = (1..5).flatMap { texts.toList() }
        repeated.joinToString(
            separator = " ${Typography.bullet} ",
            prefix = " ${Typography.bullet} ",
        )
    }
    Column(
        modifier = modifier.fillMaxWidth(),
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
