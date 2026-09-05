package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdContainerLight
import com.example.ui.theme.HdOnPrimaryContainer
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import com.example.ui.theme.HdSurface
import com.example.ui.theme.HdSurfaceVariant
import com.example.ui.theme.HdTextPrimary
import com.example.ui.theme.HdTextSecondary

/**
 * Processing type enum for visual styling of progress phases.
 */
enum class ProcessingPhaseType(
    val title: String,
    val primaryColor: Color,
    val accentColor: Color,
    val icon: ImageVector
) {
    GEMINI_ANALYSIS(
        title = "GEMINI AI ANALYSIS",
        primaryColor = Color(0xFF6750A4),
        accentColor = Color(0xFFD0BCFF),
        icon = Icons.Default.AutoAwesome
    ),
    FFMPEG_RENDERING(
        title = "FFMPEG 9:16 RENDERING",
        primaryColor = Color(0xFF006C50),
        accentColor = Color(0xFF80D7B6),
        icon = Icons.Default.Crop
    ),
    AUDIO_PEAK_SCAN(
        title = "AUDIO SPECTRUM SCAN",
        primaryColor = Color(0xFFB3261E),
        accentColor = Color(0xFFF2B8B5),
        icon = Icons.Default.GraphicEq
    ),
    BATCH_PROCESSING(
        title = "BATCH SHORTS PIPELINE",
        primaryColor = Color(0xFF0284C7),
        accentColor = Color(0xFF7DD3FC),
        icon = Icons.Default.Speed
    )
}

/**
 * High-performance, animated linear progress bar component that smoothly updates
 * in real-time during Gemini multimodal video analysis and FFmpeg 9:16 rendering.
 */
@Composable
fun ProcessingProgressBar(
    progressPercent: Int, // 0 to 100
    stageText: String,
    phaseType: ProcessingPhaseType = ProcessingPhaseType.GEMINI_ANALYSIS,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    showPercentLabel: Boolean = true,
    showShimmer: Boolean = true,
    isIndeterminate: Boolean = false,
    testTag: String = "processing_linear_progress_bar"
) {
    val clampedPercent = progressPercent.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedPercent / 100f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "LinearProgressAnimation"
    )

    // Shimmer effect across the bar
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmerTransition")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Track and fill container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(HdContainerLight)
                .border(1.dp, HdCardBorder.copy(alpha = 0.5f), RoundedCornerShape(height / 2))
        ) {
            // Active Progress Fill
            val fillFraction = if (isIndeterminate) 0.4f else animatedProgress
            val fillBrush = Brush.horizontalGradient(
                colors = listOf(
                    phaseType.primaryColor,
                    phaseType.accentColor,
                    phaseType.primaryColor
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillFraction.coerceIn(0.02f, 1f))
                    .clip(RoundedCornerShape(height / 2))
                    .background(fillBrush)
            ) {
                // Moving light sweep shimmer
                if (showShimmer && clampedPercent < 100) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    startX = shimmerOffset * 300f,
                                    endX = (shimmerOffset + 1f) * 300f
                                )
                            )
                    )
                }
            }
        }

        // Bottom status row (Stage text + Percentage)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stageText.ifBlank { "Processing stream..." },
                color = HdTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
            )

            if (showPercentLabel) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = phaseType.primaryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, phaseType.primaryColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$clampedPercent%",
                        color = phaseType.primaryColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-featured Card container displaying real-time progress for active background
 * Gemini analysis or FFmpeg video rendering pipelines.
 */
@Composable
fun ProcessingProgressCard(
    isVisible: Boolean,
    progressPercent: Int,
    stageText: String,
    phaseType: ProcessingPhaseType,
    modifier: Modifier = Modifier,
    currentStep: Int? = null,
    totalSteps: Int? = null,
    onCancel: (() -> Unit)? = null,
    testTag: String = "processing_progress_card"
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = HdSurfaceVariant),
            border = BorderStroke(1.dp, phaseType.primaryColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Phase badge & Step counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = phaseType.primaryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = phaseType.icon,
                                    contentDescription = phaseType.title,
                                    tint = phaseType.primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = phaseType.title,
                                color = phaseType.primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            if (currentStep != null && totalSteps != null) {
                                Text(
                                    text = "Phase $currentStep of $totalSteps in pipeline",
                                    color = HdTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = phaseType.primaryColor,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "${progressPercent.coerceIn(0, 100)}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Main Linear Progress Bar
                ProcessingProgressBar(
                    progressPercent = progressPercent,
                    stageText = stageText,
                    phaseType = phaseType,
                    height = 10.dp,
                    showPercentLabel = false,
                    showShimmer = progressPercent < 100
                )
            }
        }
    }
}

/**
 * Compact inline linear progress component ideal for list items, bottom sheets,
 * or modal dialogs.
 */
@Composable
fun CompactProcessingIndicator(
    progressPercent: Int,
    phaseTitle: String,
    modifier: Modifier = Modifier,
    phaseColor: Color = HdPrimary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0, 100) / 100f,
        animationSpec = tween(250),
        label = "CompactProgress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = phaseTitle,
                color = HdTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$progressPercent%",
                color = phaseColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(HdContainerLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(phaseColor)
            )
        }
    }
}
