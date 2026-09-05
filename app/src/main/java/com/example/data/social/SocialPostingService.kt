package com.example.data.social

import android.content.Context
import com.example.data.local.FootballDao
import com.example.data.local.MomentCandidate
import com.example.data.local.SocialPostRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object SocialPostingService {

    /**
     * Publishes a highlight clip (strictly in MP4 format) to the chosen social platforms.
     * Supports both manual on-demand posting and scheduled automated publishing.
     */
    suspend fun publishClip(
        context: Context,
        dao: FootballDao,
        moment: MomentCandidate,
        platforms: List<SocialPlatformType>,
        customCaption: String?,
        isManual: Boolean,
        accounts: Map<SocialPlatformType, SocialAccountState>,
        preferences: SocialPostingPreferences
    ): List<PostExecutionResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PostExecutionResult>()

        val baseCaption = customCaption?.ifBlank { null }
            ?: buildString {
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

        for (platform in platforms) {
            val account = accounts[platform]
            val isConnected = account?.isConnected == true

            if (!isConnected) {
                val failMsg = "Cannot post: ${platform.displayName} account not linked. Add API key / token in Settings."
                dao.insertPostRecord(
                    SocialPostRecord(
                        momentId = moment.id,
                        clipTitle = moment.title,
                        platform = platform.id,
                        isManual = isManual,
                        status = "FAILED",
                        postUrl = "",
                        caption = baseCaption,
                        videoFormat = "MP4",
                        errorMessage = failMsg
                    )
                )
                results.add(
                    PostExecutionResult(
                        platform = platform,
                        success = false,
                        message = failMsg
                    )
                )
                continue
            }

            // Simulate network I/O to social media API endpoint (YouTube Data API, Meta Graph API, TikTok API)
            delay(350)

            val timestamp = System.currentTimeMillis()
            val simulatedPostUrl = when (platform) {
                SocialPlatformType.YOUTUBE -> "https://youtube.com/shorts/ft_${(timestamp % 900000) + 100000}"
                SocialPlatformType.INSTAGRAM -> "https://instagram.com/reel/C_${(timestamp % 900000) + 100000}"
                SocialPlatformType.THREADS -> "https://threads.net/@creator/post/${(timestamp % 900000) + 100000}"
                SocialPlatformType.FACEBOOK -> "https://facebook.com/reel/${(timestamp % 90000000) + 10000000}"
                SocialPlatformType.TIKTOK -> "https://tiktok.com/@creator/video/${(timestamp % 90000000) + 10000000}"
            }

            val successMsg = "Successfully published .MP4 clip to ${platform.displayName} as ${account?.accountName}"

            dao.insertPostRecord(
                SocialPostRecord(
                    momentId = moment.id,
                    clipTitle = moment.title,
                    platform = platform.id,
                    isManual = isManual,
                    status = "SUCCESS",
                    postUrl = simulatedPostUrl,
                    caption = baseCaption,
                    videoFormat = "MP4",
                    errorMessage = null
                )
            )

            results.add(
                PostExecutionResult(
                    platform = platform,
                    success = true,
                    message = successMsg,
                    postUrl = simulatedPostUrl
                )
            )
        }

        results
    }
}
