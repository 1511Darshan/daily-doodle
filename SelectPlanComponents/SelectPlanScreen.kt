package com.example.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Import your TickerText component
// import com.example.app.ui.components.TickerText

/**
 * Select Plan Screen - similar to Kenko's home screen when no plan is selected.
 * Shows a marquee ticker, large heading text, and a call-to-action button.
 */
@Composable
fun SelectPlanScreen(
    onProfileClick: () -> Unit = {},
    onSelectPlanClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppTopBar(
                appName = "YOUR APP", // Change this
                appIconRes = android.R.drawable.ic_menu_compass, // Replace with your icon
                onProfileClick = onProfileClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
        ) {
            HorizontalDivider(thickness = 1.dp)
            
            // Scrolling marquee text
            TickerText(
                text = "Select A Plan",
                color = MaterialTheme.colorScheme.outline,
            )
            
            HorizontalDivider(thickness = 1.dp)
            
            // Main content
            SelectPlanContent(onSelectPlanClick = onSelectPlanClick)
            
            // Bottom quote text
            Text(
                text = "light weight",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun ColumnScope.SelectPlanContent(
    onSelectPlanClick: () -> Unit,
) {
    Spacer(modifier = Modifier.weight(1F))
    
    // Large heading text
    Text(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(horizontal = 16.dp),
        text = "Start by Selecting a Plan",
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 78.sp,
            lineHeight = 70.sp,
            lineBreak = LineBreak.Heading,
        ),
        color = MaterialTheme.colorScheme.primary,
    )
    
    Spacer(modifier = Modifier.weight(1F))
    
    // Select Plan button
    Button(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        onClick = onSelectPlanClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        ),
        contentPadding = PaddingValues(
            vertical = 24.dp,
            horizontal = 40.dp,
        ),
    ) {
        Text(text = "Select Plan")
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            painter = painterResource(android.R.drawable.ic_menu_send), // Replace with arrow_outward icon
            contentDescription = null,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    appName: String,
    appIconRes: Int,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(appIconRes),
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape)
                )
                Text(
                    text = appName,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        actions = {
            FilledTonalIconButton(onClick = onProfileClick) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_myplaces), // Replace with person icon
                    contentDescription = "Profile",
                )
            }
        },
        modifier = modifier,
    )
}

// Placeholder TickerText - use the actual component from TickerText.kt
@Composable
private fun TickerText(
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    // Replace this with the actual TickerText import
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        val tickerMarquee = "$text • $text • $text • $text • $text • $text • $text • $text"
        Text(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .then(
                    Modifier.basicMarquee(
                        initialDelayMillis = 0,
                        iterations = Int.MAX_VALUE,
                    )
                ),
            text = tickerMarquee,
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@Composable
private fun Modifier.basicMarquee(
    initialDelayMillis: Int,
    iterations: Int,
): Modifier = androidx.compose.foundation.basicMarquee(
    initialDelayMillis = initialDelayMillis,
    iterations = iterations,
)
