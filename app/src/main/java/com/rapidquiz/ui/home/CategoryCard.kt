package com.rapidquiz.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rapidquiz.data.api.dto.Category
import com.rapidquiz.ui.common.categoryColor
import com.rapidquiz.ui.common.categoryEmoji
import com.rapidquiz.ui.theme.Brand

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = categoryColor(category.color, category.slug)
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(8.dp, shape, ambientColor = Brand.copy(alpha = 0.25f), spotColor = Brand.copy(alpha = 0.25f))
            .clip(shape)
            .background(Color.White)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "${category.name}, ${category.questionCount} soru. ${category.description}"
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(8.dp).fillMaxHeight().background(color))
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 20.dp, bottom = 20.dp)
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(color.copy(alpha = 0.15f))
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            Text(categoryEmoji(category.icon), fontSize = 28.sp)
        }
        Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp).weight(1f)) {
            Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            if (category.description.isNotBlank()) {
                Text(
                    category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            Text(
                "${category.questionCount} soru",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
