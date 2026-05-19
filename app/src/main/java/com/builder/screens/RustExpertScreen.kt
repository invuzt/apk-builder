package com.builder.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RustExpertScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("camru_prefs", Context.MODE_PRIVATE) }

    var useRustHdr by remember { mutableStateOf(prefs.getBoolean("rust_hdr", false)) }
    var useRustLossless by remember { mutableStateOf(prefs.getBoolean("rust_lossless", false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rust Engine Options", color = Color.Cyan) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212),
        contentColor = Color.White
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Vivid HDR Engine")
                    Text("Kalkulasi warna kontras tinggi tingkat native", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(useRustHdr, { useRustHdr = it; prefs.edit().putBoolean("rust_hdr", it).apply() }, Modifier.scale(0.8f))
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Ultra Lossless Compression")
                    Text("Simpan gambar dalam enkapsulasi WebP tajam", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(useRustLossless, { useRustLossless = it; prefs.edit().putBoolean("rust_lossless", it).apply() }, Modifier.scale(0.8f))
            }
        }
    }
}
