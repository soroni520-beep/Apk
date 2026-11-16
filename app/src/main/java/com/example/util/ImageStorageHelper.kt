package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageStorageHelper {

  suspend fun copyAndOptimizeImage(context: Context, sourceUri: Uri): String? =
    withContext(Dispatchers.IO) {
      runCatching {
          val uploadDir = File(context.filesDir, "uploads").apply { mkdirs() }
          val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
          context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
          }

          var sampleSize = 1
          val maxDim = 960
          while (
            bounds.outWidth / sampleSize > maxDim || bounds.outHeight / sampleSize > maxDim
          ) {
            sampleSize *= 2
          }

          val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
          val bitmap =
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
              BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@runCatching null

          val destFile = File(uploadDir, "img_${UUID.randomUUID()}.jpg")
          FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 84, out)
          }
          bitmap.recycle()
          destFile.absolutePath
        }
        .getOrNull()
    }

  suspend fun copyVideoToInternalStorage(context: Context, sourceUri: Uri): String? =
    withContext(Dispatchers.IO) {
      runCatching {
          val mediaDir = File(context.filesDir, "media").apply { mkdirs() }
          // Clean previous tutorial videos to keep app storage lean
          mediaDir.listFiles()?.forEach { it.delete() }
          val destFile = File(mediaDir, "tutorial_${System.currentTimeMillis()}.mp4")
          context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output) }
          } ?: return@runCatching null
          destFile.absolutePath
        }
        .getOrNull()
    }

  suspend fun loadBitmapFromPath(path: String?): ImageBitmap? =
    withContext(Dispatchers.IO) {
      if (path.isNullOrBlank()) return@withContext null
      val file = File(path)
      if (!file.exists()) return@withContext null
      runCatching { BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() }.getOrNull()
    }
}

@Composable
fun rememberLocalImageBitmap(path: String?): ImageBitmap? {
  val bitmap by
    produceState<ImageBitmap?>(initialValue = null, key1 = path) {
      value = ImageStorageHelper.loadBitmapFromPath(path)
    }
  return bitmap
}
