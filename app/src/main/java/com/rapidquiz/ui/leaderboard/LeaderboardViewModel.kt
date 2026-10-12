package com.rapidquiz.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rapidquiz.data.api.dto.Category
import com.rapidquiz.data.api.dto.LeaderboardEntry
import com.rapidquiz.data.repo.ApiResult
import com.rapidquiz.data.repo.QuizRepository
import com.rapidquiz.domain.QuizSessionHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EntriesState {
    data object Loading : EntriesState
    data class Loaded(val entries: List<LeaderboardEntry>) : EntriesState
    data class Error(val message: String) : EntriesState
}

data class LeaderboardUiState(
    /** null: kategoriler henüz yüklenmedi. */
    val categories: List<Category>? = null,
    val categoriesError: String? = null,
    val selectedSlug: String? = null,
    val entries: EntriesState = EntriesState.Loading,
    /** Az önce kaydedilen skorun sırası; yalnızca o kategoride ve ilk 10'daysa dolu. */
    val highlightRank: Int? = null,
)

class LeaderboardViewModel(
    private val repository: QuizRepository,
    private val holder: QuizSessionHolder,
    private val initialSlug: String?,
) : ViewModel() {
    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    private var entriesJob: Job? = null

    init {
        loadCategories()
    }

    fun loadCategories() {
        _state.value = _state.value.copy(categoriesError = null)
        viewModelScope.launch {
            when (val r = repository.categories()) {
                is ApiResult.Success -> {
                    val slug = initialSlug?.takeIf { s -> r.value.any { it.slug == s } }
                        ?: r.value.firstOrNull()?.slug
                    _state.value = _state.value.copy(categories = r.value)
                    if (slug != null) select(slug)
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(categoriesError = r.error.message)
            }
        }
    }

    fun select(slug: String) {
        entriesJob?.cancel() // önceki sekmenin geç gelen yanıtı yenisini ezmesin
        _state.value = _state.value.copy(selectedSlug = slug, entries = EntriesState.Loading, highlightRank = null)
        entriesJob = viewModelScope.launch {
            when (val r = repository.leaderboard(slug)) {
                is ApiResult.Success -> {
                    val last = holder.lastScore.value
                    val highlight = last?.takeIf { it.categorySlug == slug && it.inTop10 }?.rank
                    _state.value = _state.value.copy(entries = EntriesState.Loaded(r.value), highlightRank = highlight)
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(entries = EntriesState.Error(r.error.message))
            }
        }
    }

    fun retryEntries() {
        _state.value.selectedSlug?.let(::select)
    }

    companion object {
        fun factory(repository: QuizRepository, holder: QuizSessionHolder, initialSlug: String?): ViewModelProvider.Factory =
            viewModelFactory { initializer { LeaderboardViewModel(repository, holder, initialSlug) } }
    }
}
