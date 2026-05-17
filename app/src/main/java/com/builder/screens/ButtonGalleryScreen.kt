package com.builder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonGalleryScreen() {
    var clickCount by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Button Gallery",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text("Clicked: $clickCount times")
        
        Button(onClick = { clickCount++ }) {
            Text("Filled Button")
        }
        
        OutlinedButton(onClick = { clickCount++ }) {
            Text("Outlined Button")
        }
        
        TextButton(onClick = { clickCount++ }) {
            Text("Text Button")
        }
        
        ElevatedButton(onClick = { clickCount++ }) {
            Text("Elevated Button")
        }
        
        FilledTonalButton(onClick = { clickCount++ }) {
            Text("Filled Tonal Button")
        }
        
        IconButton(onClick = { clickCount++ }) {
            Icon(Icons.Default.Home, contentDescription = "Home")
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                text = "💡 Tip: Semua button bisa diklik dan menghitung jumlah klik!",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
