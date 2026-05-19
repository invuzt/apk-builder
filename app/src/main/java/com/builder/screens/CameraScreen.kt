package com.builder.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.builder.utils.*
import java.io.File

@Composable
fun CameraScreen(
    controller: LifecycleCameraController,
    currentLoc: android.location.Location?,
    onOpenGallery: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("camru_prefs", Context.MODE_PRIVATE) }
    
    val isPremium = prefs.getBoolean("is_premium", false)
    val useRustHdr = prefs.getBoolean("rust_hdr", false) && isPremium
    val useRustLossless = prefs.getBoolean("rust_lossless", false) && isPremium

    var preview by remember { mutableStateOf<Bitmap?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (preview != null) {
            Image(bitmap = preview!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            IconButton(onClick = { preview = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        } else {
            CameraPreview(controller, Modifier.fillMaxSize())

            IconButton(onClick = onNavigateToSettings, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                Icon(Icons.Default.Settings, null, tint = if(isPremium) Color.Yellow else Color.White)
            }

            IconButton(
                onClick = { 
                    takePhoto(context, controller, useRustHdr, useRustLossless) { preview = it }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).size(80.dp)
            ) { Icon(Icons.Default.Circle, null, tint = Color.White, modifier = Modifier.size(80.dp)) }
        }
    }
}

private fun takePhoto(context: Context, c: LifecycleCameraController, useHdr: Boolean, useLossless: Boolean, onRes: (Bitmap) -> Unit) {
    c.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(img: ImageProxy) {
            val rotation = img.imageInfo.rotationDegrees
            val bitmap = img.toBitmap()
            
            // Simpan file sementara di cache internal
            val tempFile = File(context.cacheDir, "temp_capture.jpg")
            tempFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            
            // SIKAT LEWAT RUST: Rust memutar, memberi efek warna, dan mengompres file langsung
            NativeLib.processFileWithRust(tempFile.absolutePath, rotation, useHdr, useLossless)
            
            // Ambil hasil matang dari Rust untuk dimuat ke Preview UI
            val processedBitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
            
            // EKSEKUSI UTAMA: Simpan hasil akhir Rust ke Galeri Publik Android agar permanen
            FileManager.saveImageToGallery(context, processedBitmap, true)
            
            onRes(processedBitmap)
            img.close()
        }
        override fun onError(e: ImageCaptureException) { Log.e("Err", "$e") }
    })
}
