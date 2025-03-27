package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.gitrepos.bl.GetReposByProjectIdUseCase
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.bl.ProjectUpdater
import net.tactware.nimbus.projects.dal.entities.WorkItem
import net.tactware.nimbus.projects.dal.entities.azurejson.workitem.WorkItemResponse
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

@Factory
class SpecificProjectViewModel(
    @InjectedParam
    projectId: Uuid,
    projectUpdater: ProjectUpdater,
    getReposByProjectIdUseCase: GetReposByProjectIdUseCase,
    getProjectByIdUseCase: GetProjectByIdUseCase
) : ViewModel() {

    private val _projectGitRepos = MutableStateFlow<List<GitRepo>>(emptyList())
    val projectGitRepos = _projectGitRepos.asStateFlow()

    private val _workItems = MutableStateFlow<List<WorkItem>>(emptyList())
    val workItems = _workItems.asStateFlow()

    init {
        viewModelScope.launch {
            projectUpdater.update(projectId)
        }

        viewModelScope.launch {
            getReposByProjectIdUseCase.invoke(projectId).collect {
                _projectGitRepos.value = it
            }
        }

        // Fetch work items
        viewModelScope.launch {
            val project = getProjectByIdUseCase.invoke(projectId)
            if (project != null) {
                val client = AzureDevOpsClient(project)
                try {

                    _workItems.value = client.getWorkItems()
                } catch (e: Exception) {
                    // Handle error - for now, we'll just keep the empty list
                    println("Error fetching work items: ${e.message}")
                }
            }
        }
    }
}
