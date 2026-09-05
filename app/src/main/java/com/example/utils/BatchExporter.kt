package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.ClipRenderJob
import com.example.data.local.MomentCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class BatchExportResult(
    val totalCount: Int,
    val successCount: Int,
    val galleryUris: List<Uri>,
    val errorMessage: String? = null
)

object BatchExporter {
    private const val TAG = "BatchExporter"

    /**
     * Exports a list of video files directly to the device's public video Gallery / MediaStore.
     * Works on Android 10+ (Q) using zero-permission MediaStore scope.
     */
    suspend fun exportVideoFilesToGallery(
        context: Context,
        videoFiles: List<File>,
        albumName: String = "Generated Short Clips"
    ): BatchExportResult = withContext(Dispatchers.IO) {
        if (videoFiles.isEmpty()) {
            return@withContext BatchExportResult(0, 0, emptyList(), "No video files found to export.")
        }

        val galleryUris = mutableListOf<Uri>()
        var successCount = 0

        for (file in videoFiles) {
            if (!file.exists() || file.length() == 0L) continue

            try {
                val displayName = "Clip_${System.currentTimeMillis()}_${file.name}"
                val mimeType = "video/mp4"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                    put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/$albumName")
                        put(MediaStore.Video.Media.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                val uri = resolver.insert(collection, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(file).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                    }

                    galleryUris.add(uri)
                    successCount++
                    Log.i(TAG, "Successfully exported clip to Gallery: $uri")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export clip ${file.name} to gallery", e)
            }
        }

        BatchExportResult(
            totalCount = videoFiles.size,
            successCount = successCount,
            galleryUris = galleryUris
        )
    }

    /**
     * Directly shares all generated clip files in a single operation to any installed social media app
     * (Instagram, TikTok, YouTube Shorts, WhatsApp, Telegram, Google Drive, etc.).
     */
    fun shareClipsDirectly(
        context: Context,
        videoFiles: List<File>,
        shareTitle: String = "Share Short Clips",
        shareCaption: String = "Check out these generated short clips! #Shorts #Viral"
    ) {
        val validFiles = videoFiles.filter { it.exists() && it.length() > 0 }
        if (validFiles.isEmpty()) {
            Toast.makeText(context, "No rendered video clips available to share.", Toast.LENGTH_SHORT).show()
            return
        }

        val authority = "${context.packageName}.fileprovider"

        try {
            if (validFiles.size == 1) {
                val file = validFiles.first()
                val contentUri = FileProvider.getUriForFile(context, authority, file)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, shareCaption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, shareTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } else {
                val uriList = ArrayList<Uri>()
                for (file in validFiles) {
                    val contentUri = FileProvider.getUriForFile(context, authority, file)
                    uriList.add(contentUri)
                }

                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "video/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                    putExtra(Intent.EXTRA_TEXT, shareCaption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, shareTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching batch share intent", e)
            Toast.makeText(context, "Error opening share menu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
