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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.viewmodel.FootballViewModel

@Composable
fun SocialAccountSettingsSection(
    viewModel: FootballViewModel,
    accounts: Map<SocialPlatformType, SocialAccountState>,
    preferences: SocialPostingPreferences
) {
    var expandedPlatform by remember { mutableStateOf<SocialPlatformType?>(null) }
    var hashtagsText by remember(preferences.defaultHashtags) { mutableStateOf(preferences.defaultHashtags) }

    // Section Header: Mandatory .MP4 Export Format Specification
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = BorderStroke(1.dp, HdCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_mp4_format_spec")
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
                        color = Color(0xFF064E3B),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Clip Output Format: .MP4",
                            color = HdTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "H.264 Video + AAC-LC Audio (Standard Social Media Container)",
                            color = HdTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF064E3B),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Text(
                        text = "100% .MP4 READY",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = HdBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "• Container: ISO/IEC 14496-12 MP4 (.mp4)\n• Video Track: AVC / H.264 baseline-to-high profile (1080x1920 9:16 or 16:9)\n• Audio Track: AAC Stereo @ 44.1kHz / 128 kbps\n• Compatibility: Verified for YouTube Shorts, Instagram Reels, Threads, Facebook, & TikTok APIs",
                        color = HdTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Section: Social Media Auto-Posting Engine Master Switch & Hashtags
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = BorderStroke(1.dp, HdCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_social_autopost_master")
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
                        color = HdPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = HdPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto-Posting Engine",
                            color = HdTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Post scheduled .MP4 clips automatically to linked social channels",
                            color = HdTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = preferences.globalAutoPostEnabled,
                    onCheckedChange = { viewModel.setGlobalAutoPost(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = HdPrimary
                    ),
                    modifier = Modifier.testTag("switch_global_autopost")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = HdCardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Default Post Hashtags
            Text(
                text = "DEFAULT POST HASHTAGS",
                color = HdTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = hashtagsText,
                    onValueChange = {
                        hashtagsText = it
                        viewModel.updateDefaultHashtags(it)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HdPrimary,
                        unfocusedBorderColor = HdCardBorder,
                        focusedTextColor = HdTextPrimary,
                        unfocusedTextColor = HdTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_default_hashtags")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy mode options
            Text(
                text = "DEFAULT POST PRIVACY",
                color = HdTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PUBLIC", "UNLISTED", "PRIVATE").forEach { privacy ->
                    FilterChip(
                        selected = preferences.defaultPrivacy == privacy,
                        onClick = { viewModel.setDefaultPostPrivacy(privacy) },
                        label = {
                            Text(
                                text = privacy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HdPrimaryContainer,
                            selectedLabelColor = HdOnPrimaryContainer
                        )
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Section: Linked Social Media Accounts (YouTube, Instagram, Threads, Facebook, TikTok)
    Text(
        text = "LINKED SOCIAL ACCOUNTS (${SocialPlatformType.entries.count { accounts[it]?.isConnected == true }}/${SocialPlatformType.entries.size} LINKED)",
        color = HdTextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    SocialPlatformType.entries.forEach { platform ->
        val account = accounts[platform] ?: SocialAccountState(platform)
        val isExpanded = expandedPlatform == platform

        SocialAccountCard(
            platform = platform,
            account = account,
            isExpanded = isExpanded,
            onToggleExpand = {
                expandedPlatform = if (isExpanded) null else platform
            },
            onSaveCredentials = { key1, key2, id, name, autoPost ->
                viewModel.saveSocialAccount(
                    platform = platform,
                    apiKeyOrToken = key1,
                    secondaryKeyOrSecret = key2,
                    accountOrChannelId = id,
                    accountName = name,
                    autoPostEnabled = autoPost
                )
            },
            onTestConnection = {
                viewModel.testSocialConnection(platform)
            },
            onToggleAutoPost = { enabled ->
                viewModel.toggleSocialAutoPost(platform, enabled)
            },
            onDisconnect = {
                viewModel.disconnectSocialAccount(platform)
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun SocialAccountCard(
    platform: SocialPlatformType,
    account: SocialAccountState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSaveCredentials: (String, String, String, String, Boolean) -> Unit,
    onTestConnection: () -> Pair<Boolean, String>,
    onToggleAutoPost: (Boolean) -> Unit,
    onDisconnect: () -> Unit
) {
    var key1Input by remember(account.apiKeyOrToken) { mutableStateOf(account.apiKeyOrToken) }
    var key2Input by remember(account.secondaryKeyOrSecret) { mutableStateOf(account.secondaryKeyOrSecret) }
    var idInput by remember(account.accountOrChannelId) { mutableStateOf(account.accountOrChannelId) }
    var nameInput by remember(account.accountName) { mutableStateOf(account.accountName) }
    var autoPostChecked by remember(account.autoPostEnabled) { mutableStateOf(account.autoPostEnabled) }
    var verificationStatus by remember(account.verificationMessage) { mutableStateOf(account.verificationMessage) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HdSurface),
        border = BorderStroke(
            1.dp,
            if (account.isConnected) platform.primaryColor.copy(alpha = 0.5f) else HdCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_social_${platform.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main card row
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
                        color = platform.primaryColor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = platform.shortName.take(2).uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = platform.displayName,
                                color = HdTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (account.isConnected) Color(0xFF064E3B) else Color(0xFF262626)
                            ) {
                                Text(
                                    text = if (account.isConnected) "LINKED ✓" else "NOT LINKED",
                                    color = if (account.isConnected) Color(0xFF34D399) else Color(0xFF9CA3AF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (account.isConnected) account.accountName else platform.formatDescription,
                            color = if (account.isConnected) platform.primaryColor else HdTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (account.isConnected) {
                        Switch(
                            checked = account.autoPostEnabled,
                            onCheckedChange = { onToggleAutoPost(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = platform.primaryColor
                            ),
                            modifier = Modifier.testTag("switch_autopost_${platform.id}")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand configuration",
                            tint = HdTextSecondary
                        )
                    }
                }
            }

            // Expandable Credentials Configuration Panel
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = HdCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AUTHENTICATION & API CREDENTIALS",
                        color = HdTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Account Name / Handle
                    Text(
                        text = "Profile / Channel Handle:",
                        color = HdTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text(platform.handleFormat) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = platform.primaryColor,
                            unfocusedBorderColor = HdCardBorder,
                            focusedTextColor = HdTextPrimary,
                            unfocusedTextColor = HdTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_handle_${platform.id}")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key 1: API Key or Access Token
                    Text(
                        text = platform.key1Label,
                        color = HdTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = key1Input,
                        onValueChange = { key1Input = it },
                        placeholder = { Text(platform.key1Hint) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = platform.primaryColor,
                            unfocusedBorderColor = HdCardBorder,
                            focusedTextColor = HdTextPrimary,
                            unfocusedTextColor = HdTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_key1_${platform.id}")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ID: Account ID, Channel ID, or Page ID
                    Text(
                        text = platform.idLabel,
                        color = HdTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = idInput,
                        onValueChange = { idInput = it },
                        placeholder = { Text(platform.idHint) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = platform.primaryColor,
                            unfocusedBorderColor = HdCardBorder,
                            focusedTextColor = HdTextPrimary,
                            unfocusedTextColor = HdTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_id_${platform.id}")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key 2: Optional Client Secret / App ID
                    Text(
                        text = platform.key2Label,
                        color = HdTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = key2Input,
                        onValueChange = { key2Input = it },
                        placeholder = { Text(platform.key2Hint) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = platform.primaryColor,
                            unfocusedBorderColor = HdCardBorder,
                            focusedTextColor = HdTextPrimary,
                            unfocusedTextColor = HdTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_key2_${platform.id}")
                    )

                    // Auto-post checkbox
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Auto-Posting on Schedule",
                            color = HdTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = autoPostChecked,
                            onCheckedChange = { autoPostChecked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = platform.primaryColor
                            )
                        )
                    }

                    if (verificationStatus.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = verificationStatus,
                            color = if (account.isConnected) Color(0xFF34D399) else Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons: Save, Test, Disconnect
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveCredentials(
                                    key1Input,
                                    key2Input,
                                    idInput,
                                    nameInput,
                                    autoPostChecked
                                )
                                val res = onTestConnection()
                                verificationStatus = res.second
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = platform.primaryColor),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_save_${platform.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save & Link",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val res = onTestConnection()
                                verificationStatus = res.second
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_test_${platform.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Test Connection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (account.isConnected) {
                            IconButton(
                                onClick = {
                                    onDisconnect()
                                    key1Input = ""
                                    key2Input = ""
                                    idInput = ""
                                    nameInput = ""
                                    verificationStatus = "Account unlinked"
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LinkOff,
                                    contentDescription = "Disconnect",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
