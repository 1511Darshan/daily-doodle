package com.example.dailydoodle.ui.components.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dailydoodle.R
import kotlinx.coroutines.launch

/**
 * Main onboarding dialog component.
 * Displays a fullscreen dialog with a vertical pager containing
 * Welcome, Features, and Links screens.
 * 
 * @param onDismiss Callback when the dialog is dismissed
 * @param onFinish Callback when the user completes onboarding
 */
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    FullscreenPopup(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalWindowInsetsPadding()
        ) {
            // Page indicator on the left - matching Shkiper layout
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 20.dp, top = 24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerticalIndicator(pagerState = pagerState)
            }
            
            // Main content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Vertical Pager - takes most of the space
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    userScrollEnabled = true
                ) { page ->
                    when (page) {
                        0 -> WelcomeScreen()
                        1 -> FeaturesScreen()
                        2 -> LinksScreen()
                    }
                }
                
                // Full-width bottom button - matching Shkiper style
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .bottomWindowInsetsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onFinish()
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pagerState.currentPage < 2) 
                                MaterialTheme.colorScheme.surfaceContainerHighest 
                            else 
                                MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (pagerState.currentPage < 2)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .bounceClick()
                    ) {
                        Text(
                            text = if (pagerState.currentPage < 2) 
                                stringResource(R.string.onboarding_next)
                            else 
                                stringResource(R.string.onboarding_get_started),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
