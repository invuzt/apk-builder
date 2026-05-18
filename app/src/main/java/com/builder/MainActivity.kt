package com.builder

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builder.screens.CameraPreview
import com.builder.screens.PhotoBottomSheetContent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }

        setContent {
            val scope = rememberCoroutineScope()
            val scaffoldState = rememberBottomSheetScaffoldState()
            val controller = remember {
                LifecycleCameraController(applicationContext).apply {
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                }
            }

            val viewModel = viewModel<MainViewModel>()
            val bitmaps by viewModel.bitmaps.collectAsState()

            MaterialTheme {
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomSheetContent(
                            bitmaps = bitmaps,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())

                        IconButton(
                            onClick = {
                                controller.cameraSelector = 
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else CameraSelector.DEFAULT_BACK_CAMERA
                            },
                            modifier = Modifier.offset(16.dp, 16.dp)
                        ) {
                            Icon(Icons.Default.Cameraswitch, "Switch", tint = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 48.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            IconButton(onClick = { scope.launch { scaffoldState.bottomSheetState.expand() } }) {
                                Icon(Icons.Default.Photo, "Gallery", tint = Color.White)
                            }

                            IconButton(
                                onClick = { savePhotoToGallery(controller, viewModel) },
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, "Capture", tint = Color.White, modifier = Modifier.size(54.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun savePhotoToGallery(
        controller: LifecycleCameraController,
        viewModel: MainViewModel
    ) {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Photos")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        controller.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, "Foto tersimpan ke Galeri!", Toast.LENGTH_SHORT).show()
                    // Opsional: Anda tetap bisa mengambil bitmap untuk preview di BottomSheet jika mau
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("Camera", "Gagal menyimpan: ${exc.message}", exc)
                }
            }
        )
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).filter { 
            // WRITE_EXTERNAL_STORAGE tidak lagi diperlukan di Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it != Manifest.permission.WRITE_EXTERNAL_STORAGE else true
        }.toTypedArray()
    }
}
