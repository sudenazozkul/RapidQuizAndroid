package com.rapidquiz.ui.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rapidquiz.data.api.dto.LeaderboardEntry
import com.rapidquiz.ui.theme.Accent
import com.rapidquiz.ui.theme.Brand

private val MEDALS = listOf("🥇", "🥈", "🥉")

@Composable
fun LeaderboardTable(entries: List<LeaderboardEntry>, highlightRank: Int?, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) {
        Text(
            "Henüz skor yok. İlk sen ol!",
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(Color.White)
                .padding(24.dp),
        )
        return
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.forEach { LeaderboardRow(it, highlighted = it.rank == highlightRank) }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, highlighted: Boolean) {
    val shape = MaterialTheme.shapes.medium
    val description = buildString {
        append("${entry.rank}. sıra, ${entry.playerName}")
        if (highlighted) append(" (Sen)")
        append(", ${entry.score} puan, ${entry.correctCount} doğru")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .shadow(4.dp, shape, ambientColor = Brand.copy(alpha = 0.25f), spotColor = Brand.copy(alpha = 0.25f))
            .clip(shape)
            .background(if (highlighted) Accent else Color.White)
            .then(if (highlighted) Modifier.border(BorderStroke(3.dp, Brand.copy(alpha = 0.4f)), shape) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = MEDALS.getOrNull(entry.rank - 1) ?: entry.rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp).clearAndSetSemantics {},
        )
        Text(
            text = if (highlighted) "${entry.playerName} (Sen)" else entry.playerName,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).clearAndSetSemantics {},
        )
        Text(
            text = "${entry.correctCount}✓",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = entry.score.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (highlighted) Color.White else Accent)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clearAndSetSemantics {},
        )
    }
}
