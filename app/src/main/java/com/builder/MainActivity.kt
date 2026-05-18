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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) startLocationUpdates()
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
            var isHighQuality by remember { mutableStateOf(true) }
            var showFlash by remember { mutableStateOf(false) }
            
            // Watermark States
            var showTime by remember { mutableStateOf(true) }
            var showDate by remember { mutableStateOf(true) }
            var showCoords by remember { mutableStateOf(true) }
            var showAddress by remember { mutableStateOf(true) }
            var customText by remember { mutableStateOf("") }
            
            var showSettings by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState()

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

                    // Floating Settings Button
                    SmallFloatingActionButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Settings, "Settings")
                    }

                    // Bottom UI
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                                    captureAndProcess(
                                        controller, 
                                        isHighQuality, 
                                        showTime, showDate, showCoords, showAddress, customText
                                    ) { bitmap -> previewBitmap = bitmap }
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

                    // Settings Bottom Sheet
                    if (showSettings) {
                        ModalBottomSheet(
                            onDismissRequest = { showSettings = false },
                            sheetState = sheetState,
                            containerColor = Color(0xFF1A1A1A),
                            contentColor = Color.White
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text("Watermark Settings", fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
                                
                                SettingItem("Kualitas Tinggi (High)", isHighQuality) { isHighQuality = it }
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                                SettingItem("Tampilkan Waktu (Jam)", showTime) { showTime = it }
                                SettingItem("Tampilkan Hari & Tanggal", showDate) { showDate = it }
                                SettingItem("Tampilkan Koordinat Lat/Long", showCoords) { showCoords = it }
                                SettingItem("Tampilkan Alamat Lengkap", showAddress) { showAddress = it }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = customText,
                                    onValueChange = { customText = it },
                                    label = { Text("Teks Kustom (Opsional)", color = Color.LightGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SettingItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp)
            Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.scale(0.8f))
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun openAndroidGallery() {
        val intent = Intent(Intent.ACTION_VIEW).apply { type = "image/*" ; flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        startActivity(intent)
    }

    private fun captureAndProcess(
        controller: LifecycleCameraController,
        highQuality: Boolean,
        sTime: Boolean, sDate: Boolean, sCoords: Boolean, sAddress: Boolean, cText: String,
        onProcessed: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    var bitmap = image.toBitmap()
                    val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    val address = if (sAddress) getAddressFromLoc(currentGPSLocation) else ""
                    val watermarked = applyWatermark(bitmap, currentGPSLocation, address, sTime, sDate, sCoords, sAddress, cText)
                    
                    saveBitmapToGallery(watermarked, highQuality)
                    onProcessed(watermarked)
                    image.close()
                }
                override fun onError(exception: ImageCaptureException) { Log.e("Cam", "Error", exception) }
            }
        )
    }

    private fun getAddressFromLoc(loc: Location?): String {
        if (loc == null) return "Searching address..."
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            addresses?.get(0)?.getAddressLine(0) ?: "Address not found"
        } catch (e: Exception) { "Signal weak for address" }
    }

    private fun applyWatermark(
        source: Bitmap, location: Location?, address: String,
        sTime: Boolean, sDate: Boolean, sCoords: Boolean, sAddress: Boolean, cText: String
    ): Bitmap {
        val result = source.copy(source.config, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = source.width / 32f
            isAntiAlias = true
            setShadowLayer(5f, 2f, 2f, android.graphics.Color.BLACK)
        }
        
        val margin = 60f
        var currentY = source.height - margin
        
        // Render dari Bawah ke Atas (Stacking) untuk mencegah tumpang tindih
        
        // 1. Alamat (Paling Bawah)
        if (sAddress && address.isNotEmpty()) {
            val addrPaint = Paint(paint).apply { textSize = source.width / 40f }
            currentY = drawMultilineText(canvas, address, margin, currentY, addrPaint, (source.width - margin*2).toInt())
            currentY -= 20f
        }
        
        // 2. Koordinat
        if (sCoords && location != null) {
            canvas.drawText("${location.latitude}, ${location.longitude}", margin, currentY, paint)
            currentY -= paint.textSize * 1.3f
        }
        
        // 3. Waktu & Tanggal
        val timeStr = if (sTime) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) else ""
        val dateStr = if (sDate) SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()) else ""
        val combinedDateTime = listOf(dateStr, timeStr).filter { it.isNotEmpty() }.joinToString(" | ")
        
        if (combinedDateTime.isNotEmpty()) {
            canvas.drawText(combinedDateTime, margin, currentY, paint)
            currentY -= paint.textSize * 1.3f
        }
        
        // 4. Custom Text (Paling Atas)
        if (cText.isNotEmpty()) {
            val customPaint = Paint(paint).apply { 
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.YELLOW // Warna beda untuk custom text
            }
            canvas.drawText(cText.uppercase(), margin, currentY, customPaint)
        }
        
        return result
    }

    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Int): Float {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) < maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        lines.add(currentLine)
        
        var tempY = y
        for (i in lines.indices.reversed()) {
            canvas.drawText(lines[i], x, tempY, paint)
            if (i != 0) tempY -= paint.textSize * 1.2f
        }
        return tempY - paint.textSize // Mengembalikan posisi Y terakhir untuk elemen berikutnya
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, highQuality: Boolean) {
        val quality = if (highQuality) 100 else 50
        val name = "IMG_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CamRU")
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

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}
