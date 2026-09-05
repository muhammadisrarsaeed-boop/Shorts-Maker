package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_clips")
data class MasterClip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val localUri: String,
    val durationSec: Long,
    val resolution: String,
    val fps: Int,
    val fileSizeBytes: Long,
    val waveformPoints: String = "", // Comma-separated normalized floats (0.0 to 1.0)
    val rightsConfirmed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "moment_candidates")
data class MomentCandidate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val masterClipId: Long,
    val momentType: String, // "goal_scored", "high_energy_crowd", "goal_missed", "chance_created", "peak_moment", "skill_moment"
    val startSec: Float,
    val endSec: Float,
    val confidence: Float,
    val audioEnergy: Float = 0.8f,
    val priorityScore: Float = 0.85f,
    val title: String,
    val description: String,
    val commentaryCaption: String = "", // AI-generated broadcast commentary captions
    val status: String = "CANDIDATE", // "CANDIDATE", "APPROVED", "REJECTED", "USED"
    val thumbnailBase64: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "clip_render_jobs")
data class ClipRenderJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val momentId: Long? = null,
    val masterClipId: Long,
    val title: String,
    val targetAspectRatio: String = "9:16", // "9:16", "1:1", "16:9", "4:5"
    val backgroundMode: String = "blurred_fill", // "blurred_fill", "stadium_pitch", "high_density_dark", "solid_black"
    val watermarkText: String = "FOOTBALL AI",
    val logoPlacement: String = "TOP_RIGHT", // "TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT"
    val logoUri: String? = null,
    val commentaryCaption: String? = null,
    val isSoundExtracted: Boolean = true,
    val outputVideoPath: String? = null,
    val outputAudioPath: String? = null,
    val status: String = "QUEUED", // "QUEUED", "RENDERING", "COMPLETE", "FAILED"
    val progress: Int = 0,
    val durationSec: Float = 15f,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "schedule_templates")
data class ScheduleTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val platform: String, // "YouTube Shorts", "TikTok", "Instagram Reels", "Facebook"
    val daysOfWeek: String, // "Daily", "Weekend", "Sunday"
    val timeOfDay: String, // "18:30"
    val active: Boolean = true
)

@Entity(tableName = "social_post_records")
data class SocialPostRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val momentId: Long,
    val clipTitle: String,
    val platform: String, // "YOUTUBE", "INSTAGRAM", "THREADS", "FACEBOOK", "TIKTOK"
    val isManual: Boolean = true,
    val status: String = "SUCCESS", // "SUCCESS", "FAILED", "PENDING"
    val postUrl: String = "",
    val caption: String = "",
    val videoFormat: String = "MP4",
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

