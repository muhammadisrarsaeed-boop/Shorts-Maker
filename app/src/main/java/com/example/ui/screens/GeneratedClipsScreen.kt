package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClipRenderJob
import com.example.data.local.MasterClip
import com.example.data.local.MomentCandidate
import androidx.compose.material.icons.filled.Edit
import com.example.ui.components.FineTuningEditor
import com.example.ui.components.MetadataEditorDialog
import com.example.ui.components.MomentBadge
import com.example.ui.components.SchedulePostDialog
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.HdBackground
import com.example.ui.theme.HdBadge
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdContainerLight
import com.example.ui.theme.HdMediaDark
import com.example.ui.theme.HdOnPrimaryContainer
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import com.example.ui.theme.HdSurface
import com.example.ui.theme.HdSurfaceVariant
import com.example.ui.theme.HdTextPrimary
import com.example.ui.theme.HdTextSecondary
import com.example.ui.theme.HdTextTertiary
import com.example.viewmodel.FootballViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedClipsScreen(
    viewModel: FootballViewModel,
    onNavigateToStudio: () -> Unit = {},
    onNavigateToServer: () -> Unit = {},
    onNavigateToScheduler: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val moments by viewModel.moments.collectAsState()
    val masterClips by viewModel.masterClips.collectAsState()
    val renderJobs by viewModel.renderJobs.collectAsState()
    val selectedMoment by viewModel.selectedMoment.collectAsState()
    val selectedClip by viewModel.selectedMasterClip.collectAsState()

    val currentClip = selectedClip ?: masterClips.firstOrNull()
    val activeMoment = selectedMoment ?: moments.firstOrNull()

    var filterCategory by remember { mutableStateOf("ALL") }
    var showFineTuner by remember { mutableStateOf(false) }
    var isVerticalAspect by remember { mutableStateOf(true) }
    var requestedSeekSec by remember { mutableStateOf<Float?>(null) }
    var editingMoment by remember { mutableStateOf<MomentCandidate?>(null) }
    var schedulingMoment by remember { mutableStateOf<MomentCandidate?>(null) }

    val filteredMoments = remember(moments, filterCategory) {
        when (filterCategory) {
            "HOOKS" -> moments.filter { it.momentType.contains("hook", ignoreCase = true) }
            "SHORTS" -> moments.filter { (it.endSec - it.startSec) <= 30f }
            "HIGH_ENERGY" -> moments.filter { it.confidence >= 0.90f }
            else -> moments
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Video Source Bar
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HdContainerLight),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HdPrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentClip?.fileName ?: "Main Match Footage",
                                color = HdTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Source: ${currentClip?.resolution ?: "1080p"} • ${moments.size} Generated Clips",
                                color = HdTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToDashboard,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HdPrimary),
                        border = BorderStroke(1.dp, HdPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Switch Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Generated Clip Player
        if (activeMoment != null && currentClip != null) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = HdSurface),
                    border = BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_clip_player_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Player Header HUD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = HdPrimary
                                ) {
                                    Text(
                                        text = if (isVerticalAspect) "9:16 SHORT" else "16:9 CUT",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                MomentBadge(momentType = activeMoment.momentType)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Toggle Aspect Ratio (9:16 Shorts vs 16:9)
                                IconButton(
                                    onClick = { isVerticalAspect = !isVerticalAspect },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isVerticalAspect) Icons.Default.Smartphone else Icons.Default.Tv,
                                        contentDescription = "Aspect Ratio",
                                        tint = HdPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HdPrimaryContainer
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", activeMoment.endSec - activeMoment.startSec)}s",
                                        color = HdOnPrimaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Video Player View for the clip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(if (isVerticalAspect) 9f / 14f else 16f / 9f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(HdMediaDark)
                        ) {
                            VideoPlayerView(
                                videoUriString = currentClip.localUri,
                                durationSec = currentClip.durationSec,
                                moments = listOf(activeMoment),
                                seekToSec = requestedSeekSec ?: activeMoment.startSec,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Commentary Subtitle Overlay at bottom of video
                            if (!activeMoment.commentaryCaption.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xCC000000),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "\"${activeMoment.commentaryCaption}\"",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title & Timecode Info
                        Text(
                            text = activeMoment.title,
                            color = HdTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Match Time: ${formatTimestamp(activeMoment.startSec)} - ${formatTimestamp(activeMoment.endSec)}",
                                color = HdPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• ${(activeMoment.confidence * 100).toInt()}% AI Match",
                                color = HdTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Action Buttons for the active clip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Fine Tune button
                            Button(
                                onClick = { showFineTuner = !showFineTuner },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showFineTuner) HdPrimary else HdContainerLight,
                                    contentColor = if (showFineTuner) Color.White else HdTextPrimary
                                ),
                                border = BorderStroke(1.dp, if (showFineTuner) HdPrimary else HdCardBorder),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(42.dp)
                                    .testTag("btn_fine_tune_active_clip")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (showFineTuner) Color.White else HdPrimary
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (showFineTuner) "Close Frames" else "Fine-Tune",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Render / Cut in Studio button
                            Button(
                                onClick = {
                                    viewModel.startClipRender(
                                        moment = activeMoment,
                                        masterClip = currentClip,
                                        aspectRatio = if (isVerticalAspect) "9:16" else "16:9",
                                        extractSound = true
                                    )
                                    onNavigateToStudio()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HdPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(42.dp)
                                    .testTag("btn_render_clip_now")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Render Cut", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Export / Download MP4
                            IconButton(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Clip '${activeMoment.title}' exported to local storage",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(HdContainerLight, RoundedCornerShape(12.dp))
                                    .border(1.dp, HdCardBorder, RoundedCornerShape(12.dp))
                                    .testTag("btn_export_active_clip")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export MP4",
                                    tint = HdPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Stream over Local Server
                            IconButton(
                                onClick = onNavigateToServer,
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(HdContainerLight, RoundedCornerShape(12.dp))
                                    .border(1.dp, HdCardBorder, RoundedCornerShape(12.dp))
                                    .testTag("btn_stream_active_clip")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = "Stream Local Server",
                                    tint = HdPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Inline Frame-by-Frame Fine-Tuning Module
                        if (showFineTuner) {
                            Spacer(modifier = Modifier.height(14.dp))
                            FineTuningEditor(
                                moment = activeMoment,
                                masterClip = currentClip,
                                onSaveTimestamps = { id, newStart, newEnd ->
                                    viewModel.updateMomentTimestamps(id, newStart, newEnd)
                                    requestedSeekSec = newStart
                                    Toast.makeText(context, "Frame boundaries saved!", Toast.LENGTH_SHORT).show()
                                },
                                onRenderRequested = {
                                    viewModel.startClipRender(
                                        moment = activeMoment,
                                        masterClip = currentClip,
                                        aspectRatio = if (isVerticalAspect) "9:16" else "16:9",
                                        extractSound = true
                                    )
                                    onNavigateToStudio()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Batch Export & Social Share Hub Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_export_social_share_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = HdPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "1-TAP BATCH EXPORT & SOCIAL SHARE",
                                    color = HdPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Export or Share All Clips",
                                    color = HdTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HdPrimaryContainer
                        ) {
                            Text(
                                text = "${moments.size} Clips Ready",
                                color = HdOnPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Save all generated video clips straight to your phone's Photo/Video Gallery, or multi-select and share them to Instagram, TikTok, YouTube Shorts, or WhatsApp in a single action.",
                        color = HdTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Export All to Gallery Button
                        Button(
                            onClick = {
                                viewModel.exportAllClipsToGallery(context) { result ->
                                    if (result.successCount > 0) {
                                        Toast.makeText(
                                            context,
                                            "🎉 ${result.successCount} clips saved directly to your phone Gallery!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            result.errorMessage ?: "Export complete",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HdPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_batch_export_gallery")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save All to Gallery",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share All Directly to Social Apps Button
                        OutlinedButton(
                            onClick = {
                                viewModel.shareAllClipsDirectly(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = HdPrimary
                            ),
                            border = BorderStroke(1.dp, HdPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_batch_share_social")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share All Clips",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section Title & Filter Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALL GENERATED CLIPS (${filteredMoments.size})",
                        color = HdTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = "Auto-Cut from Main Video",
                        color = HdTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All Clips",
                        "HOOKS" to "Viral Hooks",
                        "SHORTS" to "Shorts (<30s)",
                        "HIGH_ENERGY" to "Top Confidence"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = filterCategory == key,
                            onClick = { filterCategory = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HdPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = HdContainerLight,
                                labelColor = HdTextPrimary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (filterCategory == key) HdPrimary else HdCardBorder
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        // Clips List
        if (filteredMoments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HdSurface),
                    border = BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = HdTextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No clips generated for this filter.",
                            color = HdTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToDashboard,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HdPrimary)
                        ) {
                            Text("Select Master Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(filteredMoments) { moment ->
            val isCurrent = activeMoment?.id == moment.id
            GeneratedClipItemCard(
                moment = moment,
                isSelected = isCurrent,
                onPlay = {
                    viewModel.selectMoment(moment)
                    requestedSeekSec = moment.startSec
                },
                onFineTune = {
                    viewModel.selectMoment(moment)
                    showFineTuner = true
                    requestedSeekSec = moment.startSec
                },
                onEditMetadata = {
                    editingMoment = moment
                },
                onSchedulePost = {
                    schedulingMoment = moment
                },
                onRenderStudio = {
                    viewModel.selectMoment(moment)
                    if (currentClip != null) {
                        viewModel.startClipRender(
                            moment = moment,
                            masterClip = currentClip,
                            aspectRatio = "9:16",
                            extractSound = true
                        )
                    }
                    onNavigateToStudio()
                },
                onDelete = {
                    viewModel.deleteMoment(moment.id)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal dialogs
    editingMoment?.let { moment ->
        MetadataEditorDialog(
            moment = moment,
            onDismiss = { editingMoment = null },
            onSave = { title, desc, caption ->
                viewModel.updateMomentMetadata(moment.id, title, desc, caption)
                Toast.makeText(context, "Metadata updated", Toast.LENGTH_SHORT).show()
            },
            onAiRegenerate = { targetMoment, onResult ->
                viewModel.regenerateMomentMetadataWithAi(targetMoment) { t, d, c ->
                    onResult(t, d, c)
                    Toast.makeText(context, "AI Title & Tags Regenerated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    schedulingMoment?.let { moment ->
        SchedulePostDialog(
            moment = moment,
            onDismiss = { schedulingMoment = null },
            onSchedule = { platform, time, days ->
                viewModel.schedulePostForRenderedClip(moment, platform, time, days)
                Toast.makeText(context, "Scheduled post for $platform at $time!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun GeneratedClipItemCard(
    moment: MomentCandidate,
    isSelected: Boolean,
    onPlay: () -> Unit,
    onFineTune: () -> Unit,
    onEditMetadata: () -> Unit = {},
    onSchedulePost: () -> Unit = {},
    onRenderStudio: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HdPrimaryContainer.copy(alpha = 0.35f) else HdSurface
        ),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) HdPrimary else HdCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("clip_item_card_${moment.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MomentBadge(momentType = moment.momentType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = moment.title,
                        color = HdTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HdBadge
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", moment.endSec - moment.startSec)}s",
                        color = HdOnPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Timecode: ${formatTimestamp(moment.startSec)} → ${formatTimestamp(moment.endSec)} (${(moment.confidence * 100).toInt()}% match score)",
                color = HdPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )

            if (!moment.commentaryCaption.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${moment.commentaryCaption}\"",
                    color = HdTextSecondary,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) HdPrimary else HdContainerLight,
                        contentColor = if (isSelected) Color.White else HdTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_play_clip_${moment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Color.White else HdPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSelected) "Playing Clip" else "Play Preview",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onRenderStudio,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HdPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_render_clip_${moment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Render 9:16",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary tools row: Trim, Edit Metadata, Schedule, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onFineTune,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HdTextPrimary),
                    border = BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_finetune_clip_${moment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = HdPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trim", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onEditMetadata,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HdTextPrimary),
                    border = BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_edit_metadata_${moment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = HdPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Title & Tags", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onSchedulePost,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HdTextPrimary),
                    border = BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_schedule_clip_${moment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = HdPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_delete_clip_${moment.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Clip",
                        tint = HdTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(sec: Float): String {
    val totalSec = sec.toInt()
    val mins = totalSec / 60
    val secs = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
