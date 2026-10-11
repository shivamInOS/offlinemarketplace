package com.shivam.offlinemarketplace.core.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object LocalImageStorage {

    suspend fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "marketplace_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val targetFile = File(imagesDir, "img_${UUID.randomUUID()}.jpg")
                val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)

                inputStream?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }

                targetFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
