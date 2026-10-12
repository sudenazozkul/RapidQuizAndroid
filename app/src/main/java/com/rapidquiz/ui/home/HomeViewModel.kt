package com.rapidquiz.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rapidquiz.data.api.dto.Category
import com.rapidquiz.data.repo.ApiResult
import com.rapidquiz.data.repo.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    /** [slow]: yükleme 5 sn'yi aştı → "Sunucu uyanıyor…" metni gösterilir. */
    data class Loading(val slow: Boolean = false) : HomeUiState
    data class Loaded(val categories: List<Category>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        // Açılışta sunucuyu uyandır (sonuç beklenmez).
        viewModelScope.launch { repository.wakeUp() }
        load()
    }

    fun load() {
        loadJob?.cancel()
        _state.value = HomeUiState.Loading()
        val slowTimer = viewModelScope.launch {
            delay(SLOW_AFTER_MS)
            if (_state.value is HomeUiState.Loading) _state.value = HomeUiState.Loading(slow = true)
        }
        loadJob = viewModelScope.launch {
            val result = repository.categories()
            slowTimer.cancel()
            _state.value = when (result) {
                is ApiResult.Success -> HomeUiState.Loaded(result.value)
                is ApiResult.Failure -> HomeUiState.Error(result.error.message)
            }
        }
    }

    companion object {
        const val SLOW_AFTER_MS = 5_000L

        fun factory(repository: QuizRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
