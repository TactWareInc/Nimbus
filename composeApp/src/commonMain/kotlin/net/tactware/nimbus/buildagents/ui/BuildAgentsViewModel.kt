package net.tactware.nimbus.buildagents.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.buildagents.dal.BuildAgentInfo
import net.tactware.nimbus.projects.dal.ProjectsRepository
import org.koin.core.annotation.Factory

/**
 * ViewModel for the Build Agents UI.
 */
@Factory
class BuildAgentsViewModel(
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    /**
     * Sealed class representing different UI states for the build agents page.
     */
    sealed class UiState {
        object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class Success(val agents: List<BuildAgent>) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Load build agents when the ViewModel is created
        loadBuildAgents()
    }

    /**
     * Loads the list of build agents from Azure DevOps.
     */
    private fun loadBuildAgents() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                // Get the current project
                val projects = projectsRepository.getProjects()
                if (projects.isEmpty()) {
                    _uiState.value = UiState.Error("No projects configured. Please add a project first.")
                    return@launch
                }

                // Use the first project for now
                val project = projects.first()
                val client = AzureDevOpsClient(project)

                // Fetch build agents
                val buildAgents = client.getBuildAgents()

                // Map BuildAgentInfo to BuildAgent
                val agents = buildAgents.map { agentInfo ->
                    BuildAgent(
                        id = agentInfo.id.toString(),
                        name = agentInfo.name,
                        description = "Agent status: ${agentInfo.status}",
                        isOnline = agentInfo.status.equals("online", ignoreCase = true)
                    )
                }

                _uiState.value = UiState.Success(agents)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error loading build agents: ${e.message}")
            }
        }
    }

    /**
     * Refreshes the list of build agents.
     * This can be called when the user wants to manually refresh the data.
     */
    fun refreshAgents() {
        loadBuildAgents()
    }
}
