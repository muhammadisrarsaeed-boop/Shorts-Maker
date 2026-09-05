package com.example.data.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File

object RealVideoGenerator {
    private const val TAG = "RealVideoGenerator"

    /**
     * Generates an actual, standard-compliant H.264 AVC MP4 video file
     * with real animated video frames, dynamic motion graphics, and wave patterns.
     */
    fun generateRealVideo(
        outputFile: File,
        width: Int = 720,
        height: Int = 1280,
        durationSec: Float = 15f,
        fps: Int = 30,
        title: String = "AI Generated Short",
        caption: String = "Viral Moment"
    ): Boolean {
        try {
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) {
                outputFile.delete()
            }

            val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
            val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val encoder = MediaCodec.createEncoderByType(mimeType)

            // Choose color format supported by device
            val caps = encoder.codecInfo.getCapabilitiesForType(mimeType)
            var chosenColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            val supportedFormats = caps.colorFormats
            if (supportedFormats != null) {
                for (fmt in supportedFormats) {
                    if (fmt == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                        chosenColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
                        break
                    } else if (fmt == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                        chosenColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
                        break
                    }
                }
            }
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, chosenColorFormat)

            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val totalFrames = (durationSec * fps).toInt().coerceAtLeast(fps)
            val frameDurationUs = 1_000_000L / fps

            val bufferInfo = MediaCodec.BufferInfo()
            val ySize = width * height
            val uvSize = ySize / 4
            val frameData = ByteArray(ySize + uvSize * 2)

            for (frameIdx in 0 until totalFrames) {
                val presentationTimeUs = frameIdx * frameDurationUs

                // Fill animated frame data into YUV byte array
                fillYuvFrameData(
                    frameData = frameData,
                    width = width,
                    height = height,
                    frameIdx = frameIdx,
                    totalFrames = totalFrames,
                    colorFormat = chosenColorFormat
                )

                // Enqueue to Encoder
                var inputDequeued = false
                var attempts = 0
                while (!inputDequeued && attempts < 100) {
                    attempts++
                    val inputBufferIndex = encoder.dequeueInputBuffer(10_000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = encoder.getInputBuffer(inputBufferIndex)
                        if (inputBuffer != null) {
                            inputBuffer.clear()
                            inputBuffer.put(frameData)
                            val flags = if (frameIdx == totalFrames - 1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                            encoder.queueInputBuffer(inputBufferIndex, 0, frameData.size, presentationTimeUs, flags)
                            inputDequeued = true
                        }
                    }
                }

                // Dequeue from Encoder & Write to Muxer
                drainEncoder(encoder, muxer, bufferInfo, false) { idx ->
                    videoTrackIndex = idx
                    muxerStarted = true
                }
            }

            // Signal EOF and drain remaining frames
            drainEncoder(encoder, muxer, bufferInfo, true) { idx ->
                videoTrackIndex = idx
                muxerStarted = true
            }

            encoder.stop()
            encoder.release()

            if (muxerStarted) {
                muxer.stop()
                muxer.release()
            }

            Log.i(TAG, "Successfully generated real H.264 video file at ${outputFile.absolutePath} (${outputFile.length()} bytes)")
            return outputFile.exists() && outputFile.length() > 2000
        } catch (e: Exception) {
            Log.e(TAG, "Error generating real video: ${e.message}", e)
            return false
        }
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onFormatChanged: (Int) -> Unit
    ) {
        var videoTrackIndex = 0
        var loopCount = 0
        while (loopCount < 50) {
            loopCount++
            val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = encoder.outputFormat
                videoTrackIndex = muxer.addTrack(newFormat)
                muxer.start()
                onFormatChanged(videoTrackIndex)
            } else if (encoderStatus >= 0) {
                val encodedData = encoder.getOutputBuffer(encoderStatus)
                if (encodedData != null) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size != 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(encoderStatus, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }

    private fun fillYuvFrameData(
        frameData: ByteArray,
        width: Int,
        height: Int,
        frameIdx: Int,
        totalFrames: Int,
        colorFormat: Int
    ) {
        val ySize = width * height
        val isSemiPlanar = (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
        val phase = (frameIdx * 0.15f)

        // Y Plane (Luminance)
        for (y in 0 until height) {
            val yRatio = y / height.toFloat()
            for (x in 0 until width) {
                val xRatio = x / width.toFloat()

                // Background dark gradient with animated wave lines
                var luma = (30 + yRatio * 50).toInt()

                // Animated vertical frequency bars in bottom half
                if (y in (height * 0.55).toInt()..(height * 0.85).toInt()) {
                    val barIndex = (xRatio * 32).toInt()
                    val barHeightFactor = kotlin.math.abs(kotlin.math.sin((barIndex * 0.8f + phase).toDouble())).toFloat()
                    val barTopY = height * 0.85f - (height * 0.28f * barHeightFactor)
                    if (y >= barTopY) {
                        luma = 220
                    }
                }

                // Top HUD border line
                if (y in (height * 0.08).toInt()..(height * 0.085).toInt()) {
                    luma = 240
                }

                // Video Center Frame Box
                if (y in (height * 0.15).toInt()..(height * 0.50).toInt()) {
                    if (x in (width * 0.1).toInt()..(width * 0.9).toInt()) {
                        val innerX = x - width * 0.1f
                        val innerY = y - height * 0.15f
                        val diagonalWave = (kotlin.math.sin((innerX + innerY + frameIdx * 8) * 0.03) * 40).toInt()
                        luma = (120 + diagonalWave).coerceIn(60, 230)
                    }
                }

                frameData[y * width + x] = luma.coerceIn(0, 255).toByte()
            }
        }

        // U and V Planes (Chrominance / Color Tint)
        val uOffset = ySize
        val vOffset = ySize + ySize / 4
        val uvHeight = height / 2
        val uvWidth = width / 2

        for (row in 0 until uvHeight) {
            for (col in 0 until uvWidth) {
                val idx = row * uvWidth + col
                val uVal = (128 + (kotlin.math.sin(phase.toDouble()) * 60).toInt()).coerceIn(16, 240).toByte()
                val vVal = (160 + (kotlin.math.cos(phase.toDouble()) * 60).toInt()).coerceIn(16, 240).toByte()

                if (isSemiPlanar) {
                    val nvIdx = ySize + row * width + col * 2
                    if (nvIdx + 1 < frameData.size) {
                        frameData[nvIdx] = uVal
                        frameData[nvIdx + 1] = vVal
                    }
                } else {
                    if (uOffset + idx < frameData.size) frameData[uOffset + idx] = uVal
                    if (vOffset + idx < frameData.size) frameData[vOffset + idx] = vVal
                }
            }
        }
    }
}
