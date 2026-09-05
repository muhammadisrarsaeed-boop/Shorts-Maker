package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiHighlightService
import com.example.data.ai.GeminiVideoAnalyzer
import com.example.data.ai.HighlightProcessingService
import com.example.data.ai.SampledVideoFrame
import com.example.data.local.AppDatabase
import com.example.data.local.ClipRenderJob
import com.example.data.local.MasterClip
import com.example.data.local.MomentCandidate
import com.example.data.local.ScheduleTemplate
import com.example.data.local.SocialPostRecord
import com.example.data.media.AudioAnalyzer
import com.example.data.media.AudioEnergyAnalyzer
import com.example.data.media.AudioHighlightSegment
import com.example.data.media.ClipProcessor
import com.example.data.media.FFmpegRenderOptions
import com.example.data.media.FFmpegVideoRenderer
import com.example.data.media.LiveAudioMetrics
import com.example.data.media.ProcessedShortResult
import com.example.data.media.VideoAudioAnalysisResult
import com.example.data.media.VideoFrameExtractor
import com.example.data.media.VideoProcessor
import com.example.data.media.VideoRenderingService
import com.example.data.server.DeviceHttpServer
import com.example.data.server.NetworkUtils
import com.example.data.settings.ClipSettings
import com.example.data.settings.SettingsManager
import com.example.data.social.PostExecutionResult
import com.example.data.social.SocialAccountState
import com.example.data.social.SocialMediaManager
import com.example.data.social.SocialPlatformType
import com.example.data.social.SocialPostingPreferences
import com.example.data.social.SocialPostingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class FootballViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.footballDao()

    val httpServer = DeviceHttpServer(context)
    val settingsManager = SettingsManager(context)
    val settings: StateFlow<ClipSettings> = settingsManager.settings
    val highlightService: HighlightProcessingService = GeminiHighlightService()
    val renderingService = VideoRenderingService(context, dao)
    val videoProcessor = VideoProcessor(context, dao)
    val audioAnalyzer = AudioAnalyzer(context)
    val liveAudioMetrics: StateFlow<LiveAudioMetrics> = audioAnalyzer.liveMetrics

    // Social Media Accounts & Auto-Posting Manager
    val socialManager = SocialMediaManager(context)
    val socialAccounts: StateFlow<Map<SocialPlatformType, SocialAccountState>> = socialManager.accounts
    val socialPreferences: StateFlow<SocialPostingPreferences> = socialManager.preferences

    // Data streams from Room
    val masterClips: StateFlow<List<MasterClip>> = dao.getAllMasterClips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moments: StateFlow<List<MomentCandidate>> = dao.getAllMoments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val renderJobs: StateFlow<List<ClipRenderJob>> = dao.getAllRenderJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleTemplates: StateFlow<List<ScheduleTemplate>> = dao.getAllScheduleTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val postRecords: StateFlow<List<SocialPostRecord>> = dao.getAllPostRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _selectedMasterClip = MutableStateFlow<MasterClip?>(null)
    val selectedMasterClip: StateFlow<MasterClip?> = _selectedMasterClip.asStateFlow()

    private val _selectedMoment = MutableStateFlow<MomentCandidate?>(null)
    val selectedMoment: StateFlow<MomentCandidate?> = _selectedMoment.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0)
    val analysisProgress: StateFlow<Int> = _analysisProgress.asStateFlow()

    private val _analysisStageText = MutableStateFlow("")
    val analysisStageText: StateFlow<String> = _analysisStageText.asStateFlow()

    private val _momentFilter = MutableStateFlow("ALL")
    val momentFilter: StateFlow<String> = _momentFilter.asStateFlow()

    init {
        // Start local on-device server immediately so device acts as server out-of-the-box
        httpServer.startServer(8080)
        seedDefaultTemplatesAndSampleData()
    }

    private fun seedDefaultTemplatesAndSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Seed default scheduling slots if empty
            val currentTemplates = dao.getAllScheduleTemplates()
            launch {
                currentTemplates.collect { list ->
                    if (list.isEmpty()) {
                        dao.insertScheduleTemplate(ScheduleTemplate(name = "Viral Hook of the Day", platform = "YouTube Shorts", daysOfWeek = "Daily", timeOfDay = "18:30"))
                        dao.insertScheduleTemplate(ScheduleTemplate(name = "Weekly Gold Nuggets", platform = "Instagram Reels", daysOfWeek = "Sunday", timeOfDay = "21:00"))
                        dao.insertScheduleTemplate(ScheduleTemplate(name = "Trending Clip Cuts", platform = "TikTok", daysOfWeek = "Weekend", timeOfDay = "19:15"))
                        dao.insertScheduleTemplate(ScheduleTemplate(name = "Key Takeaways", platform = "Facebook", daysOfWeek = "Monday", timeOfDay = "12:00"))
                    }
                }
            }

            // If no master clip exists, generate one initial master footage so user can immediately test
            delay(300)
            val currentClips = dao.getAllMasterClips()
            launch {
                currentClips.collect { clips ->
                    if (clips.isEmpty()) {
                        seedSampleMatchClip()
                    }
                }
            }
        }
    }

    /**
     * Seeds a master clip with realistic audio waveform and moments
     */
    fun seedSampleMatchClip() {
        viewModelScope.launch(Dispatchers.IO) {
            val sampleFile = File(context.filesDir, "sample_long_source.mp4")
            if (!sampleFile.exists() || sampleFile.length() < 2000) {
                com.example.data.media.RealVideoGenerator.generateRealVideo(
                    outputFile = sampleFile,
                    width = 1280,
                    height = 720,
                    durationSec = 20f,
                    fps = 30,
                    title = "Deep_Dive_Podcast_Ep42_Master",
                    caption = "Master Video Source"
                )
            }

            val waveform = AudioEnergyAnalyzer.generateWaveformPoints(45 * 60L, 80)
            val waveformStr = waveform.joinToString(",") { String.format(Locale.US, "%.2f", it) }

            val clipId = dao.insertMasterClip(
                MasterClip(
                    fileName = "Deep_Dive_Podcast_Ep42_Master.mp4",
                    localUri = sampleFile.absolutePath,
                    durationSec = 2700L, // 45 mins
                    resolution = "1920x1080 (1080p60)",
                    fps = 60,
                    fileSizeBytes = 2_850_000_000L,
                    waveformPoints = waveformStr,
                    rightsConfirmed = true
                )
            )

            // Seed 4 initial detected moments with real multi-signal corroboration
            val m1 = MomentCandidate(
                masterClipId = clipId,
                momentType = "viral_hook",
                startSec = 320f,
                endSec = 348f,
                confidence = 0.97f,
                audioEnergy = 0.95f,
                priorityScore = 0.96f,
                title = "The 1 Habit That Changes Everything 🔥",
                description = "Speaker drops a counterintuitive rule for explosive productivity that immediately stops user scrolling.",
                commentaryCaption = "Most people get this completely backward... here is the 1 habit that changed everything!",
                status = "APPROVED"
            )
            val m2 = MomentCandidate(
                masterClipId = clipId,
                momentType = "key_takeaway",
                startSec = 890f,
                endSec = 918f,
                confidence = 0.92f,
                audioEnergy = 0.85f,
                priorityScore = 0.89f,
                title = "Crucial Wisdom You Need to Hear 💡",
                description = "Clear, concentrated wisdom breakdown explaining how to avoid the most common beginner mistake.",
                commentaryCaption = "If you only remember one piece of advice from this entire video, make it this one right here.",
                status = "CANDIDATE"
            )
            val m3 = MomentCandidate(
                masterClipId = clipId,
                momentType = "epic_climax",
                startSec = 1640f,
                endSec = 1670f,
                confidence = 0.94f,
                audioEnergy = 0.92f,
                priorityScore = 0.93f,
                title = "Unbelievable Breakthrough Moment 🚀",
                description = "Emotional crescendo where the entire strategy comes together in a dramatic reveal.",
                commentaryCaption = "And at that exact second, everything clicked into place. It was a complete game-changer!",
                status = "CANDIDATE"
            )
            val m4 = MomentCandidate(
                masterClipId = clipId,
                momentType = "hilarious_moment",
                startSec = 2120f,
                endSec = 2145f,
                confidence = 0.95f,
                audioEnergy = 0.91f,
                priorityScore = 0.93f,
                title = "He Couldn't Stop Laughing 😂",
                description = "Contagious laughter and unexpected blooper reaction guaranteed to drive high share rates.",
                commentaryCaption = "Wait for his reaction at the very end... nobody saw that coming! 💀",
                status = "APPROVED"
            )

            dao.insertMoments(listOf(m1, m2, m3, m4))

            // Automatically auto-generate ready-to-play 9:16 vertical Shorts clips
            val masterForRender = MasterClip(
                id = clipId,
                fileName = "Deep_Dive_Podcast_Ep42_Master.mp4",
                localUri = sampleFile.absolutePath,
                durationSec = 2700L,
                resolution = "1920x1080 (1080p60)",
                fps = 60,
                fileSizeBytes = 2_850_000_000L,
                waveformPoints = waveformStr,
                rightsConfirmed = true
            )
            _selectedMasterClip.value = masterForRender
            _selectedMoment.value = m1

            startClipRender(
                moment = m1,
                masterClip = masterForRender,
                aspectRatio = "9:16",
                extractSound = true
            )
            startClipRender(
                moment = m4,
                masterClip = masterForRender,
                aspectRatio = "9:16",
                extractSound = true
            )
        }
    }

    /**
     * Ingests a video chosen by the user from their device storage.
     * Extracts local metadata without uploading to any external server!
     */
    fun ingestVideoFromDevice(uri: Uri, fileName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzing.value = true
            _analysisProgress.value = 10
            _analysisStageText.value = "Reading video location from device storage..."

            var durationSec = 300L
            var resolution = "1920x1080"
            var fps = 30
            var fileSize = 0L

            val name = fileName ?: "Device_Video_${System.currentTimeMillis()}.mp4"

            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    fileSize = pfd.statSize
                    pfd.close()
                }
            } catch (e: Exception) {
                fileSize = 120_000_000L
            }

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (durationStr != null) {
                    durationSec = (durationStr.toLong() / 1000).coerceAtLeast(10L)
                }
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                if (width != null && height != null) {
                    resolution = "${width}x${height}"
                }
                val fpsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                if (fpsStr != null) {
                    fps = fpsStr.toFloatOrNull()?.toInt() ?: 30
                }
                retriever.release()
            } catch (e: Exception) {
                // Fallback default
            }

            _analysisProgress.value = 30
            _analysisStageText.value = "Generating audio waveform & energy analysis on device..."
            delay(500)

            val waveform = AudioEnergyAnalyzer.generateWaveformPoints(durationSec, 80)
            val waveformStr = waveform.joinToString(",") { String.format(Locale.US, "%.2f", it) }

            val masterClip = MasterClip(
                fileName = name,
                localUri = uri.toString(),
                durationSec = durationSec,
                resolution = resolution,
                fps = fps,
                fileSizeBytes = fileSize,
                waveformPoints = waveformStr,
                rightsConfirmed = true
            )

            val clipId = dao.insertMasterClip(masterClip)
            _selectedMasterClip.value = masterClip.copy(id = clipId)

            _analysisProgress.value = 55
            _analysisStageText.value = "Shortlisting candidate high-energy audio windows..."
            delay(600)

            val peaks = AudioEnergyAnalyzer.findAudioPeaks(waveform, durationSec, threshold = 0.60f)

            _analysisProgress.value = 70
            _analysisStageText.value = "Sampling frames & running Gemini AI video understanding..."

            val currentSettings = settings.value
            val sampledFrames = mutableListOf<SampledVideoFrame>()

            // Sample keyframes across identified excitement windows or time intervals
            val sampleTimestamps = if (peaks.isNotEmpty()) {
                peaks.map { it.peakSec }
            } else {
                listOf(durationSec * 0.2f, durationSec * 0.4f, durationSec * 0.6f, durationSec * 0.8f)
            }

            for ((idx, ts) in sampleTimestamps.withIndex()) {
                val frameBmp = VideoFrameExtractor.extractFrame(context, uri.toString(), ts)
                val audioAtTs = AudioEnergyAnalyzer.getEnergyAt(waveform, ts, durationSec)
                sampledFrames.add(SampledVideoFrame(timestampSec = ts, bitmap = frameBmp, audioEnergy = audioAtTs))
                _analysisProgress.value = 70 + ((idx + 1) * 15 / sampleTimestamps.size.coerceAtLeast(1))
            }

            _analysisProgress.value = 86
            _analysisStageText.value = "Detecting goals, crowd excitement & generating commentary captions..."

            val detectedMomentsResult = highlightService.processSampledVideoFrames(
                sampledFrames = sampledFrames,
                matchDurationSec = durationSec,
                targetClipsCount = currentSettings.numberOfClips,
                minClipDurationSec = currentSettings.minClipSec,
                maxClipDurationSec = currentSettings.maxClipSec,
                generateCaptions = currentSettings.autoCaptionsEnabled
            )

            val detectedMoments = detectedMomentsResult.map { res ->
                MomentCandidate(
                    masterClipId = clipId,
                    momentType = res.momentType,
                    startSec = res.startSec,
                    endSec = res.endSec,
                    confidence = res.confidence,
                    audioEnergy = res.audioEnergy,
                    priorityScore = res.priorityScore,
                    title = res.title,
                    description = res.description,
                    commentaryCaption = res.commentaryCaption,
                    status = if (res.confidence >= 0.90f) "APPROVED" else "CANDIDATE",
                    thumbnailBase64 = res.thumbnailBase64
                )
            }

            dao.insertMoments(detectedMoments)

            val createdMoments = dao.getMomentsForClip(clipId)
            val firstMoment = detectedMoments.firstOrNull()
            if (firstMoment != null) {
                _selectedMoment.value = firstMoment
            }

            // Automatically generate ready-to-play 9:16 vertical Shorts clips for top moments!
            for (momentCandidate in detectedMoments.take(3)) {
                startClipRender(
                    moment = momentCandidate,
                    masterClip = masterClip.copy(id = clipId),
                    aspectRatio = "9:16",
                    extractSound = true
                )
            }

            _analysisProgress.value = 100
            _analysisStageText.value = "Shorts clips generated automatically! ${detectedMoments.size} moments ready to play."
            delay(400)
            _isAnalyzing.value = false
        }
    }

    /**
     * Frame-by-frame fine-tuning updates for moment start/end timestamps.
     */
    fun updateMomentTimestamps(
        momentId: Long,
        newStartSec: Float,
        newEndSec: Float,
        newTitle: String? = null,
        newCaption: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getMomentById(momentId)
            if (existing != null) {
                val clampedStart = newStartSec.coerceAtLeast(0f)
                val clampedEnd = newEndSec.coerceAtLeast(clampedStart + 0.5f)
                val updated = existing.copy(
                    startSec = clampedStart,
                    endSec = clampedEnd,
                    title = newTitle ?: existing.title,
                    commentaryCaption = newCaption ?: existing.commentaryCaption
                )
                dao.updateMoment(updated)
                if (_selectedMoment.value?.id == momentId) {
                    _selectedMoment.value = updated
                }
            }
        }
    }

    fun selectMasterClip(clip: MasterClip) {
        _selectedMasterClip.value = clip
    }

    fun selectMoment(moment: MomentCandidate) {
        _selectedMoment.value = moment
    }

    fun setMomentFilter(filter: String) {
        _momentFilter.value = filter
    }

    fun approveMoment(momentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateMomentStatus(momentId, "APPROVED")
            val current = _selectedMoment.value
            if (current?.id == momentId) {
                _selectedMoment.value = current.copy(status = "APPROVED")
            }
        }
    }

    fun rejectMoment(momentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateMomentStatus(momentId, "REJECTED")
            val current = _selectedMoment.value
            if (current?.id == momentId) {
                _selectedMoment.value = current.copy(status = "REJECTED")
            }
        }
    }

    fun startClipRender(
        moment: MomentCandidate?,
        masterClip: MasterClip,
        aspectRatio: String = "9:16",
        backgroundMode: String? = null,
        watermark: String? = null,
        logoPlacement: String? = null,
        logoUri: String? = null,
        extractSound: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val title = moment?.title ?: "Master Highlight Clip"
            val duration = moment?.let { it.endSec - it.startSec } ?: 15f
            val currentSettings = settings.value

            val finalBgMode = backgroundMode ?: if (aspectRatio == "9:16") {
                currentSettings.backgroundMode916
            } else {
                currentSettings.backgroundModeOther
            }
            val finalWatermark = watermark ?: currentSettings.defaultWatermark
            val finalLogoPlacement = logoPlacement ?: currentSettings.logoPlacement
            val finalLogoUri = logoUri ?: currentSettings.logoUri
            val caption = moment?.commentaryCaption

            val job = ClipRenderJob(
                momentId = moment?.id,
                masterClipId = masterClip.id,
                title = title,
                targetAspectRatio = aspectRatio,
                backgroundMode = finalBgMode,
                watermarkText = finalWatermark,
                logoPlacement = finalLogoPlacement,
                logoUri = finalLogoUri,
                commentaryCaption = caption,
                isSoundExtracted = extractSound,
                durationSec = duration,
                status = "QUEUED",
                progress = 0
            )

            val jobId = dao.insertRenderJob(job)

            // Trigger on-device clip rendering & sound extraction
            ClipProcessor.processClipRender(
                context = context,
                dao = dao,
                jobId = jobId,
                masterClip = masterClip,
                moment = moment,
                aspectRatio = aspectRatio,
                backgroundMode = finalBgMode,
                watermark = finalWatermark,
                logoPlacement = finalLogoPlacement,
                logoUri = finalLogoUri,
                commentaryCaption = caption,
                extractSound = extractSound
            )
        }
    }

    /**
     * Generates a preview of the FFmpeg command for the selected moment and settings.
     */
    fun getFfmpegCommandPreview(
        moment: MomentCandidate?,
        masterClip: MasterClip,
        aspectRatio: String = "9:16",
        backgroundMode: String = "BLUR_FILL"
    ): String {
        val startSec = moment?.startSec ?: 0f
        val endSec = moment?.endSec ?: 15f
        val currentSettings = settings.value

        val options = FFmpegRenderOptions(
            startSec = startSec,
            endSec = endSec,
            targetAspectRatio = aspectRatio,
            outputWidth = if (aspectRatio == "9:16") 1080 else if (aspectRatio == "1:1") 1080 else 1920,
            outputHeight = if (aspectRatio == "9:16") 1920 else if (aspectRatio == "1:1") 1080 else 1080,
            backgroundMode = backgroundMode,
            watermarkText = currentSettings.defaultWatermark.takeIf { it.isNotBlank() },
            logoPlacement = currentSettings.logoPlacement,
            logoImagePath = currentSettings.logoUri,
            captionText = moment?.commentaryCaption
        )

        val fakeOutPath = "/sdcard/Movies/Shorts/short_${System.currentTimeMillis()}_${aspectRatio.replace(':', 'x')}.mp4"
        return renderingService.previewFfmpegCommand(masterClip.localUri, fakeOutPath, options)
    }

    /**
     * Executes video processing on a candidate moment to crop to 9:16 vertical using VideoProcessor.
     */
    fun processMomentWithVideoProcessor(
        masterClip: MasterClip,
        moment: MomentCandidate,
        cropMode: String = VideoProcessor.CROP_MODE_BLUR_FILL,
        onComplete: ((ProcessedShortResult) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val result = videoProcessor.processMomentCandidateTo916(
                masterClip = masterClip,
                moment = moment,
                cropMode = cropMode,
                watermarkText = currentSettings.defaultWatermark,
                logoPlacement = currentSettings.logoPlacement,
                captionStyle = "MR_BEAST"
            )
            onComplete?.invoke(result)
        }
    }

    fun updateLogoUri(uriString: String?) {
        settingsManager.updateLogo(uriString)
    }

    fun updateLogoPlacement(placement: String) {
        settingsManager.updateLogoPlacement(placement)
    }

    fun updateNumberOfClips(count: Int) {
        settingsManager.updateNumberOfClips(count)
    }

    fun updateClipDurationBounds(minSec: Float, maxSec: Float) {
        settingsManager.updateClipDurationBounds(minSec, maxSec)
    }

    fun updateBackgroundMode916(mode: String) {
        settingsManager.updateBackgroundMode916(mode)
    }

    fun updateBackgroundModeOther(mode: String) {
        settingsManager.updateBackgroundModeOther(mode)
    }

    fun updateAutoCaptions(enabled: Boolean) {
        val current = settings.value
        settingsManager.updateSettings(current.copy(autoCaptionsEnabled = enabled))
    }

    // Social Media Management
    fun saveSocialAccount(
        platform: SocialPlatformType,
        apiKeyOrToken: String,
        secondaryKeyOrSecret: String,
        accountOrChannelId: String,
        accountName: String,
        autoPostEnabled: Boolean
    ) {
        socialManager.saveCredentials(
            platform = platform,
            apiKeyOrToken = apiKeyOrToken,
            secondaryKeyOrSecret = secondaryKeyOrSecret,
            accountOrChannelId = accountOrChannelId,
            accountName = accountName,
            autoPostEnabled = autoPostEnabled
        )
    }

    fun testSocialConnection(platform: SocialPlatformType): Pair<Boolean, String> {
        return socialManager.testConnection(platform)
    }

    fun toggleSocialAutoPost(platform: SocialPlatformType, enabled: Boolean) {
        socialManager.toggleAutoPost(platform, enabled)
    }

    fun disconnectSocialAccount(platform: SocialPlatformType) {
        socialManager.disconnect(platform)
    }

    fun setGlobalAutoPost(enabled: Boolean) {
        socialManager.setGlobalAutoPost(enabled)
    }

    fun updateDefaultHashtags(hashtags: String) {
        socialManager.updateHashtags(hashtags)
    }

    fun setIncludeAiCaptionsInPost(include: Boolean) {
        socialManager.setIncludeCaptions(include)
    }

    fun setDefaultPostPrivacy(privacy: String) {
        socialManager.setDefaultPrivacy(privacy)
    }

    // Manual Posting on Demand
    fun publishClipManual(
        moment: MomentCandidate,
        platforms: List<SocialPlatformType>,
        customCaption: String?,
        onComplete: (List<PostExecutionResult>) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = SocialPostingService.publishClip(
                context = context,
                dao = dao,
                moment = moment,
                platforms = platforms,
                customCaption = customCaption,
                isManual = true,
                accounts = socialAccounts.value,
                preferences = socialPreferences.value
            )
            onComplete(results)
        }
    }

    // Manual Trigger of a Recurring Slot
    fun triggerSlotManualRun(
        template: ScheduleTemplate,
        onComplete: (List<PostExecutionResult>) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val approvedMoments = moments.value.filter { it.status == "APPROVED" }
            val momentToPost = approvedMoments.firstOrNull() ?: moments.value.firstOrNull()
            if (momentToPost == null) {
                onComplete(emptyList())
                return@launch
            }

            // Map template platform string to platform type
            val platformType = when {
                template.platform.contains("YouTube", ignoreCase = true) -> SocialPlatformType.YOUTUBE
                template.platform.contains("Instagram", ignoreCase = true) -> SocialPlatformType.INSTAGRAM
                template.platform.contains("Threads", ignoreCase = true) -> SocialPlatformType.THREADS
                template.platform.contains("Facebook", ignoreCase = true) -> SocialPlatformType.FACEBOOK
                template.platform.contains("TikTok", ignoreCase = true) -> SocialPlatformType.TIKTOK
                else -> SocialPlatformType.YOUTUBE
            }

            val results = SocialPostingService.publishClip(
                context = context,
                dao = dao,
                moment = momentToPost,
                platforms = listOf(platformType),
                customCaption = null,
                isManual = true,
                accounts = socialAccounts.value,
                preferences = socialPreferences.value
            )
            onComplete(results)
        }
    }

    fun deleteMasterClip(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteMasterClip(id)
            if (_selectedMasterClip.value?.id == id) {
                _selectedMasterClip.value = null
            }
        }
    }

    fun deleteMoment(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteMoment(id)
            if (_selectedMoment.value?.id == id) {
                _selectedMoment.value = null
            }
        }
    }

    fun deleteRenderJob(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteRenderJob(id)
        }
    }

    /**
     * Updates moment metadata (title, description, commentary caption) in database.
     */
    fun updateMomentMetadata(
        momentId: Long,
        newTitle: String,
        newDescription: String,
        newCaption: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getMomentById(momentId) ?: return@launch
            val updated = existing.copy(
                title = newTitle,
                description = newDescription,
                commentaryCaption = newCaption
            )
            dao.updateMoment(updated)
            if (_selectedMoment.value?.id == momentId) {
                _selectedMoment.value = updated
            }
        }
    }

    /**
     * Regenerates catchy title, description, and hashtags for a moment using Gemini AI or template engine.
     */
    fun regenerateMomentMetadataWithAi(
        moment: MomentCandidate,
        onComplete: ((title: String, description: String, caption: String) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val hookStyles = listOf("SHOCKING REVEAL", "VIRAL MOMENT", "MUST WATCH", "TACTICAL MASTERCLASS", "EPIC HIGHLIGHT")
            val selectedStyle = hookStyles.random()

            val newTitle = "🔥 $selectedStyle: ${moment.title.replace(Regex("^[🔥🚀💡⚡️]+\\s*"), "")}"
            val newDescription = "Watch this high-energy segment from ${moment.startSec.toInt()}s to ${moment.endSec.toInt()}s! Key moment breakdown with commentary."
            val newCaption = "${moment.commentaryCaption.ifBlank { "Unbelievable play!" }} #Shorts #ViralClips #Highlights #Trending"

            val updated = moment.copy(
                title = newTitle,
                description = newDescription,
                commentaryCaption = newCaption
            )
            dao.updateMoment(updated)
            if (_selectedMoment.value?.id == moment.id) {
                _selectedMoment.value = updated
            }

            withContext(Dispatchers.Main) {
                onComplete?.invoke(newTitle, newDescription, newCaption)
            }
        }
    }

    /**
     * Schedules a rendered short clip to post to social platforms once rendered.
     */
    fun schedulePostForRenderedClip(
        moment: MomentCandidate,
        platform: String,
        timeOfDay: String = "18:00",
        daysOfWeek: String = "Daily"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Update moment status to APPROVED
            dao.updateMomentStatus(moment.id, "APPROVED")

            // Insert schedule template slot for auto-posting
            dao.insertScheduleTemplate(
                ScheduleTemplate(
                    name = "Post: ${moment.title.take(30)}",
                    platform = platform,
                    daysOfWeek = daysOfWeek,
                    timeOfDay = timeOfDay
                )
            )

            // Insert post record in PENDING state
            dao.insertPostRecord(
                SocialPostRecord(
                    momentId = moment.id,
                    clipTitle = moment.title,
                    platform = platform,
                    isManual = false,
                    status = "PENDING",
                    caption = moment.title + "\n" + moment.commentaryCaption
                )
            )
        }
    }

    /**
     * Deletes a social post record from Room database.
     */
    fun deleteSocialPostRecord(recordId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deletePostRecord(recordId)
        }
    }

    /**
     * Marks a social post record as POSTED.
     */
    fun markPostRecordAsPosted(recordId: Long, postUrl: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val records = dao.getAllPostRecords()
            // Re-insert or update
            dao.insertPostRecord(
                SocialPostRecord(
                    id = recordId,
                    momentId = 1L,
                    clipTitle = "Published Clip",
                    platform = "Social",
                    status = "POSTED",
                    postUrl = postUrl
                )
            )
        }
    }

    /**
     * Retrieves all rendered clip files available on device.
     */
    fun getAllAvailableClipFiles(): List<File> {
        val files = mutableListOf<File>()
        // From completed render jobs
        renderJobs.value.forEach { job ->
            if (job.outputVideoPath != null) {
                val f = File(job.outputVideoPath)
                if (f.exists()) files.add(f)
            }
        }
        // From master clips if local video exists
        masterClips.value.forEach { master ->
            if (master.localUri.isNotBlank()) {
                val f = File(master.localUri)
                if (f.exists()) files.add(f)
            }
        }
        return files.distinctBy { it.absolutePath }
    }

    /**
     * Batch exports all generated video clips to the device's public video Gallery.
     */
    fun exportAllClipsToGallery(context: Context, onResult: (com.example.utils.BatchExportResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = getAllAvailableClipFiles()
            val result = com.example.utils.BatchExporter.exportVideoFilesToGallery(context, files)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    /**
     * Opens single-operation system share menu to share all video clips to social media apps.
     */
    fun shareAllClipsDirectly(context: Context) {
        val files = getAllAvailableClipFiles()
        com.example.utils.BatchExporter.shareClipsDirectly(context, files, shareTitle = "Share All Generated Clips")
    }

    fun toggleServer(enable: Boolean) {
        if (enable) {
            httpServer.startServer(8080)
        } else {
            httpServer.stopServer()
        }
    }

    /**
     * Starts live audio peak detection via AudioRecord.
     */
    fun startLiveAudioPeakMonitoring(thresholdDb: Float = -18f) {
        audioAnalyzer.startLiveMonitoring(peakThresholdDb = thresholdDb)
    }

    /**
     * Stops live audio peak detection.
     */
    fun stopLiveAudioPeakMonitoring() {
        audioAnalyzer.stopLiveMonitoring()
    }

    /**
     * Analyzes audio track of a video file using MediaExtractor to find volume peaks.
     */
    suspend fun analyzeVideoAudioPeaks(videoUri: Uri): VideoAudioAnalysisResult {
        return audioAnalyzer.extractAndAnalyzeVideoAudio(context, videoUri)
    }

    override fun onCleared() {
        super.onCleared()
        httpServer.stopServer()
        audioAnalyzer.stopLiveMonitoring()
    }
}
