package com.builder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardGalleryScreen() {
    var liked by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Card Gallery",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Basic Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* click */ }
        ) {
            Text(
                text = "Basic Card with onClick",
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Card with elevation
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "Elevated Card (shadow effect)",
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Card with buttons
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Card with Actions", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { liked = !liked }) {
                        Text(if (liked) "Liked!" else "Like")
                    }
                    OutlinedButton(onClick = {}) {
                        Text("Share")
                    }
                }
            }
        }
        
        // Outlined Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Outlined Card (border only, no elevation)",
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                text = "🎴 Card bisa untuk: menu, profile, product, notification, dll",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
