package com.builder.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionHandler {
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA, 
        Manifest.permission.ACCESS_FINE_LOCATION, 
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
