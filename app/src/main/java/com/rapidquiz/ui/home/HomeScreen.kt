package com.rapidquiz.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rapidquiz.ui.theme.AppBackground

// M1 taslağı: gerçek tasarım M2'de (kategori kartları, iskelet, uyanıyor metni).
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategoryClick: (String) -> Unit,
    onLeaderboardClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("⚡ Rapid Quiz", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text("20 soru, 5 saniye", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            when (val s = state) {
                HomeUiState.Loading -> Text("Yükleniyor…")
                is HomeUiState.Error -> {
                    Text(s.message)
                    Button(onClick = viewModel::load) { Text("Tekrar Dene") }
                }
                is HomeUiState.Loaded -> s.categories.forEach { c ->
                    Button(onClick = { onCategoryClick(c.slug) }, modifier = Modifier.fillMaxWidth()) {
                        Text("${c.name} (${c.questionCount})")
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onLeaderboardClick, modifier = Modifier.fillMaxWidth()) { Text("🏆 Skor Tablosu") }
        }
    }
}
