package com.builder

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.builder.screens.CameraScreen
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
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) startLocUpdates()
        }
        
        if (hasPerms()) startLocUpdates() else launcher.launch(PERMS)

        setContent {
            MaterialTheme {
                CameraScreen(
                    currentLoc = currentLoc,
                    onOpenGallery = { openGallery() }
                )
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun startLocUpdates() {
        if (ActivityCompat.checkSelfPermission(this, PERMS[1]) != 0) return
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocationClient.requestLocationUpdates(req, locCallback, Looper.getMainLooper())
    }

    private fun hasPerms() = PERMS.all { ContextCompat.checkSelfPermission(this, it) == 0 }

    companion object { 
        val PERMS = arrayOf(
            Manifest.permission.CAMERA, 
            Manifest.permission.ACCESS_FINE_LOCATION, 
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) 
    }
}
