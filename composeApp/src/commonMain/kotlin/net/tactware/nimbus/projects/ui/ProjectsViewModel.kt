package net.tactware.nimbus.projects.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class ProjectsViewModel(
    @InjectedParam
    internal val initialProjects: List<String>
) : ViewModel() {

    private val _selectedProject = MutableStateFlow(0)
    val selectedProject = _selectedProject.asStateFlow()

    private val _projects = MutableStateFlow(initialProjects)
    val projectsFlow = _projects.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(
        if (initialProjects.isNotEmpty()) {
            UiState.SpecificProject(initialProjects[0])
        } else {
            UiState.AddProject
        }
    )
    val uiState = _uiState.asStateFlow()

    internal fun onInteraction(interaction: ProjectsViewInteractions) {
        when (interaction) {
            is ProjectsViewInteractions.SelectProject -> {
                _selectedProject.value = interaction.index
                _uiState.value = UiState.SpecificProject(interaction.project)

            }

            ProjectsViewInteractions.AddProject -> {
                _selectedProject.value = projectsFlow.value.size
                _uiState.value = UiState.AddProject
            }
        }
    }

    sealed class UiState {
        data class SpecificProject(val project: String) : UiState()

        data object AddProject : UiState()
    }
}
