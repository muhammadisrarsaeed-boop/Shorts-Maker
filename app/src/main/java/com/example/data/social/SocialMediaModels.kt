package com.example.data.social

import androidx.compose.ui.graphics.Color

enum class SocialPlatformType(
    val id: String,
    val displayName: String,
    val shortName: String,
    val handleFormat: String,
    val formatDescription: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val key1Label: String,
    val key1Hint: String,
    val key2Label: String,
    val key2Hint: String,
    val idLabel: String,
    val idHint: String
) {
    YOUTUBE(
        id = "YOUTUBE",
        displayName = "YouTube Shorts",
        shortName = "YouTube",
        handleFormat = "@ChannelHandle",
        formatDescription = "YouTube Data API v3 • MP4 9:16 Shorts",
        primaryColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFFEE2E2),
        key1Label = "YouTube API Key / OAuth Client ID",
        key1Hint = "AIzaSy... or OAuth 2.0 Client ID",
        key2Label = "OAuth Client Secret (Optional)",
        key2Hint = "GOCSPX-... (for automated upload token)",
        idLabel = "YouTube Channel ID",
        idHint = "UC9876543210abcdef"
    ),
    INSTAGRAM(
        id = "INSTAGRAM",
        displayName = "Instagram Reels",
        shortName = "Instagram",
        handleFormat = "@instagram_creator",
        formatDescription = "Instagram Graph API • MP4 Reels (H.264)",
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFFFCE7F3),
        key1Label = "Meta Graph Access Token",
        key1Hint = "EAAB... (Token with instagram_content_publish)",
        key2Label = "App Secret (Optional)",
        key2Hint = "Meta App Secret for HMAC validation",
        idLabel = "Instagram Business Account ID",
        idHint = "17841400123456789"
    ),
    THREADS(
        id = "THREADS",
        displayName = "Threads",
        shortName = "Threads",
        handleFormat = "@threads_creator",
        formatDescription = "Threads API (Meta) • MP4 Video Container",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFD1FAE5),
        key1Label = "Threads Access Token",
        key1Hint = "THQ... (Long-lived creator token)",
        key2Label = "App Secret (Optional)",
        key2Hint = "Threads App Secret",
        idLabel = "Threads User ID",
        idHint = "987654321012345"
    ),
    FACEBOOK(
        id = "FACEBOOK",
        displayName = "Facebook Reels",
        shortName = "Facebook",
        handleFormat = "fb.me/page",
        formatDescription = "Facebook Graph API • MP4 Page Reels",
        primaryColor = Color(0xFF3B82F6),
        secondaryColor = Color(0xFFDBEAFE),
        key1Label = "Page Access Token",
        key1Hint = "EAAG... (Page Token with pages_manage_posts)",
        key2Label = "Facebook App ID (Optional)",
        key2Hint = "App ID from developers.facebook.com",
        idLabel = "Facebook Page ID",
        idHint = "1000987654321"
    ),
    TIKTOK(
        id = "TIKTOK",
        displayName = "TikTok",
        shortName = "TikTok",
        handleFormat = "@tiktok_creator",
        formatDescription = "TikTok Content Posting API • MP4 9:16 Video",
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFFCFFAFE),
        key1Label = "TikTok Creator Access Token",
        key1Hint = "act.example.token.v2...",
        key2Label = "Client Key / App ID",
        key2Hint = "aw... (TikTok Developer Client Key)",
        idLabel = "Creator Open ID",
        idHint = "_000abcdef123456789"
    );

    companion object {
        fun fromId(id: String): SocialPlatformType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: YOUTUBE
        }
    }
}

data class SocialAccountState(
    val platform: SocialPlatformType,
    val isConnected: Boolean = false,
    val autoPostEnabled: Boolean = true,
    val apiKeyOrToken: String = "",
    val secondaryKeyOrSecret: String = "",
    val accountOrChannelId: String = "",
    val accountName: String = "",
    val lastVerifiedAt: Long? = null,
    val verificationMessage: String = ""
)

data class SocialPostingPreferences(
    val globalAutoPostEnabled: Boolean = true,
    val defaultHashtags: String = "#Football #Highlights #Shorts #Reels #TikTokSports #Golazo",
    val includeAiCaptionsInPost: Boolean = true,
    val defaultPrivacy: String = "PUBLIC" // "PUBLIC", "UNLISTED", "PRIVATE"
)

data class PostExecutionResult(
    val platform: SocialPlatformType,
    val success: Boolean,
    val message: String,
    val postUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
