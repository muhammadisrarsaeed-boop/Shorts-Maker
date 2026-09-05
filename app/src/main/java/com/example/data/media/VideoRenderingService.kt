package com.example.data.media

import android.content.Context
import com.example.data.local.ClipRenderJob
import com.example.data.local.FootballDao
import com.example.data.local.MasterClip
import com.example.data.local.MomentCandidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Real-time state of an ongoing video render job.
 */
data class RenderServiceState(
    val activeJobId: Long? = null,
    val isRendering: Boolean = false,
    val currentProgress: Int = 0,
    val currentStageText: String = "",
    val lastCompletedVideoPath: String? = null,
    val lastGeneratedCommand: String? = null,
    val error: String? = null
)

/**
 * Video Rendering Service / Helper:
 * Orchestrates queuing, rendering, and delivering vertical 9:16 MP4 short clips
 * from master long-form footage using the FFmpegVideoRenderer engine.
 */
class VideoRenderingService(
    private val context: Context,
    private val dao: FootballDao
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _serviceState = MutableStateFlow(RenderServiceState())
    val serviceState: StateFlow<RenderServiceState> = _serviceState.asStateFlow()

    /**
     * Enqueues and executes rendering of a video segment into vertical 9:16 MP4.
     */
    suspend fun renderMomentToShorts(
        masterClip: MasterClip,
        moment: MomentCandidate?,
        options: FFmpegRenderOptions,
        jobId: Long? = null
    ): ClipRenderJob? = withContext(Dispatchers.IO) {
        val startSec = moment?.startSec ?: options.startSec
        val endSec = moment?.endSec ?: options.endSec
        val duration = (endSec - startSec).coerceAtLeast(0.5f)

        // 1. Create or fetch DB job record
        val existingJob = if (jobId != null) dao.getRenderJobById(jobId) else null
        val targetJobId = existingJob?.id ?: run {
            val newJob = ClipRenderJob(
                momentId = moment?.id,
                masterClipId = masterClip.id,
                title = moment?.title ?: "Shorts Clip (${options.targetAspectRatio})",
                targetAspectRatio = options.targetAspectRatio,
                backgroundMode = options.backgroundMode,
                watermarkText = options.watermarkText ?: "ClipAI",
                logoPlacement = options.logoPlacement,
                logoUri = options.logoImagePath,
                commentaryCaption = moment?.commentaryCaption ?: options.captionText,
                isSoundExtracted = options.extractAudioSeparate,
                durationSec = duration,
                status = "QUEUED",
                progress = 0
            )
            dao.insertRenderJob(newJob)
        }

        // 2. Prepare destination files
        val outputDir = File(context.filesDir, "rendered_clips").apply { mkdirs() }
        val audioDir = File(context.filesDir, "extracted_sound").apply { mkdirs() }

        val timestamp = System.currentTimeMillis()
        val videoFileName = "short_${timestamp}_${options.targetAspectRatio.replace(':', 'x')}.mp4"
        val audioFileName = "audio_${timestamp}.aac"

        val videoOutFile = File(outputDir, videoFileName)
        val audioOutFile = if (options.extractAudioSeparate) File(audioDir, audioFileName) else null

        // 3. Update state to RENDERING
        _serviceState.value = RenderServiceState(
            activeJobId = targetJobId,
            isRendering = true,
            currentProgress = 5,
            currentStageText = "Initializing FFmpeg 9:16 pipeline..."
        )
        dao.updateRenderJobStatus(targetJobId, "RENDERING")
        dao.updateRenderJobProgress(targetJobId, 5)

        // 4. Run FFmpeg Video Renderer with live progress callbacks
        val result = FFmpegVideoRenderer.executeRender(
            context = context,
            inputPath = masterClip.localUri,
            outputVideoFile = videoOutFile,
            outputAudioFile = audioOutFile,
            options = options.copy(startSec = startSec, endSec = endSec),
            onProgress = { progress, stage ->
                _serviceState.value = _serviceState.value.copy(
                    currentProgress = progress,
                    currentStageText = stage
                )
                serviceScope.launch {
                    dao.updateRenderJobProgress(targetJobId, progress)
                }
            }
        )

        // 5. Finalize DB record and state
        if (result.success && result.outputVideoPath != null) {
            val completedJob = dao.getRenderJobById(targetJobId)?.copy(
                status = "COMPLETE",
                progress = 100,
                outputVideoPath = result.outputVideoPath,
                outputAudioPath = result.outputAudioPath,
                completedAt = System.currentTimeMillis()
            )
            if (completedJob != null) {
                dao.updateRenderJob(completedJob)
            }

            _serviceState.value = RenderServiceState(
                activeJobId = null,
                isRendering = false,
                currentProgress = 100,
                currentStageText = "Render completed!",
                lastCompletedVideoPath = result.outputVideoPath,
                lastGeneratedCommand = result.ffmpegCommand
            )
            completedJob
        } else {
            val failedJob = dao.getRenderJobById(targetJobId)?.copy(
                status = "FAILED",
                progress = 0
            )
            if (failedJob != null) {
                dao.updateRenderJob(failedJob)
            }

            _serviceState.value = RenderServiceState(
                activeJobId = null,
                isRendering = false,
                currentProgress = 0,
                error = result.errorMessage
            )
            failedJob
        }
    }

    /**
     * Helper to preview the exact FFmpeg command string without executing.
     */
    fun previewFfmpegCommand(
        inputPath: String,
        outputPath: String,
        options: FFmpegRenderOptions
    ): String {
        return FFmpegVideoRenderer.buildFFmpegCommand(inputPath, outputPath, options).joinToString(" ")
    }
}
