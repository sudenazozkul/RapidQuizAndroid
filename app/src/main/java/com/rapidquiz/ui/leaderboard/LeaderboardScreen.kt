package com.rapidquiz.ui.leaderboard

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

// M2'de uygulanacak. `initialSlug` verilmezse ilk kategori seçili açılır.
@Composable
fun LeaderboardScreen(initialSlug: String?, onHome: () -> Unit) {
    AppBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
            Text("Skor Tablosu: ${initialSlug ?: "ilk kategori"} (M2)")
            Button(onClick = onHome) { Text("Ana Sayfa") }
        }
    }
}
