package com.builder

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
            res.lastLocation?.let {
                currentLoc = it
                Log.d("GPS_SUCCESS", "Koordinat didapat: ${it.latitude}, ${it.longitude}")
            }
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startLocUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != 0) return
        
        // Taktik 1: Paksa minta lokasi instan saat ini juga (Fresh Location)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) currentLoc = loc
            }

        // Taktik 2: Minta pembaruan berkala secara agresif
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000) // Cek tiap 2 detik
            .setMinUpdateIntervalMillis(1000)
            .setWaveformPowerUsage(LocationRequest.POWER_USAGE_HIGH) // Gunakan performa penuh GPS hardware
            .build()
            
        fusedLocationClient.requestLocationUpdates(req, locCallback, Looper.getMainLooper())
    }
}
