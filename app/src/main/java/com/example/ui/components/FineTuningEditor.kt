package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MasterClip
import com.example.data.local.MomentCandidate
import com.example.ui.theme.HdBackground
import com.example.ui.theme.HdBadge
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdContainerLight
import com.example.ui.theme.HdOnPrimaryContainer
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import com.example.ui.theme.HdSurface
import com.example.ui.theme.HdTextPrimary
import com.example.ui.theme.HdTextSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Formats seconds into MM:SS:FF timecode representation.
 */
fun formatTimecode(seconds: Float, fps: Int): String {
    val totalSec = seconds.coerceAtLeast(0f)
    val mins = (totalSec / 60).toInt()
    val secs = (totalSec % 60).toInt()
    val subSec = totalSec - (mins * 60 + secs)
    val frame = (subSec * fps).roundToInt().coerceIn(0, (fps - 1).coerceAtLeast(1))
    return String.format(Locale.US, "%02d:%02d:%02d", mins, secs, frame)
}

/**
 * Professional Frame-by-Frame Fine-Tuning & In/Out Point Trimmer
 */
@Composable
fun FineTuningEditor(
    moment: MomentCandidate,
    masterClip: MasterClip?,
    onSaveTimestamps: (momentId: Long, newStartSec: Float, newEndSec: Float) -> Unit,
    onRenderRequested: ((MomentCandidate) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipFps = remember(masterClip?.fps) { masterClip?.fps ?: 30 }
    val maxDuration = remember(masterClip?.durationSec) { (masterClip?.durationSec ?: 5400L).toFloat() }

    var startSec by remember(moment.id, moment.startSec) { mutableFloatStateOf(moment.startSec) }
    var endSec by remember(moment.id, moment.endSec) { mutableFloatStateOf(moment.endSec) }
    var selectedFps by remember(clipFps) { mutableIntStateOf(if (clipFps in listOf(24, 30, 60)) clipFps else 30) }

    val initialStartSec = remember(moment.id) { moment.startSec }
    val initialEndSec = remember(moment.id) { moment.endSec }

    var isLooping by remember { mutableStateOf(false) }
    var previewCurrentSec by remember { mutableFloatStateOf(startSec) }
    var hasSavedFeedback by remember { mutableStateOf(false) }

    val currentDuration = (endSec - startSec).coerceAtLeast(0.1f)
    val totalFrames = (currentDuration * selectedFps).roundToInt()
    val frameDurationSec = 1f / selectedFps

    // Auto-loop preview playback simulation
    LaunchedEffect(isLooping, startSec, endSec) {
        if (isLooping) {
            previewCurrentSec = startSec
            while (isLooping) {
                delay(33) // ~30fps frame tick
                previewCurrentSec += 0.033f
                if (previewCurrentSec >= endSec) {
                    previewCurrentSec = startSec
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = BorderStroke(1.dp, HdCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("fine_tuning_editor_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & FPS Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = HdPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = HdPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Frame-by-Frame Fine-Tuner",
                            color = HdTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nudge In/Out boundaries with sub-frame accuracy",
                            color = HdTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // FPS Selector Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(30, 60, 24).forEach { fps ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedFps == fps) HdPrimary else HdBackground,
                            border = BorderStroke(
                                1.dp,
                                if (selectedFps == fps) HdPrimary else HdCardBorder
                            ),
                            modifier = Modifier
                                .clickable { selectedFps = fps }
                                .testTag("btn_fps_$fps")
                        ) {
                            Text(
                                text = "${fps}fps",
                                color = if (selectedFps == fps) Color.White else HdTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = HdCardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Live Timecode Display Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = HdBackground,
                border = BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Timecode
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "IN-POINT (START)",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatTimecode(startSec, selectedFps),
                            color = HdTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", startSec)}s",
                            color = HdTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Total Duration Pill
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HdPrimaryContainer,
                            border = BorderStroke(1.dp, HdPrimary.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.2fs", currentDuration),
                                    color = HdPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "$totalFrames frames",
                                    color = HdOnPrimaryContainer,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // End Timecode
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "OUT-POINT (END)",
                            color = Color(0xFFEF4444),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatTimecode(endSec, selectedFps),
                            color = HdTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", endSec)}s",
                            color = HdTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Coarse Range Slider for instant wide-boundary trimming
            Text(
                text = "TIMELINE BOUNDARY TRIMMER",
                color = HdTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            val sliderMin = (startSec - 30f).coerceAtLeast(0f)
            val sliderMax = (endSec + 30f).coerceAtMost(maxDuration)

            RangeSlider(
                value = startSec..endSec,
                onValueChange = { range ->
                    val newStart = range.start
                    val newEnd = range.endInclusive
                    if (newEnd - newStart >= 1.0f) { // Min 1s clip
                        startSec = newStart
                        endSec = newEnd
                        hasSavedFeedback = false
                    }
                },
                valueRange = sliderMin..sliderMax,
                colors = SliderDefaults.colors(
                    thumbColor = HdPrimary,
                    activeTrackColor = HdPrimary,
                    inactiveTrackColor = HdCardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("slider_fine_tuning_range")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ================= IN-POINT (START) FRAME STEPPING =================
            Text(
                text = "ADJUST IN-POINT (START)",
                color = Color(0xFF10B981),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // -1s
                FineTuneStepButton(
                    label = "-1s",
                    subLabel = "-${selectedFps}f",
                    onClick = {
                        startSec = (startSec - 1f).coerceAtLeast(0f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_start_minus_1s")
                )
                // -5 frames
                FineTuneStepButton(
                    label = "-5f",
                    subLabel = String.format(Locale.US, "-%.0fms", 5 * frameDurationSec * 1000),
                    onClick = {
                        startSec = (startSec - 5 * frameDurationSec).coerceAtLeast(0f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_start_minus_5f")
                )
                // -1 frame
                FineTuneStepButton(
                    label = "-1f",
                    subLabel = String.format(Locale.US, "-%.0fms", frameDurationSec * 1000),
                    isPrimary = true,
                    onClick = {
                        startSec = (startSec - frameDurationSec).coerceAtLeast(0f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1.1f).testTag("btn_step_start_minus_1f")
                )
                // +1 frame
                FineTuneStepButton(
                    label = "+1f",
                    subLabel = String.format(Locale.US, "+%.0fms", frameDurationSec * 1000),
                    isPrimary = true,
                    onClick = {
                        startSec = (startSec + frameDurationSec).coerceAtMost(endSec - 0.5f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1.1f).testTag("btn_step_start_plus_1f")
                )
                // +5 frames
                FineTuneStepButton(
                    label = "+5f",
                    subLabel = String.format(Locale.US, "+%.0fms", 5 * frameDurationSec * 1000),
                    onClick = {
                        startSec = (startSec + 5 * frameDurationSec).coerceAtMost(endSec - 0.5f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_start_plus_5f")
                )
                // +1s
                FineTuneStepButton(
                    label = "+1s",
                    subLabel = "+${selectedFps}f",
                    onClick = {
                        startSec = (startSec + 1f).coerceAtMost(endSec - 1f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_start_plus_1s")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= OUT-POINT (END) FRAME STEPPING =================
            Text(
                text = "ADJUST OUT-POINT (END)",
                color = Color(0xFFEF4444),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // -1s
                FineTuneStepButton(
                    label = "-1s",
                    subLabel = "-${selectedFps}f",
                    onClick = {
                        endSec = (endSec - 1f).coerceAtLeast(startSec + 1f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_end_minus_1s")
                )
                // -5 frames
                FineTuneStepButton(
                    label = "-5f",
                    subLabel = String.format(Locale.US, "-%.0fms", 5 * frameDurationSec * 1000),
                    onClick = {
                        endSec = (endSec - 5 * frameDurationSec).coerceAtLeast(startSec + 0.5f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_end_minus_5f")
                )
                // -1 frame
                FineTuneStepButton(
                    label = "-1f",
                    subLabel = String.format(Locale.US, "-%.0fms", frameDurationSec * 1000),
                    isPrimary = true,
                    onClick = {
                        endSec = (endSec - frameDurationSec).coerceAtLeast(startSec + 0.5f)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1.1f).testTag("btn_step_end_minus_1f")
                )
                // +1 frame
                FineTuneStepButton(
                    label = "+1f",
                    subLabel = String.format(Locale.US, "+%.0fms", frameDurationSec * 1000),
                    isPrimary = true,
                    onClick = {
                        endSec = (endSec + frameDurationSec).coerceAtMost(maxDuration)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1.1f).testTag("btn_step_end_plus_1f")
                )
                // +5 frames
                FineTuneStepButton(
                    label = "+5f",
                    subLabel = String.format(Locale.US, "+%.0fms", 5 * frameDurationSec * 1000),
                    onClick = {
                        endSec = (endSec + 5 * frameDurationSec).coerceAtMost(maxDuration)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_end_plus_5f")
                )
                // +1s
                FineTuneStepButton(
                    label = "+1s",
                    subLabel = "+${selectedFps}f",
                    onClick = {
                        endSec = (endSec + 1f).coerceAtMost(maxDuration)
                        hasSavedFeedback = false
                    },
                    modifier = Modifier.weight(1f).testTag("btn_step_end_plus_1s")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Confirmation Banner
            AnimatedVisibility(visible = hasSavedFeedback) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF064E3B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Timestamps saved successfully to database!",
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action Buttons Row: Preview Loop, Save, and Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Loop Playback Preview Button
                OutlinedButton(
                    onClick = { isLooping = !isLooping },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isLooping) HdPrimary.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isLooping) HdPrimary else HdTextPrimary
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isLooping) HdPrimary else HdCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_fine_tuning_loop_preview")
                ) {
                    Icon(
                        imageVector = if (isLooping) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLooping) "Pause Loop" else "Play Loop",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reset to AI Detection
                if (startSec != initialStartSec || endSec != initialEndSec) {
                    IconButton(
                        onClick = {
                            startSec = initialStartSec
                            endSec = initialEndSec
                            hasSavedFeedback = false
                        },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset to AI Detection",
                            tint = HdTextSecondary
                        )
                    }
                }

                // Save Timestamps Button
                Button(
                    onClick = {
                        onSaveTimestamps(moment.id, startSec, endSec)
                        hasSavedFeedback = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HdPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("btn_save_fine_tuned_timestamps")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Timestamps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FineTuneStepButton(
    label: String,
    subLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isPrimary) HdPrimary.copy(alpha = 0.18f) else HdBackground,
        border = BorderStroke(
            1.dp,
            if (isPrimary) HdPrimary.copy(alpha = 0.6f) else HdCardBorder
        ),
        modifier = modifier
            .clickable { onClick() }
            .height(46.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = if (isPrimary) HdPrimary else HdTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subLabel,
                color = HdTextSecondary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
