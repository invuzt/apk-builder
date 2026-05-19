package com.builder.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToWatermark: () -> Unit,
    onNavigateToRust: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("camru_prefs", Context.MODE_PRIVATE) }
    var isPremium by remember { mutableStateOf(prefs.getBoolean("is_premium", false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Main Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212),
        contentColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Lisensi Aplikasi", fontSize = 14.sp, color = Color.Gray)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Aktivasi Fitur Pro (Premium)", fontWeight = FontWeight.Bold)
                Switch(isPremium, { 
                    isPremium = it
                    prefs.edit().putBoolean("is_premium", it).apply()
                }, Modifier.scale(0.8f))
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.DarkGray)

            // Navigasi ke Sub-Menu Watermark
            Row(
                Modifier.fillMaxWidth().clickable { onNavigateToWatermark() }.padding(vertical = 16.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text("Configuration Watermark", fontWeight = FontWeight.Medium)
                    Text("Atur jam, tanggal, koordinat, dan alamat", fontSize = 12.sp, color = Color.Gray)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
            }

            // Navigasi ke Sub-Menu Rust Expert
            Row(
                Modifier.fillMaxWidth().clickable { onNavigateToRust() }.padding(vertical = 16.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text("Rust Engine Expert Options", fontWeight = FontWeight.Medium, color = if(isPremium) Color.Cyan else Color.Gray)
                    Text("Vivid HDR, Ultra Lossless Compression", fontSize = 12.sp, color = Color.Gray)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
            }
        }
    }
}
