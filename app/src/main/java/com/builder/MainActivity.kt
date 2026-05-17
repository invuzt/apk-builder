package com.builder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                HelloComposeScreen()
            }
        }
    }
}

@Composable
fun HelloComposeScreen() {
    var count by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hello from Jetpack Compose!",
            fontSize = 24.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Count: $count",
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { count++ }
        ) {
            Text("Click Me")
        }
    }
}
