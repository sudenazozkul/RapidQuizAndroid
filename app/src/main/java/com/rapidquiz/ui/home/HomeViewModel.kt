package com.rapidquiz.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rapidquiz.data.api.dto.Category
import com.rapidquiz.data.repo.ApiResult
import com.rapidquiz.data.repo.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Loaded(val categories: List<Category>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        // Açılışta sunucuyu uyandır (sonuç beklenmez).
        viewModelScope.launch { repository.wakeUp() }
        load()
    }

    fun load() {
        _state.value = HomeUiState.Loading
        viewModelScope.launch {
            _state.value = when (val r = repository.categories()) {
                is ApiResult.Success -> {
                    Log.d(TAG, "categories OK: ${r.value.size} adet -> ${r.value.joinToString { it.slug }}")
                    HomeUiState.Loaded(r.value)
                }
                is ApiResult.Failure -> {
                    Log.w(TAG, "categories FAIL: ${r.error}")
                    HomeUiState.Error(r.error.message)
                }
            }
        }
    }

    companion object {
        private const val TAG = "RapidQuiz"

        fun factory(repository: QuizRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
