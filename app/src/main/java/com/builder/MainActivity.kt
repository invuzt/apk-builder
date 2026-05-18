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
        override fun onLocationResult(res: LocationResult) { currentLoc = res.lastLocation }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) startLocUpdates()
        }
        
        if (PermissionHandler.hasAllPermissions(this)) startLocUpdates() else launcher.launch(PermissionHandler.REQUIRED_PERMISSIONS)

        setContent {
            MaterialTheme {
                // Perbaikan Performa: Inisialisasi controller di sini agar terisolasi dari Lag state UI
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
        }
        )
    }

    private fun startLocUpdates() {
        if (ActivityCompat.checkSelfPermission(this, PermissionHandler.REQUIRED_PERMISSIONS[1]) != 0) return
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocationClient.requestLocationUpdates(req, locCallback, Looper.getMainLooper())
    }
}
