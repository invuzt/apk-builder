package com.builder

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.builder.screens.CameraPreview
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }

        setContent {
            var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var isWatermarkEnabled by remember { mutableStateOf(true) }
            val controller = remember {
                LifecycleCameraController(applicationContext).apply {
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                }
            }

            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    // 1. Layer Utama: Viewfinder atau Freeze Frame
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Tombol Tutup Preview (Kembali ke Kamera)
                        IconButton(
                            onClick = { previewBitmap = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
                    }

                    // 2. Overlay Kontrol
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Toggle Watermark
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f)).padding(8.dp)
                        ) {
                            Text("Watermark", color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isWatermarkEnabled,
                                onCheckedChange = { isWatermarkEnabled = it },
                                modifier = Modifier.scale(0.8f) // Menggunakan Modifier.scale resmi dari Compose
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Switch Cam
                            IconButton(onClick = {
                                controller.cameraSelector = if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                    CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                            }) {
                                Icon(Icons.Default.Cameraswitch, "Switch", tint = Color.White)
                            }

                            // Shutter
                            IconButton(
                                onClick = {
                                    captureAndProcess(controller, isWatermarkEnabled) { bitmap ->
                                        previewBitmap = bitmap
                                    }
                                },
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(Icons.Default.Circle, "Shutter", tint = Color.White, modifier = Modifier.size(72.dp))
                            }

                            // Dummy Gallery
                            IconButton(onClick = { /* Navigasi Galeri */ }) {
                                Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun captureAndProcess(
        controller: LifecycleCameraController,
        addWatermark: Boolean,
        onProcessed: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    var bitmap = image.toBitmap()
                    
                    // Perbaiki Rotasi
                    val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    if (addWatermark) {
                        bitmap = applyWatermark(bitmap)
                    }

                    saveBitmapToGallery(bitmap)
                    onProcessed(bitmap)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Gagal mengambil foto", exception)
                }
            }
        )
    }

    private fun applyWatermark(source: Bitmap): Bitmap {
        val result = source.copy(source.config, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = source.width / 25f
            isAntiAlias = true
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
        }
        val text = "Captured by Vuzt Cam - ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}"
        canvas.drawText(text, 40f, source.height - 40f, paint)
        return result
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val name = "Vuzt_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Vuzt-Camera")
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream!!)
            }
        }
    }

    private fun hasRequiredPermissions() = CAMERAX_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}
