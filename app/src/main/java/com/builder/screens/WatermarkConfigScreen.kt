package com.builder.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun WatermarkConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("camru_prefs", Context.MODE_PRIVATE) }

    var showTime by remember { mutableStateOf(prefs.getBoolean("w_time", true)) }
    var showDate by remember { mutableStateOf(prefs.getBoolean("w_date", true)) }
    var showCoords by remember { mutableStateOf(prefs.getBoolean("w_coords", true)) }
    var showAddr by remember { mutableStateOf(prefs.getBoolean("w_addr", true)) }
    var customText by remember { mutableStateOf(prefs.getString("w_custom", "") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watermark Config", color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212),
        contentColor = Color.White
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tampilkan Jam")
                Switch(showTime, { showTime = it; prefs.edit().putBoolean("w_time", it).apply() }, Modifier.scale(0.7f))
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tampilkan Hari & Tanggal")
                Switch(showDate, { showDate = it; prefs.edit().putBoolean("w_date", it).apply() }, Modifier.scale(0.7f))
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tampilkan Koordinat")
                Switch(showCoords, { showCoords = it; prefs.edit().putBoolean("w_coords", it).apply() }, Modifier.scale(0.7f))
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tampilkan Alamat")
                Switch(showAddr, { showAddr = it; prefs.edit().putBoolean("w_addr", it).apply() }, Modifier.scale(0.7f))
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it; prefs.edit().putString("w_custom", it).apply() },
                label = { Text("Teks Kustom Watermark", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow, unfocusedBorderColor = Color.Gray)
            )
        }
    }
}
