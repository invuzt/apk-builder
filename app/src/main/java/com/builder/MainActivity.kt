package com.builder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.builder.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    "buttons" to "Buttons",
                    "textfields" to "TextFields",
                    "lists" to "Lists",
                    "cards" to "Cards",
                    "animations" to "Animations",
                    "theming" to "Theming"
                )
                items.forEach { (route, title) ->
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(route) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "buttons",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("buttons") { ButtonGalleryScreen() }
            composable("textfields") { TextFieldGalleryScreen() }
            composable("lists") { ListGalleryScreen() }
            composable("cards") { CardGalleryScreen() }
            composable("animations") { AnimationGalleryScreen() }
            composable("theming") { ThemingGalleryScreen() }
        }
    }
}
