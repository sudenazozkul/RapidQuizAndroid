package com.rapidquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rapidquiz.ui.nav.AppNavHost
import com.rapidquiz.ui.theme.RapidQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RapidQuizApp).container
        setContent {
            RapidQuizTheme {
                AppNavHost(container)
            }
        }
    }
}
