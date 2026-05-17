package com.builder.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimationGalleryScreen() {
    var expanded by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    var colorIndex by remember { mutableStateOf(0) }
    
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta, Color.Cyan)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Animation Gallery",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // 1. Size animation
        Card(
            onClick = { expanded = !expanded }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("1. Size Animation (click me!)")
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .size(if (expanded) 150.dp else 50.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .animateContentSize()
                )
            }
        }
        
        // 2. Fade animation
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Fade Animation")
                Button(onClick = { visible = !visible }) {
                    Text(if (visible) "Hide" else "Show")
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
        }
        
        // 3. Color animation
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("3. Color Animation")
                Button(onClick = { colorIndex = (colorIndex + 1) % colors.size }) {
                    Text("Change Color")
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(colors[colorIndex])
                )
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Text(
                text = "✨ Animasi di Compose: animateContentSize, AnimatedVisibility, animateColorAsState, dan masih banyak lagi!",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
