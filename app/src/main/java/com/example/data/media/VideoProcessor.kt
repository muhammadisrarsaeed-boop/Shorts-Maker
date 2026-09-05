package com.example.data.media

import android.content.Context
import android.util.Log
import com.example.data.ai.DetectedHighlightMoment
import com.example.data.local.ClipRenderJob
import com.example.data.local.FootballDao
import com.example.data.local.MasterClip
import com.example.data.local.MomentCandidate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Result data from a single video segment processing job.
 */
data class ProcessedShortResult(
    val momentId: Long?,
    val title: String,
    val startSec: Float,
    val endSec: Float,
    val durationSec: Float,
    val outputVideoPath: String,
    val outputAudioPath: String?,
    val viralityScore: Float,
    val ffmpegCommand: String,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

/**
 * Summary result for batch processing multiple Gemini-detected highlight moments.
 */
data class BatchProcessingResult(
    val totalMoments: Int,
    val successfulClips: Int,
    val failedClips: Int,
    val results: List<ProcessedShortResult>,
    val totalTimeTakenMs: Long
)

/**
 * Real-time progress and status tracking for the VideoProcessor.
 */
data class VideoProcessorState(
    val isProcessing: Boolean = false,
    val currentClipIndex: Int = 0,
    val totalClips: Int = 0,
    val currentProgress: Int = 0,
    val currentStage: String = "",
    val activeClipTitle: String = "",
    val recentCompletedShort: ProcessedShortResult? = null,
    val error: String? = null
)

/**
 * VideoProcessor:
 * Dedicated engine utilizing FFmpeg command pipelines to trim videos precisely
 * based on Gemini AI analysis timestamps and crop/re-frame them into high-converting
 * vertical 9:16 aspect ratio MP4 Shorts, Reels, and TikTok clips.
 */
class VideoProcessor(
    private val context: Context,
    private val dao: FootballDao? = null
) {
    companion object {
        private const val TAG = "VideoProcessor"
        const val ASPECT_9_16 = "9:16"
        const val ASPECT_1_1 = "1:1"
        const val ASPECT_16_9 = "16:9"

        const val CROP_MODE_BLUR_FILL = "BLUR_FILL"
        const val CROP_MODE_CENTER_CROP = "CENTER_CROP"
        const val CROP_MODE_SPLIT_SCREEN = "SPLIT_SCREEN"
        const val CROP_MODE_SOLID_LETTERBOX = "SOLID_BLACK"
    }

    private val processorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _processorState = MutableStateFlow(VideoProcessorState())
    val processorState: StateFlow<VideoProcessorState> = _processorState.asStateFlow()

    /**
     * Trims and converts a long master video into a 9:16 vertical short based on
     * Gemini AI detected highlight moment timestamps.
     */
    suspend fun processGeminiMomentToShort(
        masterClip: MasterClip,
        geminiMoment: DetectedHighlightMoment,
        cropMode: String = CROP_MODE_BLUR_FILL,
        watermarkText: String? = "ClipAI",
        logoPlacement: String = "TOP_RIGHT",
        captionStyle: String = "MR_BEAST",
        extractAudio: Boolean = true,
        onProgress: ((percent: Int, stage: String) -> Unit)? = null
    ): ProcessedShortResult = withContext(Dispatchers.IO) {
        val startSec = geminiMoment.startSec
        val endSec = geminiMoment.endSec
        val duration = (endSec - startSec).coerceAtLeast(0.5f)

        Log.i(TAG, "Processing Gemini moment '${geminiMoment.title}' [$startSec - $endSec s] into 9:16 vertical short.")

        _processorState.value = _processorState.value.copy(
            isProcessing = true,
            currentProgress = 10,
            currentStage = "Analyzing Gemini timestamp [${String.format(Locale.US, "%.1f", startSec)}s - ${String.format(Locale.US, "%.1f", endSec)}s]...",
            activeClipTitle = geminiMoment.title
        )
        onProgress?.invoke(10, "Setting up 9:16 video transformation...")

        val outputDir = File(context.filesDir, "rendered_clips").apply { mkdirs() }
        val audioDir = File(context.filesDir, "extracted_sound").apply { mkdirs() }

        val timestamp = System.currentTimeMillis()
        val videoFileName = "short_9_16_${timestamp}_${geminiMoment.momentType}.mp4"
        val audioFileName = "audio_${timestamp}_${geminiMoment.momentType}.aac"

        val videoOutFile = File(outputDir, videoFileName)
        val audioOutFile = if (extractAudio) File(audioDir, audioFileName) else null

        val renderOptions = FFmpegRenderOptions(
            startSec = startSec,
            endSec = endSec,
            targetAspectRatio = ASPECT_9_16,
            outputWidth = 1080,
            outputHeight = 1920,
            backgroundMode = cropMode,
            watermarkText = watermarkText,
            logoPlacement = logoPlacement,
            captionText = geminiMoment.commentaryCaption.ifBlank { geminiMoment.title },
            captionStyle = captionStyle,
            extractAudioSeparate = extractAudio,
            fps = masterClip.fps.takeIf { it in 24..60 } ?: 30
        )

        val ffmpegArgs = FFmpegVideoRenderer.buildFFmpegCommand(
            inputPath = masterClip.localUri,
            outputVideoPath = videoOutFile.absolutePath,
            options = renderOptions
        )
        val commandString = ffmpegArgs.joinToString(" ")

        try {
            // Optional: Insert Job Record in Room Database if DAO is provided
            var dbJobId: Long? = null
            if (dao != null) {
                val renderJob = ClipRenderJob(
                    masterClipId = masterClip.id,
                    title = geminiMoment.title,
                    targetAspectRatio = ASPECT_9_16,
                    backgroundMode = cropMode,
                    watermarkText = watermarkText ?: "ClipAI",
                    logoPlacement = logoPlacement,
                    commentaryCaption = geminiMoment.commentaryCaption,
                    isSoundExtracted = extractAudio,
                    durationSec = duration,
                    status = "RENDERING",
                    progress = 20
                )
                dbJobId = dao.insertRenderJob(renderJob)
            }

            // Execute FFmpeg render pipeline
            val result = FFmpegVideoRenderer.executeRender(
                context = context,
                inputPath = masterClip.localUri,
                outputVideoFile = videoOutFile,
                outputAudioFile = audioOutFile,
                options = renderOptions,
                onProgress = { percent, stage ->
                    _processorState.value = _processorState.value.copy(
                        currentProgress = percent,
                        currentStage = stage
                    )
                    onProgress?.invoke(percent, stage)
                    dbJobId?.let { id ->
                        processorScope.launch {
                            dao?.updateRenderJobProgress(id, percent)
                        }
                    }
                }
            )

            if (result.success && result.outputVideoPath != null) {
                dbJobId?.let { id ->
                    val job = dao?.getRenderJobById(id)
                    if (job != null) {
                        dao.updateRenderJob(
                            job.copy(
                                status = "COMPLETE",
                                progress = 100,
                                outputVideoPath = result.outputVideoPath,
                                outputAudioPath = result.outputAudioPath,
                                completedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                val processedShort = ProcessedShortResult(
                    momentId = null,
                    title = geminiMoment.title,
                    startSec = startSec,
                    endSec = endSec,
                    durationSec = duration,
                    outputVideoPath = result.outputVideoPath,
                    outputAudioPath = result.outputAudioPath,
                    viralityScore = geminiMoment.priorityScore,
                    ffmpegCommand = commandString,
                    isSuccess = true
                )

                _processorState.value = _processorState.value.copy(
                    isProcessing = false,
                    currentProgress = 100,
                    currentStage = "Finished rendering 9:16 vertical short",
                    recentCompletedShort = processedShort
                )

                processedShort
            } else {
                dbJobId?.let { id ->
                    dao?.updateRenderJobStatus(id, "FAILED")
                }
                ProcessedShortResult(
                    momentId = null,
                    title = geminiMoment.title,
                    startSec = startSec,
                    endSec = endSec,
                    durationSec = duration,
                    outputVideoPath = "",
                    outputAudioPath = null,
                    viralityScore = geminiMoment.priorityScore,
                    ffmpegCommand = commandString,
                    isSuccess = false,
                    errorMessage = result.errorMessage ?: "FFmpeg rendering failed"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Video processing error: ${e.message}", e)
            _processorState.value = _processorState.value.copy(
                isProcessing = false,
                error = e.message
            )
            ProcessedShortResult(
                momentId = null,
                title = geminiMoment.title,
                startSec = startSec,
                endSec = endSec,
                durationSec = duration,
                outputVideoPath = "",
                outputAudioPath = null,
                viralityScore = geminiMoment.priorityScore,
                ffmpegCommand = commandString,
                isSuccess = false,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Batch processes a list of Gemini AI detected highlight moments into vertical 9:16 shorts.
     */
    suspend fun batchProcessGeminiMoments(
        masterClip: MasterClip,
        geminiMoments: List<DetectedHighlightMoment>,
        cropMode: String = CROP_MODE_BLUR_FILL,
        watermarkText: String? = "ClipAI",
        onTotalProgress: ((completed: Int, total: Int, currentStage: String) -> Unit)? = null
    ): BatchProcessingResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val results = mutableListOf<ProcessedShortResult>()
        val total = geminiMoments.size

        _processorState.value = _processorState.value.copy(
            isProcessing = true,
            totalClips = total,
            currentClipIndex = 0,
            currentProgress = 0,
            currentStage = "Starting batch 9:16 extraction of $total moments..."
        )

        geminiMoments.forEachIndexed { index, moment ->
            val clipNum = index + 1
            _processorState.value = _processorState.value.copy(
                currentClipIndex = clipNum,
                activeClipTitle = moment.title,
                currentStage = "Rendering clip $clipNum of $total: ${moment.title}"
            )
            onTotalProgress?.invoke(index, total, "Rendering clip $clipNum of $total: ${moment.title}")

            val result = processGeminiMomentToShort(
                masterClip = masterClip,
                geminiMoment = moment,
                cropMode = cropMode,
                watermarkText = watermarkText,
                onProgress = { pct, stage ->
                    val overallPct = (((index.toFloat() / total) * 100) + (pct.toFloat() / total)).toInt()
                    _processorState.value = _processorState.value.copy(
                        currentProgress = overallPct,
                        currentStage = "Clip $clipNum/$total: $stage"
                    )
                }
            )
            results.add(result)
        }

        val successCount = results.count { it.isSuccess }
        val failCount = results.count { !it.isSuccess }

        _processorState.value = _processorState.value.copy(
            isProcessing = false,
            currentProgress = 100,
            currentStage = "Completed batch processing ($successCount/$total clips ready)"
        )

        BatchProcessingResult(
            totalMoments = total,
            successfulClips = successCount,
            failedClips = failCount,
            results = results,
            totalTimeTakenMs = System.currentTimeMillis() - startTime
        )
    }

    /**
     * Trims and renders a custom MomentCandidate entity into vertical 9:16 aspect ratio.
     */
    suspend fun processMomentCandidateTo916(
        masterClip: MasterClip,
        moment: MomentCandidate,
        cropMode: String = CROP_MODE_BLUR_FILL,
        watermarkText: String? = "ClipAI",
        logoPlacement: String = "TOP_RIGHT",
        captionStyle: String = "MR_BEAST"
    ): ProcessedShortResult {
        val geminiEquivalent = DetectedHighlightMoment(
            momentType = moment.momentType,
            startSec = moment.startSec,
            endSec = moment.endSec,
            confidence = moment.confidence,
            audioEnergy = moment.audioEnergy,
            priorityScore = moment.priorityScore,
            title = moment.title,
            description = moment.description,
            commentaryCaption = moment.commentaryCaption
        )
        return processGeminiMomentToShort(
            masterClip = masterClip,
            geminiMoment = geminiEquivalent,
            cropMode = cropMode,
            watermarkText = watermarkText,
            logoPlacement = logoPlacement,
            captionStyle = captionStyle
        )
    }

    /**
     * Builds and returns the exact FFmpeg command string for 9:16 vertical cropping & trimming.
     */
    fun generateFfmpegCommandPreview(
        inputVideoPath: String,
        outputVideoPath: String,
        startSec: Float,
        endSec: Float,
        cropMode: String = CROP_MODE_BLUR_FILL,
        watermarkText: String? = null,
        captionText: String? = null
    ): String {
        val options = FFmpegRenderOptions(
            startSec = startSec,
            endSec = endSec,
            targetAspectRatio = ASPECT_9_16,
            outputWidth = 1080,
            outputHeight = 1920,
            backgroundMode = cropMode,
            watermarkText = watermarkText,
            captionText = captionText
        )
        return FFmpegVideoRenderer.buildFFmpegCommand(inputVideoPath, outputVideoPath, options).joinToString(" ")
    }
}
