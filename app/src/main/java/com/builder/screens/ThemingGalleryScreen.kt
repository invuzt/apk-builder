package com.builder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemingGalleryScreen() {
    var useDarkTheme by remember { mutableStateOf(false) }
    
    val customColorScheme = if (useDarkTheme) {
        darkColorScheme(
            primary = MaterialTheme.colorScheme.primary,
            secondary = MaterialTheme.colorScheme.secondary
        )
    } else {
        lightColorScheme(
            primary = MaterialTheme.colorScheme.primary,
            secondary = MaterialTheme.colorScheme.secondary
        )
    }
    
    MaterialTheme(
        colorScheme = customColorScheme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Theming Gallery",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Theme:", style = MaterialTheme.typography.titleMedium)
                    Text("Dark Mode: ${if (useDarkTheme) "ON" else "OFF"}")
                    Button(onClick = { useDarkTheme = !useDarkTheme }) {
                        Text("Toggle Dark Mode")
                    }
                }
            }
            
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Material 3 Color System:", style = MaterialTheme.typography.titleMedium)
                    Row {
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.error
                        ).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(color)
                            )
                        }
                    }
                }
            }
            
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Typography:", style = MaterialTheme.typography.titleMedium)
                    Text("Display Large", style = MaterialTheme.typography.displayLarge)
                    Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
                    Text("Body Large", style = MaterialTheme.typography.bodyLarge)
                    Text("Label Small", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "🎨 Material You: Warna adaptif dari wallpaper HP (Android 12+)",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
