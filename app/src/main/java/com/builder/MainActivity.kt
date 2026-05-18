package com.builder

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.builder.screens.CameraPreview
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentGPSLocation: Location? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentGPSLocation = locationResult.lastLocation
            }
        }

        if (!hasRequiredPermissions()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            startLocationUpdates()
        }
        
        setContent {
            var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var isWatermarkEnabled by remember { mutableStateOf(true) }
            var isHighQuality by remember { mutableStateOf(true) }
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
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pengaturan UI (Watermark & Kualitas)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Kualitas Toggle
                            Text(if (isHighQuality) "HIGH" else "LOW", color = Color.White, fontSize = 12.sp)
                            Switch(
                                checked = isHighQuality,
                                onCheckedChange = { isHighQuality = it },
                                modifier = Modifier.scale(0.7f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            // Watermark Toggle
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
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
                                    captureAndProcess(controller, isWatermarkEnabled, isHighQuality) { bitmap ->
                                        previewBitmap = bitmap
                                    }
                                },
                                modifier = Modifier.size(80.dp)
                            ) {
                                Icon(Icons.Default.Circle, "Shutter", tint = Color.White, modifier = Modifier.size(80.dp))
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

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun openAndroidGallery() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun captureAndProcess(
        controller: LifecycleCameraController,
        addWatermark: Boolean,
        highQuality: Boolean,
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
                        val address = getAddressFromLoc(currentGPSLocation)
                        val watermarked = applyWatermark(bitmap, currentGPSLocation, address)
                        saveBitmapToGallery(watermarked, highQuality)
                        onProcessed(watermarked)
                    } else {
                        saveBitmapToGallery(bitmap, highQuality)
                        onProcessed(bitmap)
                    }
                    image.close()
                }
                override fun onError(exception: ImageCaptureException) { Log.e("Cam", "Error", exception) }
            }
        )
    }

    private fun getAddressFromLoc(loc: Location?): String {
        if (loc == null) return "Mencari alamat..."
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) // Alamat lengkap
            } else "Alamat tidak ditemukan"
        } catch (e: Exception) {
            "Gagal memuat alamat"
        }
    }

    private fun applyWatermark(source: Bitmap, location: Location?, address: String): Bitmap {
        val result = source.copy(source.config, true)
        val canvas = Canvas(result)
        
        // Setup Paint untuk Text
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = source.width / 32f
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
        }
        
        val timeStamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val coords = if (location != null) "${location.latitude}, ${location.longitude}" else "GPS Locking..."
        
        val margin = 60f
        val lineSpacing = 1.4f
        
        // Menggambar text (Tanpa Cam RU)
        canvas.drawText(timeStamp, margin, source.height - (margin * 3.5f), paint)
        canvas.drawText(coords, margin, source.height - (margin * 2.2f), paint)
        
        // Bungkus alamat agar tidak terlalu panjang ke samping
        val addressPaint = Paint(paint).apply { textSize = source.width / 38f }
        val maxWidth = source.width - (margin * 2)
        drawMultilineText(canvas, address, margin, source.height - margin, addressPaint, maxWidth.toInt())
        
        return result
    }

    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Int) {
        val words = text.split(" ")
        var line = ""
        var currentY = y
        
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) < maxWidth) {
                line = testLine
            } else {
                canvas.drawText(line, x, currentY - paint.textSize, paint)
                line = word
                // currentY -= paint.textSize * 1.2f // Gambar ke atas (Stacking)
            }
        }
        canvas.drawText(line, x, currentY, paint)
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, highQuality: Boolean) {
        val quality = if (highQuality) 100 else 50
        val name = "IMG_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CamRU")
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream!!)
            }
        }
    }

    private fun hasRequiredPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() { super.onResume() ; if (hasRequiredPermissions()) startLocationUpdates() }
    override fun onPause() { super.onPause() ; fusedLocationClient.removeLocationUpdates(locationCallback) }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
