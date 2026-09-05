package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Tune
import com.example.data.local.ClipRenderJob
import com.example.data.local.MasterClip
import com.example.data.local.MomentCandidate
import com.example.ui.components.FineTuningEditor
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import com.example.ui.components.MomentBadge
import com.example.ui.components.SchedulePostDialog
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.HdBackground
import com.example.ui.components.ProcessingPhaseType
import com.example.ui.components.ProcessingProgressBar
import com.example.ui.components.ProcessingProgressCard
import com.example.ui.theme.HdBadge
import com.example.ui.theme.HdCardBorder
import com.example.ui.theme.HdContainerLight
import com.example.ui.theme.HdOnPrimaryContainer
import com.example.ui.theme.HdPrimary
import com.example.ui.theme.HdPrimaryContainer
import com.example.ui.theme.HdSurface
import com.example.ui.theme.HdSurfaceVariant
import com.example.ui.theme.HdTextPrimary
import com.example.ui.theme.HdTextSecondary
import com.example.ui.theme.HdTextTertiary
import com.example.viewmodel.FootballViewModel

@Composable
fun ClipStudioScreen(
    viewModel: FootballViewModel,
    onNavigateToServer: () -> Unit = {},
    onNavigateToClips: () -> Unit = {}
) {
    val context = LocalContext.current
    val renderJobs by viewModel.renderJobs.collectAsState()
    val moments by viewModel.moments.collectAsState()
    val masterClips by viewModel.masterClips.collectAsState()
    val selectedMoment by viewModel.selectedMoment.collectAsState()
    val selectedClip by viewModel.selectedMasterClip.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val currentClip = selectedClip ?: masterClips.firstOrNull()
    val currentMoment = selectedMoment ?: moments.firstOrNull()

    var targetAspect by remember { mutableStateOf("9:16") }
    var backgroundMode by remember(targetAspect, settings) {
        mutableStateOf(if (targetAspect == "9:16") settings.backgroundMode916 else settings.backgroundModeOther)
    }
    var watermarkText by remember(settings) { mutableStateOf(settings.defaultWatermark) }
    var extractSound by remember { mutableStateOf(true) }
    var showFfmpegInspector by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick workflow banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HdContainerLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STEP 3 OF 4: RENDER & EXPORT",
                            color = HdPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (renderJobs.isNotEmpty()) "${renderJobs.size} clips ready • Stream on local server" else "Pick moment & render 9:16 cuts",
                            color = HdTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    OutlinedButton(
                        onClick = onNavigateToServer,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HdPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HdPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Server", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ON-DEVICE CLIP RENDER STUDIO",
                color = HdPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Fine-Tune & Render Shorts",
                color = HdTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Adjust in/out boundaries frame-by-frame and render 9:16 vertical MP4 cuts",
                color = HdTextSecondary,
                fontSize = 12.sp
            )
        }

        // Moment Selection Strip
        if (moments.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT MOMENT (${moments.size})",
                            color = HdPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (currentMoment != null) {
                            Text(
                                text = "Active: ${currentMoment.title}",
                                color = HdTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moments.forEach { moment ->
                            val isSelected = currentMoment?.id == moment.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) HdPrimaryContainer else HdSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) HdPrimary else HdCardBorder
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.selectMoment(moment) }
                                    .testTag("moment_chip_${moment.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MomentBadge(momentType = moment.momentType)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = moment.title,
                                            color = if (isSelected) HdOnPrimaryContainer else HdTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.1f", moment.startSec)}s - ${String.format(java.util.Locale.US, "%.1f", moment.endSec)}s (${String.format(java.util.Locale.US, "%.1f", moment.endSec - moment.startSec)}s)",
                                            color = if (isSelected) HdPrimary else HdTextSecondary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Frame-by-Frame Fine-Tuning Editor Component
        if (currentMoment != null) {
            item {
                FineTuningEditor(
                    moment = currentMoment,
                    masterClip = currentClip,
                    onSaveTimestamps = { id, start, end ->
                        viewModel.updateMomentTimestamps(id, start, end)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Render Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. TARGET ASPECT RATIO",
                        color = HdPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("9:16", "Shorts / Reels", Icons.Default.Smartphone),
                            Triple("1:1", "Square Feed", Icons.Default.Crop),
                            Triple("16:9", "YouTube Land", Icons.Default.Tv)
                        ).forEach { (ratio, desc, icon) ->
                            val isSelected = targetAspect == ratio
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) HdPrimary else HdContainerLight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) HdPrimary else HdCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { targetAspect = ratio }
                                    .testTag("ratio_button_$ratio")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = ratio,
                                        tint = if (isSelected) Color.White else HdTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ratio,
                                        color = if (isSelected) Color.White else HdTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = desc,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else HdTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. BACKGROUND FILL (FOR VERTICAL CUTS)",
                        color = HdPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair("blurred_fill", "Blurred Fill"),
                            Pair("solid_color", "Solid Tone"),
                            Pair("stadium_theme", "Match Theme")
                        ).forEach { (mode, label) ->
                            val isSelected = backgroundMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) HdPrimaryContainer else HdContainerLight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) HdPrimary else HdCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { backgroundMode = mode }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) HdOnPrimaryContainer else HdTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sound Extraction Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(HdContainerLight)
                            .border(1.dp, HdCardBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = HdPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Extract Sound Track (.AAC)",
                                    color = HdTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Isolates crowd excitement roar and commentary",
                                    color = HdTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = extractSound,
                            onCheckedChange = { extractSound = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HdPrimary,
                                uncheckedThumbColor = HdTextTertiary,
                                uncheckedTrackColor = HdContainerLight
                            ),
                            modifier = Modifier.testTag("extract_sound_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Global Logo & Placement default indicator
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = HdContainerLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E1B24),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (settings.logoUri != null) {
                                            AsyncImage(
                                                model = settings.logoUri,
                                                contentDescription = "Logo",
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Default logo",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Clip Logo Overlay",
                                        color = HdTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Position: ${settings.logoPlacement.replace('_', ' ')}",
                                        color = HdPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HdPrimaryContainer
                            ) {
                                Text(
                                    text = "SETTINGS DEFAULT",
                                    color = HdOnPrimaryContainer,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Commentary Caption preview for current moment
                    if (!currentMoment?.commentaryCaption.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E1B24),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = "AI Captions",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI COMMENTARY CAPTION OVERLAY",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${currentMoment?.commentaryCaption}\"",
                                    color = Color(0xFFFEF3C7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI Dynamic Subtitle & Caption Stylizer Section
                    DynamicSubtitleStylizerCard(
                        captionText = currentMoment?.commentaryCaption ?: "Unbelievable highlight moment!",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark / Channel Tag") },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HdPrimary,
                            unfocusedBorderColor = HdCardBorder,
                            focusedTextColor = HdTextPrimary,
                            unfocusedTextColor = HdTextPrimary,
                            focusedLabelColor = HdPrimary,
                            unfocusedLabelColor = HdTextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // FFmpeg Command Inspector Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HdContainerLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFfmpegInspector = !showFfmpegInspector }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = "FFmpeg Pipeline",
                                        tint = HdPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FFMPEG 9:16 PIPELINE COMMAND",
                                        color = HdTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (showFfmpegInspector) "HIDE ▲" else "VIEW ▼",
                                    color = HdPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (showFfmpegInspector && currentClip != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val cmdPreview = remember(currentClip, currentMoment, targetAspect, backgroundMode, watermarkText) {
                                    viewModel.getFfmpegCommandPreview(
                                        moment = currentMoment,
                                        masterClip = currentClip,
                                        aspectRatio = targetAspect,
                                        backgroundMode = backgroundMode
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = cmdPreview,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentClip != null) {
                                viewModel.startClipRender(
                                    moment = currentMoment,
                                    masterClip = currentClip,
                                    aspectRatio = targetAspect,
                                    backgroundMode = backgroundMode,
                                    watermark = watermarkText,
                                    logoPlacement = settings.logoPlacement,
                                    logoUri = settings.logoUri,
                                    extractSound = extractSound
                                )
                            }
                        },
                        enabled = currentClip != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HdPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = HdContainerLight,
                            disabledContentColor = HdTextTertiary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_render_job_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Render Vertical 9:16 via FFmpeg",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Render Queue Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RENDER QUEUE (${renderJobs.size})",
                    color = HdTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Stored in Local Storage",
                    color = HdTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (renderJobs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HdSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No clips rendered yet. Pick a moment and tap 'Render Now on Device'.",
                            color = HdTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        items(renderJobs) { job ->
            RenderJobCard(
                job = job,
                onDelete = { viewModel.deleteRenderJob(job.id) },
                onSchedulePost = {
                    val matchingMoment = moments.find { it.id == job.momentId } ?: MomentCandidate(
                        masterClipId = job.masterClipId,
                        title = job.title,
                        description = "Rendered 9:16 short clip",
                        startSec = 0f,
                        endSec = job.durationSec.toFloat(),
                        confidence = 0.95f,
                        momentType = "9:16 Short",
                        commentaryCaption = job.title
                    )
                    viewModel.schedulePostForRenderedClip(matchingMoment, "YouTube Shorts")
                    Toast.makeText(context, "Scheduled '${job.title}' to post!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RenderJobCard(
    job: ClipRenderJob,
    onDelete: () -> Unit = {},
    onSchedulePost: () -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("render_job_card_${job.id}")
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
                    Surface(
                        shape = CircleShape,
                        color = HdBadge
                    ) {
                        Text(
                            text = job.targetAspectRatio,
                            color = HdOnPrimaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = job.title,
                        color = HdTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (job.status) {
                        "COMPLETE" -> HdPrimaryContainer
                        "RENDERING" -> Color(0xFFFFDCC1)
                        else -> HdContainerLight
                    }
                ) {
                    Text(
                        text = job.status,
                        color = when (job.status) {
                            "COMPLETE" -> HdOnPrimaryContainer
                            "RENDERING" -> Color(0xFF825500)
                            else -> HdTextSecondary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (job.status == "RENDERING") {
                Spacer(modifier = Modifier.height(10.dp))
                ProcessingProgressBar(
                    progressPercent = job.progress,
                    stageText = "Rendering 9:16 vertical short • ${job.progress}%",
                    phaseType = ProcessingPhaseType.FFMPEG_RENDERING,
                    height = 8.dp,
                    testTag = "render_job_progress_${job.id}"
                )
            }

            if (job.outputVideoPath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saved: ${job.outputVideoPath.substringAfterLast('/')}",
                    color = HdTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Schedule Post, Gallery Export, Direct Share, & Delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSchedulePost,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HdPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HdPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule Post", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (job.outputVideoPath != null) {
                        val file = File(job.outputVideoPath)
                        IconButton(
                            onClick = {
                                com.example.utils.BatchExporter.shareClipsDirectly(
                                    context,
                                    listOf(file),
                                    shareTitle = "Share ${job.title}"
                                )
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Clip Directly",
                                tint = HdPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Rendered Clip",
                            tint = HdTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicSubtitleStylizerCard(
    captionText: String,
    modifier: Modifier = Modifier
) {
    var selectedStyle by remember { mutableStateOf("VIRAL_YELLOW") }
    var yPosition by remember { mutableStateOf("CENTER") }
    var fontSizeSp by remember { mutableStateOf(20) }

    val styles = listOf(
        Triple("VIRAL_YELLOW", "⚡ MrBeast Yellow", Color(0xFFFFE500)),
        Triple("CYBER_NEON", "🔮 Cyber Cyan", Color(0xFF00F0FF)),
        Triple("MINIMAL_PILL", "🏷️ Minimal Pill", Color.White),
        Triple("FLAME_GLOW", "🔥 Flame Red", Color(0xFFFF3B30))
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HdContainerLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, HdCardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HdPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANIMATED DYNAMIC CAPTIONS",
                        color = HdTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HdPrimaryContainer
                ) {
                    Text(
                        text = "VIRAL STYLES",
                        color = HdOnPrimaryContainer,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Style Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { (styleKey, label, color) ->
                    val isSelected = selectedStyle == styleKey
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) HdPrimary else HdSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) HdPrimary else HdCardBorder
                        ),
                        modifier = Modifier.clickable { selectedStyle = styleKey }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else HdTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Position Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("TOP", "CENTER", "BOTTOM").forEach { pos ->
                    val isSelected = yPosition == pos
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) HdPrimaryContainer else HdSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) HdPrimary else HdCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { yPosition = pos }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pos,
                                color = if (isSelected) HdOnPrimaryContainer else HdTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time 9:16 Canvas Live Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, HdCardBorder, RoundedCornerShape(16.dp)),
                contentAlignment = when (yPosition) {
                    "TOP" -> Alignment.TopCenter
                    "BOTTOM" -> Alignment.BottomCenter
                    else -> Alignment.Center
                }
            ) {
                Text(
                    text = "9:16 VERTICAL SHORT CANVAS",
                    color = Color.White.copy(alpha = 0.15f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                val activeTextColor = when (selectedStyle) {
                    "VIRAL_YELLOW" -> Color(0xFFFFE500)
                    "CYBER_NEON" -> Color(0xFF00F0FF)
                    "FLAME_GLOW" -> Color(0xFFFF3B30)
                    else -> Color.White
                }

                val activeBgColor = when (selectedStyle) {
                    "MINIMAL_PILL" -> Color(0xCC0F172A)
                    else -> Color.Transparent
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = activeBgColor,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = if (captionText.isNotBlank()) "\"${captionText.take(45)}...\"" else "\"VIRAL DYNAMIC CAPTION\"",
                        color = activeTextColor,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

