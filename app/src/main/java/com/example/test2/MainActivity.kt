package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.test2.ui.screens.MainCalculatorScreen
import com.example.test2.ui.screens.SettingsScreen
import com.example.test2.ui.theme.Test2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ダークモードの状態管理（デフォルトでダークモード）
            var isDarkTheme by remember { mutableStateOf(true) }
            var showSettings by remember { mutableStateOf(false) }
            
            Test2Theme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showSettings) {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { isDarkTheme = it },
                            onBackClick = { showSettings = false },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        MainCalculatorScreen(
                            isDarkTheme = isDarkTheme,
                            onSettingsClick = { showSettings = true },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Test2Theme {
        MainCalculatorScreen(
            isDarkTheme = true,
            onSettingsClick = {}
        )
    }
}