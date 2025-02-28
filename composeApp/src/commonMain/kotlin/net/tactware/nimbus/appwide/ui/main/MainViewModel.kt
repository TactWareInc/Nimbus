package net.tactware.nimbus.appwide.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.projects.bl.GetAllProjectNameUseCase
import net.tactware.nimbus.projects.bl.GetAllProjectsFlowUseCase
import org.koin.core.annotation.Factory

@Factory
class MainViewModel(getAllProjectNameUseCase: GetAllProjectNameUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = UiState.LoadedProjects(getAllProjectNameUseCase.invoke())
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