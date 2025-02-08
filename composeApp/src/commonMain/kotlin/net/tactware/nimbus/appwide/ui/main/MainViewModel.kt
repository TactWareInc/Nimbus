package net.tactware.nimbus.appwide.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            delay(2000)
            _uiState.value = UiState.LoadedProjects(listOf())
        }
    }

    internal fun onInteraction(interaction: MainViewInteractions) {
        when (interaction) {
            MainViewInteractions.RetryLoadProject -> {
                _uiState.value = UiState.Loading
            }
        }
    }

    sealed class UiState {
        data object Loading : UiState()

        data class LoadedProjects(val projects: List<String>) : UiState()

        data object Error : UiState()
    }
}