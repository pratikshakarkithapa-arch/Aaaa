package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.GeneratedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageSaver {

    suspend fun saveImageToGallery(
        context: Context,
        image: GeneratedImage
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val bitmap = image.bitmap ?: run {
                if (image.fallbackDrawableRes != null) {
                    BitmapFactory.decodeResource(context.resources, image.fallbackDrawableRes)
                } else null
            } ?: return@withContext Result.failure(Exception("Image data not available"))

            val filename = "AI_Art_${System.currentTimeMillis()}_${image.style.name.lowercase()}.jpg"
            val resolver = context.contentResolver

            val imageUri: Uri?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AI_Generator")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                imageUri = resolver.insert(collection, contentValues)

                if (imageUri != null) {
                    resolver.openOutputStream(imageUri)?.use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "AI_Generator").apply { mkdirs() }
                val imageFile = File(appDir, filename)
                FileOutputStream(imageFile).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                }
                val contentValues = ContentValues().apply {
                    @Suppress("DEPRECATION")
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                @Suppress("DEPRECATION")
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }

            if (imageUri != null) {
                Result.success(imageUri)
            } else {
                Result.failure(Exception("Failed to create MediaStore entry"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareImage(context: Context, image: GeneratedImage) = withContext(Dispatchers.IO) {
        try {
            val bitmap = image.bitmap ?: run {
                if (image.fallbackDrawableRes != null) {
                    BitmapFactory.decodeResource(context.resources, image.fallbackDrawableRes)
                } else null
            } ?: return@withContext

            val cachePath = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val file = File(cachePath, "ai_artwork_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            val contentUri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "✨ Created with AI Image Generator\n\nPrompt: \"${image.prompt}\"\nStyle: ${image.style.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                val chooser = Intent.createChooser(shareIntent, "Share Generated Image")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
