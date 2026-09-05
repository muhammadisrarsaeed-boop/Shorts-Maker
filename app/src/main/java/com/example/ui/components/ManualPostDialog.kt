package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MomentCandidate
import com.example.data.social.PostExecutionResult
import com.example.data.social.SocialAccountState
import com.example.data.social.SocialPlatformType
import com.example.data.social.SocialPostingPreferences
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
import kotlinx.coroutines.launch

@Composable
fun ManualPostDialog(
    moment: MomentCandidate,
    socialAccounts: Map<SocialPlatformType, SocialAccountState>,
    preferences: SocialPostingPreferences,
    onDismiss: () -> Unit,
    onPublish: (MomentCandidate, List<SocialPlatformType>, String, (List<PostExecutionResult>) -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Default pre-populated caption
    val initialCaption = remember {
        buildString {
            append(moment.title)
            if (preferences.includeAiCaptionsInPost && moment.commentaryCaption.isNotBlank()) {
                append("\n\n🎙️ ")
                append(moment.commentaryCaption)
            }
            if (preferences.defaultHashtags.isNotBlank()) {
                append("\n\n")
                append(preferences.defaultHashtags)
            }
        }
    }

    var captionText by remember { mutableStateOf(initialCaption) }

    // Pre-select all connected platforms by default
    val selectedPlatforms = remember {
        mutableStateListOf<SocialPlatformType>().apply {
            SocialPlatformType.entries.forEach { p ->
                if (socialAccounts[p]?.isConnected == true) {
                    add(p)
                }
            }
            if (isEmpty()) {
                add(SocialPlatformType.YOUTUBE)
            }
        }
    }

    var isPosting by remember { mutableStateOf(false) }
    var postingResults by remember { mutableStateOf<List<PostExecutionResult>?>(null) }

    Dialog(
        onDismissRequest = { if (!isPosting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = HdSurface),
            border = BorderStroke(1.dp, HdCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("manual_post_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
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
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = HdPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Manual Social Post",
                                color = HdTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Publish highlight immediately to selected channels",
                                color = HdTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isPosting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = HdTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = HdCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Clip Info & Format Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = HdBackground,
                    border = BorderStroke(1.dp, HdCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = moment.title,
                                color = HdTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Duration: ${(moment.endSec - moment.startSec).toInt()}s • Audio Energy: ${(moment.audioEnergy * 100).toInt()}%",
                                color = HdTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Mandatory .MP4 format pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = ".MP4 H.264",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (postingResults == null) {
                    // Caption & Hashtags Field
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "CAPTION & HASHTAGS",
                        color = HdTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        placeholder = { Text("Enter post caption with commentary & tags...") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HdPrimary,
                            unfocusedBorderColor = HdCardBorder,
                            focusedTextColor = HdTextPrimary,
                            unfocusedTextColor = HdTextPrimary,
                            focusedContainerColor = HdBackground,
                            unfocusedContainerColor = HdBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_manual_post_caption")
                    )

                    // Target Platforms Selection
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT TARGET PLATFORMS (${selectedPlatforms.size}/${SocialPlatformType.entries.size})",
                            color = HdTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Select All",
                            color = HdPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    selectedPlatforms.clear()
                                    selectedPlatforms.addAll(SocialPlatformType.entries)
                                }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Platform List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SocialPlatformType.entries.forEach { platform ->
                            val account = socialAccounts[platform]
                            val isConnected = account?.isConnected == true
                            val isSelected = selectedPlatforms.contains(platform)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) platform.primaryColor.copy(alpha = 0.12f) else HdBackground,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) platform.primaryColor else HdCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedPlatforms.remove(platform)
                                        } else {
                                            selectedPlatforms.add(platform)
                                        }
                                    }
                                    .testTag("select_platform_${platform.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = platform.primaryColor,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = platform.displayName,
                                                color = HdTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (isConnected) account.accountName else "Not Linked (requires API Key)",
                                                color = if (isConnected) Color(0xFF10B981) else Color(0xFFF59E0B),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isConnected) Color(0xFF064E3B) else Color(0xFF451A03)
                                    ) {
                                        Text(
                                            text = if (isConnected) "LINKED" else "UNLINKED",
                                            color = if (isConnected) Color(0xFF34D399) else Color(0xFFFBBF24),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Publish Button
                    Button(
                        onClick = {
                            if (selectedPlatforms.isNotEmpty()) {
                                isPosting = true
                                coroutineScope.launch {
                                    onPublish(
                                        moment,
                                        selectedPlatforms.toList(),
                                        captionText
                                    ) { results ->
                                        isPosting = false
                                        postingResults = results
                                    }
                                }
                            }
                        },
                        enabled = selectedPlatforms.isNotEmpty() && !isPosting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HdPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_manual_post_button")
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Publishing .MP4 to ${selectedPlatforms.size} Platforms...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Publish .MP4 Now (${selectedPlatforms.size} channels)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Results View after publishing
                    val results = postingResults ?: emptyList()
                    val successCount = results.count { it.success }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (successCount > 0) Color(0xFF064E3B) else Color(0xFF451A03),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (successCount > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (successCount > 0) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (successCount > 0) "PUBLISHED SUCCESSFULLY!" else "PUBLISHING NOTICE",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$successCount of ${results.size} platforms received .MP4 highlight clip",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "PUBLISHED POST DETAILS:",
                        color = HdTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        results.forEach { res ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HdBackground,
                                border = BorderStroke(1.dp, HdCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = res.platform.displayName,
                                            color = res.platform.primaryColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (res.success) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                                        ) {
                                            Text(
                                                text = if (res.success) "SUCCESS" else "FAILED",
                                                color = if (res.success) Color(0xFF34D399) else Color(0xFFF87171),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = res.message,
                                        color = HdTextSecondary,
                                        fontSize = 11.sp
                                    )

                                    if (res.postUrl != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = res.postUrl,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HdPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
