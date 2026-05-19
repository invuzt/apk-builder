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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.builder.utils.WatermarkOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("camru_prefs", Context.MODE_PRIVATE) }
    
    var isPremium by remember { mutableStateOf(prefs.getBoolean("is_premium", false)) }
    var useRustHdr by remember { mutableStateOf(prefs.getBoolean("rust_hdr", false)) }
    var useRustLossless by remember { mutableStateOf(prefs.getBoolean("rust_lossless", false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings Pro", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212),
        contentColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("User Status", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = if(isPremium) "PREMIUM MEMBER (ACTIVE)" else "FREE VERSION",
                color = if(isPremium) Color.Yellow else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            Spacer(Modifier.height(20.dp))
            Text("Eksklusif Mesin Rust (Pro)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
            
            // Fitur HDR Rust
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vivid HDR Engine", color = if(isPremium) Color.White else Color.Gray)
                    Text("Warna cerah ala iPhone (Native Rust)", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(
                    checked = useRustHdr && isPremium,
                    onCheckedChange = { 
                        useRustHdr = it
                        prefs.edit().putBoolean("rust_hdr", it).apply()
                    },
                    enabled = isPremium,
                    modifier = Modifier.scale(0.8f)
                )
            }

            // Fitur Lossless Rust
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ultra Lossless Compression", color = if(isPremium) Color.White else Color.Gray)
                    Text("Tajam maksimal, size minimal (WebP)", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(
                    checked = useRustLossless && isPremium,
                    onCheckedChange = { 
                        useRustLossless = it
                        prefs.edit().putBoolean("rust_lossless", it).apply()
                    },
                    enabled = isPremium,
                    modifier = Modifier.scale(0.8f)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.DarkGray)
            
            Text("General Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Aktivasi Lisensi Pro")
                Switch(isPremium, { 
                    isPremium = it
                    prefs.edit().putBoolean("is_premium", it).apply()
                }, Modifier.scale(0.8f))
            }
            
            Spacer(Modifier.height(32.dp))
            Text("Engine Info", fontSize = 14.sp, color = Color.Gray)
            Text("Hybrid Architecture: Kotlin + Rust NDK", fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}
