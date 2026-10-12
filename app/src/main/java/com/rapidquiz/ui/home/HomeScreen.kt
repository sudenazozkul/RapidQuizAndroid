package com.rapidquiz.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rapidquiz.ui.common.ErrorCard
import com.rapidquiz.ui.common.LoadingSkeleton
import com.rapidquiz.ui.theme.AppBackground
import com.rapidquiz.ui.theme.Brand
import com.rapidquiz.ui.theme.Ink
import com.rapidquiz.ui.theme.Secondary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategoryClick: (String) -> Unit,
    onLeaderboardClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(state, viewModel::load, onCategoryClick, onLeaderboardClick)
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onRetry: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onLeaderboardClick: () -> Unit,
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "⚡ Rapid Quiz",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Brand,
                modifier = Modifier.semantics { heading() },
            )
            Text("20 soru, 5 saniye", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                "Bir kategori seç ve hızını göster.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink.copy(alpha = 0.7f),
            )

            when (state) {
                is HomeUiState.Loading -> {
                    repeat(4) { LoadingSkeleton(height = 112.dp) }
                    if (state.slow) {
                        Text(
                            "Sunucu uyanıyor, ~1 dk sürebilir…",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                is HomeUiState.Error -> ErrorCard(
                    title = "Kategoriler yüklenemedi",
                    detail = state.message,
                    onAction = onRetry,
                )
                is HomeUiState.Loaded -> state.categories.forEach { category ->
                    CategoryCard(category, onClick = { onCategoryClick(category.slug) })
                }
            }

            Button(
                onClick = onLeaderboardClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(top = 8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Ink),
            ) { Text("🏆 Skor Tablosu", fontWeight = FontWeight.ExtraBold) }
        }
    }
}
