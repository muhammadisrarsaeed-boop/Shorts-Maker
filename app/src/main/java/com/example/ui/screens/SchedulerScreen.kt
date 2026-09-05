package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.local.ScheduleTemplate
import com.example.data.local.SocialPostRecord
import com.example.ui.components.ManualPostDialog
import com.example.ui.components.MomentBadge
import com.example.ui.theme.HdBackground
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
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.viewmodel.FootballViewModel

@Composable
fun SchedulerScreen(
    viewModel: FootballViewModel
) {
    val context = LocalContext.current
    val templates by viewModel.scheduleTemplates.collectAsState()
    val moments by viewModel.moments.collectAsState()
    val postRecords by viewModel.postRecords.collectAsState()
    val approvedMoments = moments.filter { it.status == "APPROVED" }

    var selectedPlatformFilter by remember { mutableStateOf("ALL") }
    var manualPostingRecord by remember { mutableStateOf<SocialPostRecord?>(null) }

    val socialAccounts by viewModel.socialAccounts.collectAsState()
    val socialPreferences by viewModel.socialPreferences.collectAsState()

    val filteredRecords = remember(postRecords, selectedPlatformFilter) {
        if (selectedPlatformFilter == "ALL") postRecords
        else postRecords.filter { it.platform.equals(selectedPlatformFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CONTENT CALENDAR & DISTRIBUTION",
                color = HdPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Social Post Calendar",
                color = HdTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Scheduled clip publishing to Shorts, Reels, TikTok, and Facebook",
                color = HdTextSecondary,
                fontSize = 12.sp
            )
        }

        // Distribution Status Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HdSurface),
                border = BorderStroke(1.dp, HdCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = HdPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = HdPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AUTOMATED DISTRIBUTION ACTIVE",
                            color = HdTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${postRecords.size} clips queued for multi-platform social release.",
                            color = HdTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Platform Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val platforms = listOf("ALL", "YouTube Shorts", "TikTok", "Instagram Reels", "Facebook")
                platforms.forEach { platform ->
                    val isSelected = selectedPlatformFilter == platform
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPlatformFilter = platform },
                        label = {
                            Text(
                                text = if (platform == "ALL") "All Platforms" else platform,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HdPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = HdSurface,
                            labelColor = HdTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = HdCardBorder,
                            selectedBorderColor = HdPrimary
                        )
                    )
                }
            }
        }

        // Scheduled Post Queue Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCHEDULED POST QUEUE (${filteredRecords.size})",
                    color = HdTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HdSurface),
                    border = BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No scheduled posts found. Go to 'Clips' tab and click 'Schedule' on any clip.",
                            color = HdTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        items(filteredRecords) { record ->
            ScheduledPostItemCard(
                record = record,
                onManualPost = { manualPostingRecord = record },
                onDelete = {
                    viewModel.deleteSocialPostRecord(record.id)
                    Toast.makeText(context, "Removed scheduled post", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Recurring Slot Templates
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RECURRING PUBLISHING SLOTS (${templates.size})",
                color = HdTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        items(templates) { template ->
            ScheduleTemplateCard(template = template)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Manual Post Dialog Modal
    manualPostingRecord?.let { record ->
        val moment = moments.find { it.id == record.momentId } ?: moments.firstOrNull()
        if (moment != null) {
            ManualPostDialog(
                moment = moment,
                socialAccounts = socialAccounts,
                preferences = socialPreferences,
                onDismiss = { manualPostingRecord = null },
                onPublish = { candidate, platforms, caption, callback ->
                    val selectedPlatform = platforms.firstOrNull()?.displayName ?: record.platform
                    viewModel.schedulePostForRenderedClip(candidate, selectedPlatform)
                    viewModel.markPostRecordAsPosted(record.id, "https://$selectedPlatform.com/post")
                    manualPostingRecord = null
                    Toast.makeText(context, "Published to $selectedPlatform!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ScheduledPostItemCard(
    record: SocialPostRecord,
    onManualPost: () -> Unit,
    onDelete: () -> Unit
) {
    val platformColor = when (record.platform) {
        "YouTube Shorts" -> Color(0xFFEF4444)
        "TikTok" -> Color(0xFF06B6D4)
        "Instagram Reels" -> Color(0xFFEC4899)
        else -> HdPrimary
    }

    val isPosted = record.status == "POSTED"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = BorderStroke(1.dp, HdCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_record_${record.id}")
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
                        color = platformColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = record.platform,
                            color = platformColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.clipTitle,
                        color = HdTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPosted) StatusSuccess.copy(alpha = 0.2f) else StatusWarning.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = record.status,
                        color = if (isPosted) StatusSuccess else StatusWarning,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.caption,
                color = HdTextSecondary,
                fontSize = 11.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onManualPost,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPosted) HdContainerLight else HdPrimary,
                        contentColor = if (isPosted) HdTextPrimary else Color.White
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPosted) "View / Re-post" else "Publish Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Scheduled Post",
                        tint = HdTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleTemplateCard(template: ScheduleTemplate) {
    val platformColor = when (template.platform) {
        "YouTube Shorts" -> Color(0xFFEF4444)
        "TikTok" -> Color(0xFF06B6D4)
        "Instagram Reels" -> Color(0xFFEC4899)
        else -> HdPrimary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = BorderStroke(1.dp, HdCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_template_${template.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = platformColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = platformColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = template.name,
                        color = HdTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${template.platform} • ${template.daysOfWeek}",
                        color = HdTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = HdContainerLight,
                border = BorderStroke(1.dp, HdCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = HdPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = template.timeOfDay,
                        color = HdPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
