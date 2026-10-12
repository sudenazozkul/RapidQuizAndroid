package com.rapidquiz.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rapidquiz.data.api.dto.Category
import com.rapidquiz.ui.common.ErrorCard
import com.rapidquiz.ui.common.LoadingSkeleton
import com.rapidquiz.ui.common.categoryColor
import com.rapidquiz.ui.common.categoryEmoji
import com.rapidquiz.ui.theme.AppBackground
import com.rapidquiz.ui.theme.Brand
import com.rapidquiz.ui.theme.Ink

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel, onHome: () -> Unit, onPlayAgain: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LeaderboardContent(
        state = state,
        onHome = onHome,
        onPlayAgain = onPlayAgain,
        onSelect = viewModel::select,
        onRetryCategories = viewModel::loadCategories,
        onRetryEntries = viewModel::retryEntries,
    )
}

@Composable
fun LeaderboardContent(
    state: LeaderboardUiState,
    onHome: () -> Unit,
    onPlayAgain: (String) -> Unit,
    onSelect: (String) -> Unit,
    onRetryCategories: () -> Unit,
    onRetryEntries: () -> Unit,
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "🏆 Skor Tablosu",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Brand,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                TextButton(onClick = onHome, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Ana Sayfa", fontWeight = FontWeight.ExtraBold)
                }
            }

            val categories = state.categories
            when {
                state.categoriesError != null -> ErrorCard(
                    title = "Skor tablosu yüklenemedi",
                    detail = state.categoriesError,
                    onAction = onRetryCategories,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                categories == null -> Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) { repeat(5) { LoadingSkeleton(height = 56.dp) } }
                else -> {
                    CategoryTabs(categories, state.selectedSlug, onSelect)
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (val e = state.entries) {
                            EntriesState.Loading -> repeat(5) { LoadingSkeleton(height = 56.dp) }
                            is EntriesState.Error -> ErrorCard(
                                title = "Skor tablosu yüklenemedi",
                                detail = e.message,
                                onAction = onRetryEntries,
                            )
                            is EntriesState.Loaded -> LeaderboardTable(e.entries, state.highlightRank)
                        }
                    }
                    state.selectedSlug?.let { slug ->
                        Button(
                            onClick = { onPlayAgain(slug) },
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().heightIn(min = 48.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) { Text("🔁 Tekrar Oyna", fontWeight = FontWeight.ExtraBold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTabs(categories: List<Category>, selectedSlug: String?, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = { it.slug }) { category ->
            val selected = category.slug == selectedSlug
            val shape = MaterialTheme.shapes.medium
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(if (selected) Brand else Color.White)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(category.slug) })
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(categoryEmoji(category.icon), modifier = Modifier.semantics { /* dekoratif */ })
                Text(
                    category.name,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Color.White else Ink,
                    textAlign = TextAlign.Center,
                )
                if (selected) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(categoryColor(category.color, category.slug)))
                }
            }
        }
    }
}
