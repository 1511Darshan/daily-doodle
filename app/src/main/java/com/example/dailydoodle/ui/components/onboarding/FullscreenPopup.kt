package com.example.dailydoodle.ui.components.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

/**
 * A fullscreen popup dialog that can display content over the entire screen.
 * 
 * @param onDismissRequest Callback when the user attempts to dismiss the dialog
 * @param backgroundColor Background color of the dialog content area
 * @param content The content to display inside the fullscreen popup
 */
@Composable
fun FullscreenPopup(
    onDismissRequest: () -> Unit = {},
    backgroundColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
        )
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

        val onDismiss by rememberUpdatedState(onDismissRequest)

        DisposableEffect(Unit) {
            onDispose {
                onDismiss()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
        ) {
            content()
        }
    }
}
