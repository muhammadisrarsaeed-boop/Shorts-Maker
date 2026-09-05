package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.local.MomentCandidate
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdOnPrimaryContainer
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import com.example.ui.theme.MomentChance
import com.example.ui.theme.MomentGoal
import com.example.ui.theme.MomentMiss
import com.example.ui.theme.MomentPeak
import com.example.ui.theme.MomentSkill

@Composable
fun WaveformView(
    waveformPoints: List<Float>,
    currentSec: Float,
    durationSec: Long,
    moments: List<MomentCandidate> = emptyList(),
    modifier: Modifier = Modifier,
    onSeekTo: (Float) -> Unit = {}
) {
    val totalSec = if (durationSec <= 0) 1f else durationSec.toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .pointerInput(totalSec) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeekTo(fraction * totalSec)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            if (waveformPoints.isEmpty()) {
                // Draw subtle placeholder baseline
                drawLine(
                    color = HdCardBorder,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 2f
                )
                return@Canvas
            }

            val barCount = waveformPoints.size
            val barSpacing = width / barCount
            val barWidth = (barSpacing * 0.65f).coerceAtLeast(2f)

            // Draw audio bars
            for (i in 0 until barCount) {
                val amp = waveformPoints[i].coerceIn(0.08f, 1.0f)
                val barHeight = (height * 0.85f * amp).coerceAtLeast(4f)
                val x = i * barSpacing + (barSpacing - barWidth) / 2f
                val y = centerY - (barHeight / 2f)

                // High excitement peaks glow in Primary purple
                val isPeak = amp >= 0.70f
                val barColor = if (isPeak) {
                    if (amp >= 0.85f) HdPrimary else HdPrimary.copy(alpha = 0.8f)
                } else {
                    HdCardBorder
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            // Draw moment markers along top edge
            moments.forEach { moment ->
                val startFraction = (moment.startSec / totalSec).coerceIn(0f, 1f)
                val endFraction = (moment.endSec / totalSec).coerceIn(0f, 1f)
                val markerX = startFraction * width
                val markerWidth = ((endFraction - startFraction) * width).coerceAtLeast(8f)

                val markerColor = when (moment.momentType.lowercase()) {
                    "goal_scored" -> MomentGoal
                    "goal_missed" -> MomentMiss
                    "chance_created" -> MomentChance
                    "skill_moment" -> MomentSkill
                    else -> MomentPeak
                }

                // Highlight band on bottom
                drawRoundRect(
                    color = markerColor.copy(alpha = 0.45f),
                    topLeft = Offset(markerX, height - 6f),
                    size = Size(markerWidth, 6f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Small pin dot on top
                drawCircle(
                    color = markerColor,
                    radius = 4f,
                    center = Offset(markerX, 4f)
                )
            }

            // Draw current playback head line
            val playheadX = (currentSec / totalSec).coerceIn(0f, 1f) * width
            drawLine(
                color = HdPrimary,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, height),
                strokeWidth = 3f
            )
            drawCircle(
                color = HdPrimary,
                radius = 5f,
                center = Offset(playheadX, centerY)
            )
        }
    }
}
