package com.rapidquiz.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Aktif oturum. Yalnızca bellekte tutulur; diske asla yazılmaz (altın kural 1). */
data class QuizSession(
    val sessionId: String,
    val token: String,
    val categorySlug: String,
    val categoryName: String,
    val totalQuestions: Int,
    val score: Int = 0,
) {
    // Token log/toString'e sızmasın.
    override fun toString() = "QuizSession(sessionId=$sessionId, category=$categorySlug, score=$score)"
}

/** Az önce kaydedilen skor; Skor Tablosu'nda satırı vurgulamak için (yalnızca bellekte). */
data class LastScore(val categorySlug: String, val rank: Int, val inTop10: Boolean)

/** Quiz → Sonuç ekranları arasında paylaşılan oturum; Application seviyesinde yaşar. */
class QuizSessionHolder {
    private val _session = MutableStateFlow<QuizSession?>(null)
    val session: StateFlow<QuizSession?> = _session.asStateFlow()

    private val _lastScore = MutableStateFlow<LastScore?>(null)
    val lastScore: StateFlow<LastScore?> = _lastScore.asStateFlow()

    /** Yeni oturum başlayınca eski vurgu da silinir. */
    fun start(session: QuizSession) {
        _lastScore.value = null
        _session.value = session
    }

    fun setLastScore(score: LastScore) {
        _lastScore.value = score
    }

    fun update(transform: (QuizSession) -> QuizSession) {
        _session.value = _session.value?.let(transform)
    }

    fun clear() {
        _session.value = null
    }
}
