package com.builder.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextFieldGalleryScreen() {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TextField Gallery",
            style = MaterialTheme.typography.headlineMedium
        )
        
        OutlinedTextField(
            value = text1,
            onValueChange = { text1 = it },
            label = { Text("Outlined TextField") },
            placeholder = { Text("Type something...") },
            modifier = Modifier.fillMaxWidth()
        )
        
        TextField(
            value = text2,
            onValueChange = { text2 = it },
            label = { Text("Filled TextField") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = text3,
            onValueChange = { text3 = it },
            label = { Text("Disabled TextField") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Text(
                text = "📝 Preview: $text1",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
