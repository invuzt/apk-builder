package com.builder

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.builder.screens.CameraPreview
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 0)
        }

        setContent {
            var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var isWatermarkEnabled by remember { mutableStateOf(true) }
            var showFlash by remember { mutableStateOf(false) }
            
            val flashAlpha by animateFloatAsState(
                targetValue = if (showFlash) 1f else 0f,
                animationSpec = tween(durationMillis = 100),
                finishedListener = { showFlash = false }
            )

            val controller = remember {
                LifecycleCameraController(applicationContext).apply {
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                }
            }

            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { previewBitmap = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
                    }

                    Box(modifier = Modifier.fillMaxSize().alpha(flashAlpha).background(Color.White))

                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Watermark GPS", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isWatermarkEnabled,
                                onCheckedChange = { isWatermarkEnabled = it },
                                modifier = Modifier.scale(0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                controller.cameraSelector = if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                    CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                            }) {
                                Icon(Icons.Default.Cameraswitch, "Switch", tint = Color.White)
                            }

                            IconButton(
                                onClick = {
                                    showFlash = true
                                    captureAndProcess(controller, isWatermarkEnabled) { bitmap ->
                                        previewBitmap = bitmap
                                    }
                                },
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(Icons.Default.Circle, "Shutter", tint = Color.White, modifier = Modifier.size(72.dp))
                            }

                            IconButton(onClick = { openAndroidGallery() }) {
                                Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openAndroidGallery() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka galeri", Toast.LENGTH_SHORT).show()
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
                    val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    if (addWatermark) {
                        getLastLocation { location ->
                            val watermarked = applyWatermark(bitmap, location)
                            saveBitmapToGallery(watermarked)
                            onProcessed(watermarked)
                        }
                    } else {
                        saveBitmapToGallery(bitmap)
                        onProcessed(bitmap)
                    }
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Gagal mengambil foto", exception)
                }
            }
        )
    }

    private fun getLastLocation(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback(null)
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { loc: Location? -> callback(loc) }
            .addOnFailureListener { callback(null) }
    }

    private fun applyWatermark(source: Bitmap, location: Location?): Bitmap {
        val result = source.copy(source.config, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = source.width / 28f
            isAntiAlias = true
            setShadowLayer(3f, 2f, 2f, android.graphics.Color.BLACK)
        }
        
        val timeStamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val locText = if (location != null) "Lat: ${location.latitude}, Lon: ${location.longitude}" else "GPS Unavailable"
        
        val margin = 50f
        canvas.drawText("Vuzt Cam | $timeStamp", margin, source.height - (margin * 2.5f), paint)
        canvas.drawText(locText, margin, source.height - margin, paint)
        
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

    private fun hasRequiredPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
