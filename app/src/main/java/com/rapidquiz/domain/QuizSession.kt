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

/** Quiz → Sonuç ekranları arasında paylaşılan oturum; Application seviyesinde yaşar. */
class QuizSessionHolder {
    private val _session = MutableStateFlow<QuizSession?>(null)
    val session: StateFlow<QuizSession?> = _session.asStateFlow()

    fun start(session: QuizSession) {
        _session.value = session
    }

    fun update(transform: (QuizSession) -> QuizSession) {
        _session.value = _session.value?.let(transform)
    }

    fun clear() {
        _session.value = null
    }
}
