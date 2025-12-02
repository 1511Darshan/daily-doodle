package com.example.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Extension function to create a large header text style.
 * Used for the "Start by Selecting a Plan" large text.
 * 
 * The original Kenko app uses:
 * - DarkerGrotesque font (bold)
 * - 78sp font size
 * - 70sp line height
 */
fun Typography.header(): TextStyle = displayLarge.copy(
    fontSize = 78.sp,
    lineHeight = 70.sp,
)

/**
 * Example custom fonts setup (similar to Kenko)
 * 
 * To use custom fonts like Kenko:
 * 1. Download DarkerGrotesque and SpaceMono fonts
 * 2. Place them in res/font/
 * 3. Uncomment and use the font families below
 */

// val displayFont = FontFamily(
//     Font(R.font.darkergrotesque_bold, weight = FontWeight.Bold),
//     Font(R.font.darkergrotesque_semibold, weight = FontWeight.SemiBold),
// )
// 
// val bodyFont = FontFamily(
//     Font(R.font.spacemono_bold, weight = FontWeight.Bold),
//     Font(R.font.spacemono_normal, weight = FontWeight.Normal),
// )

/**
 * Custom Typography setup with display fonts
 * 
 * val AppTypography = Typography().copy(
 *     displayLarge = Typography().displayLarge.copy(
 *         fontFamily = displayFont,
 *         fontWeight = FontWeight.Bold,
 *     ),
 *     // ... other styles
 * )
 */
