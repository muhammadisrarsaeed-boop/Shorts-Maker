package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ClipSettings(
    val logoUri: String? = null,
    val logoPlacement: String = "TOP_RIGHT", // "TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT"
    val defaultWatermark: String = "FOOTBALL AI",
    val numberOfClips: Int = 5, // 1 to 15
    val minClipSec: Float = 10f, // 5s to 30s
    val maxClipSec: Float = 30f, // 15s to 90s
    val backgroundMode916: String = "blurred_fill", // "blurred_fill", "stadium_pitch", "high_density_dark", "solid_black"
    val backgroundModeOther: String = "blurred_fill", // "blurred_fill", "solid_black", "stadium_pitch"
    val autoCaptionsEnabled: Boolean = true
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("football_clip_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ClipSettings> = _settings.asStateFlow()

    private fun loadSettings(): ClipSettings {
        return ClipSettings(
            logoUri = prefs.getString(KEY_LOGO_URI, null),
            logoPlacement = prefs.getString(KEY_LOGO_PLACEMENT, "TOP_RIGHT") ?: "TOP_RIGHT",
            defaultWatermark = prefs.getString(KEY_WATERMARK, "FOOTBALL AI") ?: "FOOTBALL AI",
            numberOfClips = prefs.getInt(KEY_NUM_CLIPS, 5).coerceIn(1, 15),
            minClipSec = prefs.getFloat(KEY_MIN_CLIP_SEC, 10f).coerceIn(5f, 30f),
            maxClipSec = prefs.getFloat(KEY_MAX_CLIP_SEC, 30f).coerceIn(15f, 90f),
            backgroundMode916 = prefs.getString(KEY_BG_916, "blurred_fill") ?: "blurred_fill",
            backgroundModeOther = prefs.getString(KEY_BG_OTHER, "blurred_fill") ?: "blurred_fill",
            autoCaptionsEnabled = prefs.getBoolean(KEY_AUTO_CAPTIONS, true)
        )
    }

    fun updateSettings(newSettings: ClipSettings) {
        prefs.edit().apply {
            putString(KEY_LOGO_URI, newSettings.logoUri)
            putString(KEY_LOGO_PLACEMENT, newSettings.logoPlacement)
            putString(KEY_WATERMARK, newSettings.defaultWatermark)
            putInt(KEY_NUM_CLIPS, newSettings.numberOfClips)
            putFloat(KEY_MIN_CLIP_SEC, newSettings.minClipSec)
            putFloat(KEY_MAX_CLIP_SEC, newSettings.maxClipSec)
            putString(KEY_BG_916, newSettings.backgroundMode916)
            putString(KEY_BG_OTHER, newSettings.backgroundModeOther)
            putBoolean(KEY_AUTO_CAPTIONS, newSettings.autoCaptionsEnabled)
            apply()
        }
        _settings.value = newSettings
    }

    fun updateLogo(uriString: String?) {
        val current = _settings.value
        updateSettings(current.copy(logoUri = uriString))
    }

    fun updateLogoPlacement(placement: String) {
        val current = _settings.value
        updateSettings(current.copy(logoPlacement = placement))
    }

    fun updateNumberOfClips(count: Int) {
        val current = _settings.value
        updateSettings(current.copy(numberOfClips = count.coerceIn(1, 15)))
    }

    fun updateClipDurationBounds(minSec: Float, maxSec: Float) {
        val current = _settings.value
        val safeMin = minSec.coerceIn(5f, 30f)
        val safeMax = maxSec.coerceIn(safeMin, 90f)
        updateSettings(current.copy(minClipSec = safeMin, maxClipSec = safeMax))
    }

    fun updateBackgroundMode916(mode: String) {
        val current = _settings.value
        updateSettings(current.copy(backgroundMode916 = mode))
    }

    fun updateBackgroundModeOther(mode: String) {
        val current = _settings.value
        updateSettings(current.copy(backgroundModeOther = mode))
    }

    companion object {
        private const val KEY_LOGO_URI = "key_logo_uri"
        private const val KEY_LOGO_PLACEMENT = "key_logo_placement"
        private const val KEY_WATERMARK = "key_watermark"
        private const val KEY_NUM_CLIPS = "key_num_clips"
        private const val KEY_MIN_CLIP_SEC = "key_min_clip_sec"
        private const val KEY_MAX_CLIP_SEC = "key_max_clip_sec"
        private const val KEY_BG_916 = "key_bg_916"
        private const val KEY_BG_OTHER = "key_bg_other"
        private const val KEY_AUTO_CAPTIONS = "key_auto_captions"
    }
}
