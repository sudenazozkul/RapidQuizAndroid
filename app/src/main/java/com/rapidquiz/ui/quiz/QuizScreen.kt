package com.rapidquiz.ui.quiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rapidquiz.ui.theme.AppBackground

// M3'te uygulanacak.
@Composable
fun QuizScreen(slug: String, onExit: () -> Unit) {
    AppBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
            Text("Quiz: $slug (M3)")
            Button(onClick = onExit) { Text("Ana Sayfa") }
        }
    }
}
