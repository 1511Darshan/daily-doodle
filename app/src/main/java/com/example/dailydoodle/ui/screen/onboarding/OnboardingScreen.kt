package com.example.dailydoodle.ui.screen.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.util.Analytics

// Color definitions matching the design
object OnboardingColors {
    val Background = Color(0xFFF5F3ED)
    val Yellow = Color(0xFFFFEA66)
    val YellowGold = Color(0xFFFFD700)
    val Blue = Color(0xFF1E48A8)
    val DarkText = Color(0xFF1A1A1A)
    val GrayText = Color(0xFF6B7280)
    val Green = Color(0xFFA8D4A8)
    val GreenDark = Color(0xFF5B9A6F)
    val Pink = Color(0xFFFFB5D5)
    val PinkDark = Color(0xFFFF8CB8)
    val LightBlue = Color(0xFFA8C8E8)
    val LightPurple = Color(0xFFD8C8E8)
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingColors.Background)
            .systemBarsPadding()
    ) {
        // Curved Lines Background
        CurvedLinesBackground()

        // Floating Elements
        FloatingElements()

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(0.2f))

            // Central Illustration - Doodle Chain themed
            DoodleChainIllustration(
                modifier = Modifier.size(260.dp)
            )

            Spacer(modifier = Modifier.weight(0.2f))

            // Bottom Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Text Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Daily Doodle" with yellow background
                    Box(
                        modifier = Modifier
                            .background(
                                color = OnboardingColors.Yellow,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Daily Doodle",
                            color = OnboardingColors.Blue,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // "Chain"
                    Text(
                        text = "Chain",
                        color = OnboardingColors.DarkText,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = "Draw one panel. Create endless comics together.",
                        color = OnboardingColors.GrayText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Login button (outlined)
                    Button(
                        onClick = {
                            Analytics.logOnboardingSkipped()
                            onSkip()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(2.dp, OnboardingColors.Black, RoundedCornerShape(50)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnboardingColors.White
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Login",
                            color = OnboardingColors.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Sign Up button (filled)
                    Button(
                        onClick = {
                            Analytics.logOnboardingCompleted()
                            onComplete()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnboardingColors.Black
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Sign Up",
                            color = OnboardingColors.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurvedLinesBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val lineColor = OnboardingColors.LightPurple

        // Top curved lines
        val path1 = Path().apply {
            moveTo(-50f, height * 0.12f)
            quadraticBezierTo(width * 0.25f, height * 0.07f, width * 0.5f, height * 0.1f)
            quadraticBezierTo(width * 0.75f, height * 0.13f, width + 50f, height * 0.12f)
        }
        drawPath(
            path = path1,
            color = lineColor.copy(alpha = 0.6f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        val path2 = Path().apply {
            moveTo(-50f, height * 0.17f)
            quadraticBezierTo(width * 0.3f, height * 0.13f, width * 0.6f, height * 0.16f)
            quadraticBezierTo(width * 0.85f, height * 0.19f, width + 50f, height * 0.15f)
        }
        drawPath(
            path = path2,
            color = lineColor.copy(alpha = 0.5f),
            style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round)
        )

        // Middle curved lines
        val path3 = Path().apply {
            moveTo(-30f, height * 0.4f)
            quadraticBezierTo(width * 0.35f, height * 0.37f, width * 0.7f, height * 0.42f)
            quadraticBezierTo(width * 0.9f, height * 0.44f, width + 50f, height * 0.4f)
        }
        drawPath(
            path = path3,
            color = lineColor.copy(alpha = 0.55f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        val path4 = Path().apply {
            moveTo(-60f, height * 0.48f)
            quadraticBezierTo(width * 0.25f, height * 0.5f, width * 0.6f, height * 0.46f)
            quadraticBezierTo(width * 0.85f, height * 0.43f, width + 50f, height * 0.5f)
        }
        drawPath(
            path = path4,
            color = lineColor.copy(alpha = 0.45f),
            style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round)
        )

        // Bottom curved lines
        val path5 = Path().apply {
            moveTo(-40f, height * 0.7f)
            quadraticBezierTo(width * 0.3f, height * 0.67f, width * 0.65f, height * 0.72f)
            quadraticBezierTo(width * 0.9f, height * 0.75f, width + 50f, height * 0.7f)
        }
        drawPath(
            path = path5,
            color = lineColor.copy(alpha = 0.5f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        val path6 = Path().apply {
            moveTo(-70f, height * 0.78f)
            quadraticBezierTo(width * 0.28f, height * 0.8f, width * 0.62f, height * 0.76f)
            quadraticBezierTo(width * 0.88f, height * 0.73f, width + 50f, height * 0.8f)
        }
        drawPath(
            path = path6,
            color = lineColor.copy(alpha = 0.4f),
            style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FloatingElements() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Green rounded square with pencil icon - top left
        Box(
            modifier = Modifier
                .padding(start = 48.dp, top = 112.dp)
                .size(56.dp)
                .rotate(6f)
                .background(OnboardingColors.Green, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✏️",
                fontSize = 24.sp
            )
        }

        // Pink paper plane - top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 32.dp, top = 96.dp)
        ) {
            PaperPlane()
        }

        // Pink square with palette - left side middle
        Box(
            modifier = Modifier
                .padding(start = 40.dp, top = 440.dp)
                .size(48.dp)
                .rotate(-12f)
                .background(OnboardingColors.Pink, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎨",
                fontSize = 20.sp
            )
        }

        // Blue square with chain link - right side middle-lower
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 48.dp, top = 380.dp)
                .size(48.dp)
                .rotate(12f)
                .background(OnboardingColors.LightBlue, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔗",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun PaperPlane(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(60.dp)
            .rotate(12f)
    ) {
        val planeColor = OnboardingColors.Pink
        val strokeColor = OnboardingColors.PinkDark

        // Paper plane body
        val path = Path().apply {
            moveTo(10f, 30f)
            lineTo(48f, 12f)
            lineTo(35f, 48f)
            lineTo(24f, 32f)
            close()
        }

        drawPath(
            path = path,
            color = planeColor.copy(alpha = 0.9f)
        )
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
        )

        // Inner fold line
        drawLine(
            color = strokeColor,
            start = Offset(24f, 32f),
            end = Offset(35f, 20f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Sketch texture lines
        drawLine(
            color = strokeColor.copy(alpha = 0.4f),
            start = Offset(18f, 28f),
            end = Offset(28f, 38f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = strokeColor.copy(alpha = 0.4f),
            start = Offset(30f, 24f),
            end = Offset(38f, 36f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun DoodleChainIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        val blueStroke = OnboardingColors.Blue
        val yellowGold = OnboardingColors.YellowGold
        val greenStem = OnboardingColors.GreenDark

        // Draw chain links connecting panels
        val chainColor = OnboardingColors.Blue.copy(alpha = 0.3f)
        
        // Left chain link
        drawCircle(
            color = chainColor,
            radius = 15.dp.toPx(),
            center = Offset(centerX - 80, centerY),
            style = Stroke(width = 4.dp.toPx())
        )
        
        // Right chain link
        drawCircle(
            color = chainColor,
            radius = 15.dp.toPx(),
            center = Offset(centerX + 80, centerY),
            style = Stroke(width = 4.dp.toPx())
        )

        // Draw three comic panels in a row
        val panelWidth = 50.dp.toPx()
        val panelHeight = 60.dp.toPx()
        val panelSpacing = 20.dp.toPx()

        // Panel 1 (left)
        val panel1X = centerX - panelWidth - panelSpacing
        drawRoundRect(
            color = OnboardingColors.Yellow.copy(alpha = 0.8f),
            topLeft = Offset(panel1X - panelWidth/2, centerY - panelHeight/2),
            size = Size(panelWidth, panelHeight),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawRoundRect(
            color = blueStroke,
            topLeft = Offset(panel1X - panelWidth/2, centerY - panelHeight/2),
            size = Size(panelWidth, panelHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        // Doodle in panel 1 - simple star
        drawLine(
            color = blueStroke,
            start = Offset(panel1X - 10, centerY - 15),
            end = Offset(panel1X + 10, centerY + 5),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = blueStroke,
            start = Offset(panel1X + 10, centerY - 15),
            end = Offset(panel1X - 10, centerY + 5),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Panel 2 (center) - main panel, larger
        val panel2Width = panelWidth * 1.3f
        val panel2Height = panelHeight * 1.3f
        drawRoundRect(
            color = OnboardingColors.Pink.copy(alpha = 0.8f),
            topLeft = Offset(centerX - panel2Width/2, centerY - panel2Height/2),
            size = Size(panel2Width, panel2Height),
            cornerRadius = CornerRadius(10.dp.toPx())
        )
        drawRoundRect(
            color = blueStroke,
            topLeft = Offset(centerX - panel2Width/2, centerY - panel2Height/2),
            size = Size(panel2Width, panel2Height),
            cornerRadius = CornerRadius(10.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
        // Doodle in panel 2 - heart
        val heartPath = Path().apply {
            moveTo(centerX, centerY + 15)
            quadraticBezierTo(centerX - 20, centerY - 5, centerX, centerY - 15)
            quadraticBezierTo(centerX + 20, centerY - 5, centerX, centerY + 15)
        }
        drawPath(
            path = heartPath,
            color = yellowGold,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Panel 3 (right)
        val panel3X = centerX + panelWidth + panelSpacing
        drawRoundRect(
            color = OnboardingColors.Green.copy(alpha = 0.8f),
            topLeft = Offset(panel3X - panelWidth/2, centerY - panelHeight/2),
            size = Size(panelWidth, panelHeight),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawRoundRect(
            color = blueStroke,
            topLeft = Offset(panel3X - panelWidth/2, centerY - panelHeight/2),
            size = Size(panelWidth, panelHeight),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        // Doodle in panel 3 - smiley face
        drawCircle(
            color = blueStroke,
            radius = 12.dp.toPx(),
            center = Offset(panel3X, centerY - 5),
            style = Stroke(width = 3.dp.toPx())
        )
        // Eyes
        drawCircle(color = blueStroke, radius = 2.dp.toPx(), center = Offset(panel3X - 5, centerY - 8))
        drawCircle(color = blueStroke, radius = 2.dp.toPx(), center = Offset(panel3X + 5, centerY - 8))
        // Smile
        val smilePath = Path().apply {
            moveTo(panel3X - 5, centerY)
            quadraticBezierTo(panel3X, centerY + 5, panel3X + 5, centerY)
        }
        drawPath(
            path = smilePath,
            color = blueStroke,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Connection arrows between panels
        val arrowColor = OnboardingColors.Blue.copy(alpha = 0.6f)
        // Arrow 1 -> 2
        drawLine(
            color = arrowColor,
            start = Offset(panel1X + panelWidth/2 + 5, centerY),
            end = Offset(centerX - panel2Width/2 - 5, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Arrow 2 -> 3
        drawLine(
            color = arrowColor,
            start = Offset(centerX + panel2Width/2 + 5, centerY),
            end = Offset(panel3X - panelWidth/2 - 5, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Plus sign indicating "add your panel"
        drawLine(
            color = greenStem,
            start = Offset(panel3X + panelWidth/2 + 20, centerY - 10),
            end = Offset(panel3X + panelWidth/2 + 20, centerY + 10),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = greenStem,
            start = Offset(panel3X + panelWidth/2 + 10, centerY),
            end = Offset(panel3X + panelWidth/2 + 30, centerY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen()
}
