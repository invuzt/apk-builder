package com.builder

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.builder.screens.CameraScreen
import com.builder.screens.SettingsScreen
import com.builder.utils.PermissionHandler
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLoc by mutableStateOf<Location?>(null)

    private val locCallback = object : LocationCallback() {
        override fun onLocationResult(res: LocationResult) { 
            res.lastLocation?.let { currentLoc = it }
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
                
                var currentScreen by remember { mutableStateOf("camera") }

                when (currentScreen) {
                    "camera" -> {
                        CameraScreen(
                            controller = cameraController,
                            currentLoc = currentLoc,
                            onOpenGallery = { openGallery() },
                            onNavigateToSettings = { currentScreen = "settings" }
                        )
                    }
                    "settings" -> {
                        SettingsScreen(
                            onBack = { currentScreen = "camera" }
                        )
                    }
                }
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
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc -> if (loc != null) currentLoc = loc }

        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()
            
        fusedLocationClient.requestLocationUpdates(req, locCallback, Looper.getMainLooper())
    }
}
