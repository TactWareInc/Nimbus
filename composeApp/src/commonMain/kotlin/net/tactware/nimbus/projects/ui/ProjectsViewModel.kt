package net.tactware.nimbus.projects.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.projects.bl.GetAllProjectsFlowUseCase
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

@Factory
class ProjectsViewModel(
    @InjectedParam
    internal val initialProjects: List<ProjectIdentifier>,
    private val getAllProjectsFlowUseCase: GetAllProjectsFlowUseCase
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

    init {
        viewModelScope.launch(Dispatchers.Default) {
            getAllProjectsFlowUseCase.invoke().collect {
                val names = it.map { p -> ProjectIdentifier(Uuid.parse(p.id), p.name) }
                _projects.value = names
            }
        }
    }

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
        data class SpecificProject(val project: ProjectIdentifier) : UiState()

        data object AddProject : UiState()
    }
}
