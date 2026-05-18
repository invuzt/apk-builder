package com.builder.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore

object FileManager {
    fun saveImageToGallery(context: Context, bitmap: Bitmap, isHighQuality: Boolean) {
        val quality = if (isHighQuality) 100 else 50
        val values = ContentValues().apply { 
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CamRU")
            }
        }
        
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
            context.contentResolver.openOutputStream(uri).use { stream -> 
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream!!) 
            }
        }
    }
}
