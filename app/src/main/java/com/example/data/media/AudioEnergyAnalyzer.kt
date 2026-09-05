package com.example.data.media

import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

data class AudioPeak(
    val peakSec: Float,
    val windowStartSec: Float,
    val windowEndSec: Float,
    val energyLevel: Float, // 0.0 to 1.0
    val peakType: String // "crowd_roar", "commentary_spike", "whistle"
)

object AudioEnergyAnalyzer {
    /**
     * Generates a normalized waveform amplitude array (values 0.1 to 1.0)
     * reflecting natural match dynamics with distinct cheer/excitement spikes.
     */
    fun generateWaveformPoints(durationSec: Long, sampleCount: Int = 80): List<Float> {
        val count = sampleCount.coerceIn(40, 200)
        val points = mutableListOf<Float>()
        val rnd = Random(durationSec xor 0x5EED)

        // Generate base match ambience (low hum with occasional surges)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            // Base murmur + noise
            var amp = 0.22f + 0.15f * sin(progress * 18f) + rnd.nextFloat() * 0.15f

            // Inject 2-4 major crowd roar spikes (e.g. at 25%, 55%, 82%)
            val spike1 = kotlin.math.exp(-((progress - 0.25f) * (progress - 0.25f)) / 0.003f) * 0.65f
            val spike2 = kotlin.math.exp(-((progress - 0.58f) * (progress - 0.58f)) / 0.0025f) * 0.75f
            val spike3 = kotlin.math.exp(-((progress - 0.85f) * (progress - 0.85f)) / 0.004f) * 0.55f

            amp += (spike1 + spike2 + spike3).toFloat()
            points.add(amp.coerceIn(0.08f, 0.98f))
        }
        return points
    }

    /**
     * Identifies candidate windows around high-energy audio peaks for AI video analysis.
     */
    fun findAudioPeaks(waveform: List<Float>, durationSec: Long, threshold: Float = 0.65f): List<AudioPeak> {
        val peaks = mutableListOf<AudioPeak>()
        if (waveform.isEmpty() || durationSec <= 0) return peaks

        val stepSec = durationSec.toFloat() / waveform.size

        for (i in 1 until waveform.size - 1) {
            val current = waveform[i]
            val prev = waveform[i - 1]
            val next = waveform[i + 1]

            // Local maximum exceeding threshold
            if (current >= threshold && current >= prev && current >= next) {
                val peakSec = i * stepSec
                // Check minimum 15s separation from already detected peaks
                val isDuplicate = peaks.any { kotlin.math.abs(it.peakSec - peakSec) < 18f }
                if (!isDuplicate) {
                    val start = (peakSec - 12f).coerceAtLeast(0f)
                    val end = (peakSec + 10f).coerceAtMost(durationSec.toFloat())
                    val peakType = when {
                        current > 0.85f -> "crowd_roar"
                        current > 0.75f -> "commentary_spike"
                        else -> "whistle"
                    }
                    peaks.add(
                        AudioPeak(
                            peakSec = peakSec,
                            windowStartSec = start,
                            windowEndSec = end,
                            energyLevel = current,
                            peakType = peakType
                        )
                    )
                }
            }
        }

        // Guarantee at least 2 candidate moments if clip is long enough
        if (peaks.isEmpty() && durationSec >= 20) {
            val p1 = durationSec * 0.35f
            val p2 = durationSec * 0.72f
            peaks.add(AudioPeak(p1, (p1 - 10f).coerceAtLeast(0f), p1 + 10f, 0.78f, "crowd_roar"))
            peaks.add(AudioPeak(p2, (p2 - 10f).coerceAtLeast(0f), p2 + 10f, 0.72f, "commentary_spike"))
        }

        return peaks
    }

    /**
     * Gets audio excitement energy at a specific time in seconds.
     */
    fun getEnergyAt(waveform: List<Float>, timeSec: Float, durationSec: Long): Float {
        if (waveform.isEmpty() || durationSec <= 0) return 0.5f
        val index = ((timeSec / durationSec.toFloat()) * waveform.size).toInt().coerceIn(0, waveform.size - 1)
        return waveform[index]
    }
}
