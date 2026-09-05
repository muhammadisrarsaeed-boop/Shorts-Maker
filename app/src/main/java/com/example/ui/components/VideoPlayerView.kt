package com.example.ui.components

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.MomentCandidate
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdMediaDark
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.math.sin

/**
 * Validates whether the given URI or path points to a real, playable media file.
 */
private fun checkIsPlayableVideo(context: Context, uriString: String?): Boolean {
    if (uriString.isNullOrBlank()) return false
    return try {
        if (uriString.startsWith("/")) {
            val file = File(uriString)
            if (!file.exists() || file.length() < 1000) return false
            return true
        }

        val retriever = MediaMetadataRetriever()
        try {
            if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                retriever.setDataSource(context, Uri.parse(uriString))
            } else {
                retriever.setDataSource(uriString)
            }
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            hasVideo != null || (duration != null && (duration.toLongOrNull() ?: 0L) > 0L)
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    } catch (_: Exception) {
        true
    }
}

@Composable
fun VideoPlayerView(
    videoUriString: String,
    durationSec: Long,
    moments: List<MomentCandidate> = emptyList(),
    seekToSec: Float? = null,
    modifier: Modifier = Modifier,
    onPositionChanged: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionSec by remember { mutableFloatStateOf(0f) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isNativeReady by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(true) }

    val isPlayable = remember(videoUriString) {
        checkIsPlayableVideo(context, videoUriString)
    }
    var useNativePlayer by remember(isPlayable) { mutableStateOf(isPlayable) }

    val totalSec = if (durationSec <= 0) 1L else durationSec

    // Identify if the playhead is currently within an identified viral moment
    val activeMoment = remember(currentPositionSec, moments) {
        moments.firstOrNull { currentPositionSec >= it.startSec && currentPositionSec <= it.endSec }
    }

    // React to external seek requests
    LaunchedEffect(seekToSec) {
        seekToSec?.let { target ->
            val clamped = target.coerceIn(0f, totalSec.toFloat())
            currentPositionSec = clamped
            if (useNativePlayer && isNativeReady) {
                try {
                    videoViewRef?.seekTo((clamped * 1000).toInt())
                } catch (_: Exception) {}
            }
            onPositionChanged(clamped)
        }
    }

    // Playback loop for native VideoView
    LaunchedEffect(isPlaying, useNativePlayer, isNativeReady) {
        if (useNativePlayer && isNativeReady) {
            while (isPlaying) {
                videoViewRef?.let { vv ->
                    if (vv.isPlaying) {
                        val pos = vv.currentPosition / 1000f
                        currentPositionSec = pos
                        onPositionChanged(pos)
                    }
                }
                delay(200)
            }
        }
    }

    // Playback loop for preview canvas mode
    LaunchedEffect(isPlaying, useNativePlayer) {
        if (!useNativePlayer) {
            while (isPlaying) {
                delay(200)
                val next = currentPositionSec + 0.2f
                if (next >= totalSec) {
                    if (isLooping) {
                        currentPositionSec = 0f
                        onPositionChanged(0f)
                    } else {
                        isPlaying = false
                    }
                } else {
                    currentPositionSec = next
                    onPositionChanged(next)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HdMediaDark)
            .aspectRatio(16f / 9f)
    ) {
        // Render native VideoView when the media file is verified playable
        if (useNativePlayer) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setOnPreparedListener { mp ->
                            isNativeReady = true
                            mp.isLooping = isLooping
                            if (isPlaying) {
                                try { start() } catch (_: Exception) {}
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            useNativePlayer = false
                            true
                        }
                        try {
                            if (videoUriString.startsWith("content://") || videoUriString.startsWith("file://")) {
                                setVideoURI(Uri.parse(videoUriString))
                            } else if (videoUriString.startsWith("/")) {
                                setVideoPath(videoUriString)
                            }
                        } catch (_: Exception) {
                            useNativePlayer = false
                        }
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                }
            )
        }

        // Fallback Studio Canvas when native player is disabled
        if (!useNativePlayer) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Sleek Studio Background with dynamic waveform spectrum
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Deep Cinematic Gradient
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2C243B), Color(0xFF141218), Color(0xFF0D0B10)),
                            center = Offset(w / 2, h / 2),
                            radius = w * 0.7f
                        ),
                        size = size
                    )

                    // Audio energy frequency spectrum bars across bottom
                    val barCount = 48
                    val barWidth = (w / barCount) * 0.65f
                    val gap = (w / barCount) * 0.35f
                    val progressRatio = (currentPositionSec / totalSec.toFloat()).coerceIn(0f, 1f)

                    for (i in 0 until barCount) {
                        val barRatio = i / barCount.toFloat()
                        val isPassed = barRatio <= progressRatio
                        val wave = (0.5f + 0.5f * sin((barRatio * 20f + currentPositionSec * 3f))).coerceIn(0.15f, 1f)
                        val barHeight = h * 0.35f * wave

                        val barColor = if (isPassed) {
                            Color(0xFFD0BCFF).copy(alpha = 0.85f)
                        } else {
                            Color(0xFF49454F).copy(alpha = 0.40f)
                        }

                        val x = i * (barWidth + gap) + gap / 2
                        val y = h - 60f - barHeight

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }

                // Top Video HUD Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color(0xFF4CAF50) else Color(0xFFFF9800))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "PLAYING" else "PAUSED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI SHORTS CLIPPER",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Center Viral Hook Banner / Live Subtitle Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (activeMoment != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xEE2A1D3C),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, HdPrimary),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MomentBadge(momentType = activeMoment.momentType)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Viral Score: ${(activeMoment.confidence * 100).toInt()}%",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = activeMoment.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        if (activeMoment.commentaryCaption.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xDD000000),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "“${activeMoment.commentaryCaption}”",
                                    color = Color(0xFFFFEB3B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 2
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "SOURCE VIDEO TIMELINE",
                            color = Color(0xFFCAC4D0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scrub timeline to preview AI detected viral hooks & shorts",
                            color = Color(0xFF938F99),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom Controls Gradient Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xDD141218), Color(0xFF141218))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Column {
                // Timecode & Moments Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatTime(currentPositionSec.toLong())} / ${formatTime(totalSec)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    if (moments.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HdPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${moments.size} Clips Generated",
                                color = Color(0xFF21005D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Scrub Slider with Moment Markers
                Slider(
                    value = currentPositionSec.coerceIn(0f, totalSec.toFloat()),
                    onValueChange = { newPos ->
                        currentPositionSec = newPos
                        if (useNativePlayer && isNativeReady) {
                            try {
                                videoViewRef?.seekTo((newPos * 1000).toInt())
                            } catch (_: Exception) {}
                        }
                        onPositionChanged(newPos)
                    },
                    valueRange = 0f..totalSec.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = HdPrimary,
                        activeTrackColor = HdPrimary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("video_scrub_slider")
                )

                // Playback Control Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 5s
                        IconButton(
                            onClick = {
                                val target = (currentPositionSec - 5f).coerceAtLeast(0f)
                                currentPositionSec = target
                                if (useNativePlayer && isNativeReady) {
                                    try {
                                        videoViewRef?.seekTo((target * 1000).toInt())
                                    } catch (_: Exception) {}
                                }
                                onPositionChanged(target)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 5s",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Play/Pause Main Button
                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                if (useNativePlayer && isNativeReady) {
                                    if (isPlaying) videoViewRef?.start() else videoViewRef?.pause()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HdPrimary)
                                .testTag("btn_play_pause")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Fast Forward 5s
                        IconButton(
                            onClick = {
                                val target = (currentPositionSec + 5f).coerceAtMost(totalSec.toFloat())
                                currentPositionSec = target
                                if (useNativePlayer && isNativeReady) {
                                    try {
                                        videoViewRef?.seekTo((target * 1000).toInt())
                                    } catch (_: Exception) {}
                                }
                                onPositionChanged(target)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 5s",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Next/Prev Moment Jump Buttons
                    if (moments.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prev Moment
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    val prev = moments.filter { it.startSec < currentPositionSec - 0.5f }
                                        .maxByOrNull { it.startSec } ?: moments.lastOrNull()
                                    prev?.let {
                                        currentPositionSec = it.startSec
                                        if (useNativePlayer && isNativeReady) {
                                            try { videoViewRef?.seekTo((it.startSec * 1000).toInt()) } catch (_: Exception) {}
                                        }
                                        onPositionChanged(it.startSec)
                                    }
                                }
                            ) {
                                Text(
                                    text = "◀ Prev",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }

                            // Next Moment
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    val next = moments.filter { it.startSec > currentPositionSec + 0.5f }
                                        .minByOrNull { it.startSec } ?: moments.firstOrNull()
                                    next?.let {
                                        currentPositionSec = it.startSec
                                        if (useNativePlayer && isNativeReady) {
                                            try { videoViewRef?.seekTo((it.startSec * 1000).toInt()) } catch (_: Exception) {}
                                        }
                                        onPositionChanged(it.startSec)
                                    }
                                }
                            ) {
                                Text(
                                    text = "Next ▶",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(sec: Long): String {
    val m = sec / 60
    val s = sec % 60
    return String.format(Locale.US, "%02d:%02d", m, s)
}
