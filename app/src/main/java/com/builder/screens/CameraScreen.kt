package com.builder.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.builder.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    controller: LifecycleCameraController,
    currentLoc: android.location.Location?,
    onOpenGallery: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("camru_prefs", Context.MODE_PRIVATE) }
    
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var showFlash by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    var isHighQuality by remember { mutableStateOf(prefs.getBoolean("hq", true)) }
    
    // Status Lisensi Premium
    var isPremium by remember { mutableStateOf(prefs.getBoolean("is_premium", false)) }
    
    var options by remember {
        mutableStateOf(
            WatermarkOptions(
                showTime = prefs.getBoolean("w_time", true),
                showDate = prefs.getBoolean("w_date", true),
                showCoords = prefs.getBoolean("w_coords", true),
                showAddress = prefs.getBoolean("w_addr", true),
                customText = prefs.getString("w_custom", "") ?: "",
                removeBrand = prefs.getBoolean("w_remove_brand", false)
            )
        )
    }

    val currentAddress by produceState(initialValue = "", currentLoc) {
        value = if (options.showAddress) LocationHelper.getAddress(context, currentLoc) else ""
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (preview != null) {
            Image(bitmap = preview!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            IconButton(onClick = { preview = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        } else {
            CameraPreview(controller, Modifier.fillMaxSize())
            
            val alpha by animateFloatAsState(targetValue = if (showFlash) 1f else 0f, animationSpec = tween(100), finishedListener = { showFlash = false })
            Box(Modifier.fillMaxSize().alpha(alpha).background(Color.White))

            SmallFloatingActionButton(
                onClick = { showSettings = true },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                containerColor = Color.Black.copy(0.5f),
                contentColor = Color.White
            ) { Icon(Icons.Default.Settings, null) }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    controller.cameraSelector = if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                }) { Icon(Icons.Default.Cameraswitch, null, tint = Color.White) }
                
                IconButton(
                    onClick = { 
                        showFlash = true
                        takePhoto(context, controller, isHighQuality, options, currentLoc, currentAddress) { preview = it }
                    },
                    modifier = Modifier.size(80.dp)
                ) { Icon(Icons.Default.Circle, null, tint = Color.White, modifier = Modifier.size(80.dp)) }
                
                IconButton(onOpenGallery) { Icon(Icons.Default.PhotoLibrary, null, tint = Color.White) }
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                containerColor = Color(0xFF121212),
                contentColor = Color.White
            ) {
                SettingsSheet(
                    hq = isHighQuality,
                    premium = isPremium,
                    opt = options,
                    onHq = { isHighQuality = it; prefs.edit().putBoolean("hq", it).apply() },
                    onPremiumChange = { active ->
                        isPremium = active
                        prefs.edit().putBoolean("is_premium", active).apply()
                        // Jika premium mati, paksa watermark brand muncul lagi
                        if (!active) {
                            options = options.copy(removeBrand = false)
                            prefs.edit().putBoolean("w_remove_brand", false).apply()
                        }
                    },
                    onOpt = { newOpt ->
                        options = newOpt
                        prefs.edit().apply {
                            putBoolean("w_time", newOpt.showTime); putBoolean("w_date", newOpt.showDate)
                            putBoolean("w_coords", newOpt.showCoords); putBoolean("w_addr", newOpt.showAddress)
                            putString("w_custom", newOpt.customText)
                            putBoolean("w_remove_brand", newOpt.removeBrand)
                        }.apply()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSheet(
    hq: Boolean, 
    premium: Boolean, 
    opt: WatermarkOptions, 
    onHq: (Boolean) -> Unit, 
    onPremiumChange: (Boolean) -> Unit,
    onOpt: (WatermarkOptions) -> Unit
) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Watermark Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("High Quality Photo (Slow Save)"); Switch(hq, onHq, Modifier.scale(0.8f))
        }
        
        // FITUR LISENSI: Simulasi Aktivasi Premium/Beli Lisensi
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Aktivasi Lisensi Pro (Premium)", color = if(premium) Color.Yellow else Color.White)
            Switch(premium, onPremiumChange, Modifier.scale(0.8f))
        }
        
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        
        SettingToggle("Tampilkan Jam", opt.showTime) { onOpt(opt.copy(showTime = it)) }
        SettingToggle("Tampilkan Hari & Tanggal", opt.showDate) { onOpt(opt.copy(showDate = it)) }
        SettingToggle("Tampilkan Koordinat", opt.showCoords) { onOpt(opt.copy(showCoords = it)) }
        SettingToggle("Tampilkan Alamat", opt.showAddress) { onOpt(opt.copy(showAddress = it)) }
        
        // SWITCH PREMIUM: Hanya bisa diubah jika status Premium aktif
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                text = "Hilangkan Watermark 'Shot by CakRu'", 
                fontSize = 14.sp,
                color = if (premium) Color.White else Color.Gray
            )
            Switch(
                checked = opt.removeBrand, 
                onCheckedChange = { if (premium) onOpt(opt.copy(removeBrand = it)) },
                enabled = premium,
                modifier = Modifier.scale(0.7f)
            )
        }
        
        OutlinedTextField(
            value = opt.customText,
            onValueChange = { onOpt(opt.copy(customText = it)) },
            label = { Text("Teks Kustom") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Spacer(Modifier.height(24.dp))
        Text("About", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Developer: CakRu", color = Color.Yellow, fontWeight = FontWeight.Bold)
                Text("License: Open Source (MIT)", fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("Dependencies:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("• Jetpack Compose (UI)\n• CameraX (Engine)\n• Google Play Services (GPS)\n• Kotlin Coroutines (Async)", fontSize = 11.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingToggle(l: String, v: Boolean, onC: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(l, fontSize = 14.sp); Switch(v, onC, Modifier.scale(0.7f))
    }
}

private fun takePhoto(
    context: Context,
    c: LifecycleCameraController,
    hq: Boolean,
    opt: WatermarkOptions,
    currentLoc: android.location.Location?,
    currentAddr: String,
    onRes: (Bitmap) -> Unit
) {
    c.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(img: ImageProxy) {
            val b = img.toBitmap().let { src ->
                val matrix = Matrix().apply { postRotate(img.imageInfo.rotationDegrees.toFloat()) }
                Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            }
            
            val wm = WatermarkManager.apply(b, currentLoc, currentAddr, opt)
            FileManager.saveImageToGallery(context, wm, hq)
            onRes(wm)
            img.close()
        }
        override fun onError(e: ImageCaptureException) { Log.e("Err", "$e") }
    })
}
