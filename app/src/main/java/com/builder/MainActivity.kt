package com.builder

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.builder.screens.CameraPreview
import com.builder.utils.*
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLoc: android.location.Location? = null

    private val locCallback = object : LocationCallback() {
        override fun onLocationResult(res: LocationResult) { currentLoc = res.lastLocation }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) startLocUpdates()
        }
        
        if (hasPerms()) startLocUpdates() else launcher.launch(PERMS)

        setContent {
            var preview by remember { mutableStateOf<Bitmap?>(null) }
            var isHighQuality by remember { mutableStateOf(true) }
            var showFlash by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }
            
            // Watermark States
            var options by remember { mutableStateOf(WatermarkOptions(true, true, true, true, "")) }

            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    if (preview != null) {
                        Image(bitmap = preview!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        IconButton(onClick = { preview = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    } else {
                        val controller = remember { LifecycleCameraController(applicationContext).apply { setEnabledUseCases(CameraController.IMAGE_CAPTURE) } }
                        CameraPreview(controller, Modifier.fillMaxSize())
                        
                        // Flash Overlay
                        val alpha by animateFloatAsState(if (showFlash) 1f else 0f, tween(100), finishedListener = { showFlash = false })
                        Box(Modifier.fillMaxSize().alpha(alpha).background(Color.White))

                        // UI Controls
                        SmallFloatingActionButton(onClick = { showSettings = true }, Modifier.align(Alignment.TopStart).padding(16.dp), containerColor = Color.Black.copy(0.5f), contentColor = Color.White) {
                            Icon(Icons.Default.Settings, null)
                        }

                        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 32.dp), Arrangement.SpaceAround, Alignment.CenterVertically) {
                            IconButton(onClick = { controller.cameraSelector = if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA }) {
                                Icon(Icons.Default.Cameraswitch, null, tint = Color.White)
                            }
                            IconButton(onClick = { 
                                showFlash = true
                                takePhoto(controller, isHighQuality, options) { preview = it }
                            }, Modifier.size(80.dp)) {
                                Icon(Icons.Default.Circle, null, tint = Color.White, modifier = Modifier.size(80.dp))
                            }
                            IconButton(onClick = { startActivity(Intent(Intent.ACTION_VIEW).apply { type = "image/*"; flags = Intent.FLAG_ACTIVITY_NEW_TASK }) }) {
                                Icon(Icons.Default.PhotoLibrary, null, tint = Color.White)
                            }
                        }
                    }

                    if (showSettings) {
                        ModalBottomSheet(onDismissRequest = { showSettings = false }, containerColor = Color(0xFF1A1A1A), contentColor = Color.White) {
                            SettingsSheet(isHighQuality, options, { isHighQuality = it }, { options = it })
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SettingsSheet(hq: Boolean, opt: WatermarkOptions, onHq: (Boolean) -> Unit, onOpt: (WatermarkOptions) -> Unit) {
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Settings", fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("High Quality"); Switch(hq, onHq, Modifier.scale(0.8f))
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.Gray)
            SettingToggle("Show Time", opt.showTime) { onOpt(opt.copy(showTime = it)) }
            SettingToggle("Show Date", opt.showDate) { onOpt(opt.copy(showDate = it)) }
            SettingToggle("Show Coords", opt.showCoords) { onOpt(opt.copy(showCoords = it)) }
            SettingToggle("Show Address", opt.showAddress) { onOpt(opt.copy(showAddress = it)) }
            OutlinedTextField(opt.customText, { onOpt(opt.copy(customText = it)) }, label = { Text("Custom Text") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Spacer(Modifier.height(40.dp))
        }
    }

    @Composable
    fun SettingToggle(l: String, v: Boolean, onC: (Boolean) -> Unit) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(l, fontSize = 14.sp); Switch(v, onC, Modifier.scale(0.7f)) }
    }

    private fun takePhoto(c: LifecycleCameraController, hq: Boolean, opt: WatermarkOptions, onRes: (Bitmap) -> Unit) {
        c.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(img: ImageProxy) {
                var b = img.toBitmap()
                val m = Matrix().apply { postRotate(img.imageInfo.rotationDegrees.toFloat()) }
                b = Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
                
                val addr = if (opt.showAddress) LocationHelper.getAddress(this@MainActivity, currentLoc) else ""
                val wm = WatermarkManager.apply(b, currentLoc, addr, opt)
                
                save(wm, hq)
                onRes(wm)
                img.close()
            }
            override fun onError(e: ImageCaptureException) { Log.e("Err", "$e") }
        })
    }

    private fun save(b: Bitmap, hq: Boolean) {
        val q = if (hq) 100 else 50
        val cv = ContentValues().apply { 
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CamRU")
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)?.let {
            contentResolver.openOutputStream(it).use { s -> b.compress(Bitmap.CompressFormat.JPEG, q, s!!) }
        }
    }

    private fun startLocUpdates() {
        if (ActivityCompat.checkSelfPermission(this, PERMS[1]) != 0) return
        fusedLocationClient.requestLocationUpdates(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build(), locCallback, Looper.getMainLooper())
    }
    private fun hasPerms() = PERMS.all { ContextCompat.checkSelfPermission(this, it) == 0 }
    companion object { val PERMS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION) }
}
