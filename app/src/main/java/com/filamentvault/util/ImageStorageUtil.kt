package com.filamentvault.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageStorageUtil {

    private const val IMAGE_DIR = "filament_images"

    fun saveImage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, IMAGE_DIR).apply { mkdirs() }
            val fileName = "filament_${UUID.randomUUID()}.jpg"
            val destFile = File(dir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteImage(imagePath: String?) {
        if (imagePath == null) return
        try {
            File(imagePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCameraOutputFile(context: Context): File {
        val dir = File(context.cacheDir, "camera_cache").apply { mkdirs() }
        return File(dir, "camera_${UUID.randomUUID()}.jpg")
    }
}
