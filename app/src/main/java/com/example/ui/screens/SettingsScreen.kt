package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
import com.example.viewmodel.FootballViewModel

@Composable
fun SettingsScreen(viewModel: FootballViewModel) {
    val settings by viewModel.settings.collectAsState()

    // Android Photo Picker launcher for selecting custom logo image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateLogoUri(uri.toString())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clip Defaults & Settings",
                        color = HdTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Defaults automatically applied to all clips generated from video",
                        color = HdTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HdPrimaryContainer
                ) {
                    Text(
                        text = "GLOBAL DEFAULTS",
                        color = HdOnPrimaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: Logo & Placement Option
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_logo_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = HdBadge,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Logo",
                                        tint = HdOnPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Logo & Watermark Placement",
                                    color = HdTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Overlay on all rendered clips",
                                    color = HdTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (settings.logoUri != null) {
                            IconButton(
                                onClick = { viewModel.updateLogoUri(null) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove custom logo",
                                    tint = Color(0xFFB3261E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Logo selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Logo preview box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1B24),
                            border = BorderStroke(1.dp, HdCardBorder),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (settings.logoUri != null) {
                                    AsyncImage(
                                        model = settings.logoUri,
                                        contentDescription = "Custom Logo",
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Default logo",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Text(
                                            text = "CLIPPER AI",
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (settings.logoUri != null) "Custom Image Logo Active" else "Default Broadcast Logo Active",
                                color = HdTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Defaults to all clips generated from main source video",
                                color = HdTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_pick_logo")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Select logo",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (settings.logoUri != null) "Change Logo" else "Pick Logo from Device",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Logo Placement in Clips:",
                        color = HdTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 placement buttons grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "TOP_LEFT" to "Top Left",
                            "TOP_RIGHT" to "Top Right"
                        ).forEach { (posKey, label) ->
                            val isSelected = settings.logoPlacement == posKey
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) HdPrimaryContainer else HdContainerLight,
                                border = BorderStroke(1.dp, if (isSelected) HdPrimary else HdCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.updateLogoPlacement(posKey) }
                                    .testTag("placement_$posKey")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = HdOnPrimaryContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = label,
                                        color = if (isSelected) HdOnPrimaryContainer else HdTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "BOTTOM_LEFT" to "Bottom Left",
                            "BOTTOM_RIGHT" to "Bottom Right"
                        ).forEach { (posKey, label) ->
                            val isSelected = settings.logoPlacement == posKey
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) HdPrimaryContainer else HdContainerLight,
                                border = BorderStroke(1.dp, if (isSelected) HdPrimary else HdCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.updateLogoPlacement(posKey) }
                                    .testTag("placement_$posKey")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = HdOnPrimaryContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = label,
                                        color = if (isSelected) HdOnPrimaryContainer else HdTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live preview mockup frame showing logo position
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Studio video background
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                                        )
                                    )
                            )

                            Text(
                                text = "SOURCE VIDEO • 9:16 PREVIEW",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            // Logo placed dynamically
                            val alignment = when (settings.logoPlacement) {
                                "TOP_LEFT" -> Alignment.TopStart
                                "BOTTOM_LEFT" -> Alignment.BottomStart
                                "BOTTOM_RIGHT" -> Alignment.BottomEnd
                                else -> Alignment.TopEnd
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(alignment)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LOGO HERE",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 2: Number of Clips & Min/Max Duration Sliders
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_clip_generation_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HdBadge,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Clips",
                                    tint = HdOnPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Clip Generation Controls",
                                color = HdTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure highlight count and duration limits",
                                color = HdTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Number of Clips Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clips to Generate from Source:",
                            color = HdTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HdPrimaryContainer
                        ) {
                            Text(
                                text = "${settings.numberOfClips} Clips",
                                color = HdOnPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = settings.numberOfClips.toFloat(),
                        onValueChange = { viewModel.updateNumberOfClips(it.toInt()) },
                        valueRange = 1f..15f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = HdPrimary,
                            activeTrackColor = HdPrimary,
                            inactiveTrackColor = HdContainerLight
                        ),
                        modifier = Modifier.testTag("slider_number_of_clips")
                    )

                    // Quick presets chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3 to "Condensed (3)", 5 to "Standard (5)", 8 to "Deep (8)", 12 to "Extended (12)").forEach { (cnt, label) ->
                            val isSelected = settings.numberOfClips == cnt
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateNumberOfClips(cnt) },
                                label = { Text(text = label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HdPrimaryContainer,
                                    selectedLabelColor = HdOnPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimum Clip Duration Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Min time",
                                tint = HdTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Minimum Clip Duration:",
                                color = HdTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${settings.minClipSec.toInt()}s",
                            color = HdPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Slider(
                        value = settings.minClipSec,
                        onValueChange = { newMin ->
                            viewModel.updateClipDurationBounds(newMin, settings.maxClipSec)
                        },
                        valueRange = 5f..30f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            thumbColor = HdPrimary,
                            activeTrackColor = HdPrimary,
                            inactiveTrackColor = HdContainerLight
                        ),
                        modifier = Modifier.testTag("slider_min_clip_sec")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Maximum Clip Duration Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Max time",
                                tint = HdTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Maximum Clip Duration:",
                                color = HdTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${settings.maxClipSec.toInt()}s",
                            color = HdPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Slider(
                        value = settings.maxClipSec,
                        onValueChange = { newMax ->
                            viewModel.updateClipDurationBounds(settings.minClipSec, newMax)
                        },
                        valueRange = 15f..90f,
                        steps = 74,
                        colors = SliderDefaults.colors(
                            thumbColor = HdPrimary,
                            activeTrackColor = HdPrimary,
                            inactiveTrackColor = HdContainerLight
                        ),
                        modifier = Modifier.testTag("slider_max_clip_sec")
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HdContainerLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target Window:",
                                color = HdTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${settings.minClipSec.toInt()}s — ${settings.maxClipSec.toInt()}s per generated clip",
                                color = HdTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 3: Background of Clips (9:16 and other aspect ratios)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_background_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HdBadge,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Background",
                                    tint = HdOnPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Clip Background Styling",
                                color = HdTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Letterbox & pillarbox fill for 9:16 and other formats",
                                color = HdTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Vertical (9:16) Background Mode:",
                        color = HdTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val bgOptions916 = listOf(
                        "blurred_fill" to "Gaussian Blurred Footage (Recommended)",
                        "stadium_pitch" to "Stadium Pitch Emerald Gradient",
                        "high_density_dark" to "High Density Violet Contrast",
                        "solid_black" to "Solid Onyx Black Matte"
                    )

                    bgOptions916.forEach { (modeKey, title) ->
                        val isSelected = settings.backgroundMode916 == modeKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) HdPrimaryContainer else HdContainerLight,
                            border = BorderStroke(1.dp, if (isSelected) HdPrimary else HdCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.updateBackgroundMode916(modeKey) }
                                .testTag("bg_916_$modeKey")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) HdPrimary else Color.Transparent,
                                    border = BorderStroke(2.dp, if (isSelected) HdPrimary else HdCardBorder),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    if (isSelected) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = title,
                                        color = if (isSelected) HdOnPrimaryContainer else HdTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Other Aspect Ratios (1:1, 4:5, 16:9) Background:",
                        color = HdTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "blurred_fill" to "Blurred Fill",
                            "stadium_pitch" to "Stadium Pitch",
                            "solid_black" to "Solid Black"
                        ).forEach { (modeKey, label) ->
                            val isSelected = settings.backgroundModeOther == modeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateBackgroundModeOther(modeKey) },
                                label = { Text(text = label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HdPrimaryContainer,
                                    selectedLabelColor = HdOnPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 4: AI Commentary Captions
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_captions_settings")
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
                                color = HdBadge,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Captions",
                                        tint = HdOnPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gemini Commentary Captions",
                                    color = HdTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Generate TV subtitles on crowd and goal peaks",
                                    color = HdTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = settings.autoCaptionsEnabled,
                            onCheckedChange = { viewModel.updateAutoCaptions(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HdPrimary
                            ),
                            modifier = Modifier.testTag("switch_auto_captions")
                        )
                    }

                    if (settings.autoCaptionsEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E1B24),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "SAMPLE BROADCAST CAPTION OVERLAY:",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"🔥 Most people get this completely backward... here is the 1 rule that changes everything!\"",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
