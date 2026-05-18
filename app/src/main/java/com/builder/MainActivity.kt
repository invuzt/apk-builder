package com.builder

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import com.builder.screens.CameraScreen
import com.builder.utils.PermissionHandler
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLoc: android.location.Location? = null

    private val locCallback = object : LocationCallback() {
        override fun onLocationResult(res: LocationResult) { 
            currentLoc = res.lastLocation 
        }
    }

    // PENGATURAN IZIN YANG BENAR & SINKRON
    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        // Cek apakah izin lokasi diberikan setelah dialog muncul
        val fineLocGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineLocGranted || coarseLocGranted) {
            startLocUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Eksekusi pengecekan izin saat aplikasi dibuka
        if (PermissionHandler.hasAllPermissions(this)) {
            startLocUpdates()
        } else {
            launcher.launch(PermissionHandler.REQUIRED_PERMISSIONS)
        }

        setContent {
            MaterialTheme {
                val cameraController = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                    }
                }
                
                CameraScreen(
                    controller = cameraController,
                    currentLoc = currentLoc,
                    onOpenGallery = { openGallery() }
                )
            }
        }
    }

    private fun openGallery() {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun startLocUpdates() {
        try {
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000) // Update tiap 2 detik jika bergerak
                .build()
                
            fusedLocationClient.requestLocationUpdates(req, locCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            // Berjaga-jaga jika izin dicabut paksa oleh sistem
            e.printStackTrace()
        }
    }
}
