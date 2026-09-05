package com.example.data.media

import android.content.Context
import com.example.data.local.ClipRenderJob
import com.example.data.local.FootballDao
import com.example.data.local.MasterClip
import com.example.data.local.MomentCandidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object ClipProcessor {

    /**
     * Executes the clip generation & sound extraction pipeline using the FFmpegVideoRenderer engine.
     * Generates trimmed vertical 9:16 (or custom aspect) MP4 cuts with background blur and subtitles.
     */
    suspend fun processClipRender(
        context: Context,
        dao: FootballDao,
        jobId: Long,
        masterClip: MasterClip,
        moment: MomentCandidate?,
        aspectRatio: String,
        backgroundMode: String,
        watermark: String,
        logoPlacement: String = "TOP_RIGHT",
        logoUri: String? = null,
        commentaryCaption: String? = null,
        extractSound: Boolean = true
    ): ClipRenderJob? = withContext(Dispatchers.IO) {
        val initialJob = dao.getRenderJobById(jobId) ?: return@withContext null

        try {
            val startSec = moment?.startSec ?: 0f
            val endSec = moment?.endSec ?: 15f

            val outputDir = File(context.filesDir, "rendered_clips").apply { mkdirs() }
            val audioDir = File(context.filesDir, "extracted_sound").apply { mkdirs() }

            val timestamp = System.currentTimeMillis()
            val videoFileName = "clip_${timestamp}_${aspectRatio.replace(':', 'x')}.mp4"
            val audioFileName = "sound_${timestamp}.aac"

            val videoOutFile = File(outputDir, videoFileName)
            val audioOutFile = if (extractSound) File(audioDir, audioFileName) else null

            val renderOptions = FFmpegRenderOptions(
                startSec = startSec,
                endSec = endSec,
                targetAspectRatio = aspectRatio,
                outputWidth = if (aspectRatio == "9:16") 1080 else if (aspectRatio == "1:1") 1080 else 1920,
                outputHeight = if (aspectRatio == "9:16") 1920 else if (aspectRatio == "1:1") 1080 else 1080,
                backgroundMode = backgroundMode,
                watermarkText = watermark.takeIf { it.isNotBlank() },
                logoPlacement = logoPlacement,
                logoImagePath = logoUri,
                captionText = commentaryCaption ?: moment?.commentaryCaption,
                extractAudioSeparate = extractSound
            )

            // Execute FFmpeg render pipeline
            val result = FFmpegVideoRenderer.executeRender(
                context = context,
                inputPath = masterClip.localUri,
                outputVideoFile = videoOutFile,
                outputAudioFile = audioOutFile,
                options = renderOptions,
                onProgress = { progress, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.updateRenderJobProgress(jobId, progress)
                    }
                }
            )

            if (result.success && result.outputVideoPath != null) {
                val finalJob = initialJob.copy(
                    status = "COMPLETE",
                    progress = 100,
                    logoPlacement = logoPlacement,
                    logoUri = logoUri,
                    commentaryCaption = commentaryCaption ?: moment?.commentaryCaption,
                    backgroundMode = backgroundMode,
                    outputVideoPath = result.outputVideoPath,
                    outputAudioPath = result.outputAudioPath,
                    completedAt = System.currentTimeMillis()
                )
                dao.updateRenderJob(finalJob)
                finalJob
            } else {
                val failedJob = initialJob.copy(status = "FAILED", progress = 0)
                dao.updateRenderJob(failedJob)
                failedJob
            }
        } catch (e: Exception) {
            val failedJob = initialJob.copy(status = "FAILED", progress = 0)
            dao.updateRenderJob(failedJob)
            failedJob
        }
    }
}

