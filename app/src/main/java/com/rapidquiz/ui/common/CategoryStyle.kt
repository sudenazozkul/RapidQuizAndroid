package com.rapidquiz.ui.common

import androidx.compose.ui.graphics.Color
import com.rapidquiz.ui.theme.Brand

// Kategori görselleri: ikon adı → emoji; renk API'den gelir, gelmezse slug'a göre yedek (web utils/category.ts).
private val ICONS = mapOf(
    "code" to "💻",
    "brain" to "🧠",
    "cpu" to "🖥️",
    "globe" to "🌍",
    "atom" to "⚛️",
)

private val FALLBACK_COLORS = mapOf(
    "yazilim" to 0xFF3B82F6,
    "yapay-zeka" to 0xFFA855F7,
    "bilgisayar-muhendisligi" to 0xFF14B8A6,
    "ulkeler" to 0xFFF97316,
    "fizik" to 0xFFEC4899,
)

fun categoryEmoji(icon: String?): String = ICONS[icon] ?: "✨"

fun categoryColor(hex: String?, slug: String): Color =
    parseHexColor(hex) ?: FALLBACK_COLORS[slug]?.let { Color(it) } ?: Brand

/** `#RRGGBB` biçimini ayrıştırır; geçersizse null (android.graphics'e bağımlı değil, birim testlenebilir). */
internal fun parseHexColor(hex: String?): Color? {
    val digits = hex?.trim()?.removePrefix("#") ?: return null
    if (digits.length != 6) return null
    val rgb = digits.toLongOrNull(16) ?: return null
    return Color(0xFF000000 or rgb)
}
